package zemeckis;

import elemental2.dom.Blob;
import elemental2.dom.DomGlobal;
import elemental2.dom.MessageEvent;
import elemental2.dom.URL;
import elemental2.dom.Worker;
import grim.annotations.OmitSymbol;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import org.jetbrains.annotations.TestOnly;
import static org.realityforge.braincheck.Guards.*;

/**
 * The scheduler is responsible for scheduling and executing tasks asynchronously.
 * The scheduler provides an "abstract asynchronous boundary" to stream operators.
 *
 * <p>The scheduler has an internal clock that represents time as a monotonically increasing
 * <code>int</code> value. The value may or may not have a direct relationship to wall-clock
 * time and the unit of the value is defined by the implementation.</p>
 */
final class TemporalScheduler
{
  @Nonnull
  private static AbstractScheduler c_scheduler = new SchedulerImpl();

  private TemporalScheduler()
  {
  }

  /**
   * Return a value representing the "current time" of the scheduler.
   *
   * @return the "current time" of the scheduler.
   */
  static int now()
  {
    return c_scheduler.now();
  }

  /**
   * Schedules the execution of the given task after a specified delay.
   *
   * @param name  A human consumable name for the task. It must be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task  the task to execute.
   * @param delay the delay before the task should execute. Must not be a negative value.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  static Cancelable delayedTask( @Nullable final String name, @Nonnull final Runnable task, final int delay )
  {
    return c_scheduler.delayedTask( name, task, delay );
  }

  /**
   * Schedules the periodic execution of the given task with specified period.
   *
   * @param name   A human consumable name for the task. It must be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task   the task to execute.
   * @param period the period after execution when the task should be re-executed. Must be a value greater than 0.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  static Cancelable periodicTask( @Nullable final String name, @Nonnull final Runnable task, final int period )
  {
    return c_scheduler.periodicTask( name, task, period );
  }

  @TestOnly
  static void reset()
  {
    c_scheduler.shutdown();
    c_scheduler = new SchedulerImpl();
  }

  private static final class SchedulerImpl
    extends AbstractScheduler
  {
    @GwtIncompatible
    private final ScheduledExecutorService _executorService = new ScheduledThreadPoolExecutor( 1, r -> {
      final Thread thread = new Thread( r, "Scheduler" );
      thread.setDaemon( true );
      thread.setUncaughtExceptionHandler( ( t, e ) -> Zemeckis.reportUncaughtError( e ) );
      return thread;
    } )
    {
      {
        setMaximumPoolSize( 1 );
      }
    };

    @GwtIncompatible
    void shutdown()
    {
      _executorService.shutdown();
      super.shutdown();
    }

    @GwtIncompatible
    @Nonnull
    @Override
    Cancelable doDelayedTask( @Nonnull final Runnable task, final int delay )
    {
      final ScheduledFuture<?> future = _executorService.schedule( task, delay, TimeUnit.MILLISECONDS );
      return () -> future.cancel( true );
    }

    @Nonnull
    @Override
    @GwtIncompatible
    Cancelable doPeriodicTask( @Nonnull final Runnable task, final int period )
    {
      final ScheduledFuture<?> future = _executorService.scheduleAtFixedRate( task, 0, period, TimeUnit.MILLISECONDS );
      return () -> future.cancel( true );
    }
  }

  private static abstract class AbstractScheduler
  {
    private static final boolean ENABLE_WORKERS = Zemeckis.useWorkerToScheduleDelayedTasks();
    @Nonnull
    private final String SRC =
      "var timers = {};\n" +
      "\n" +
      "function cancelTimer(id) {\n" +
      "  let timerId = timers[id] || 0;\n" +
      "  self.clearTimeout(timerId);\n" +
      "  timers.delete(id);\n" +
      "}\n" +
      "\n" +
      "function createTimer(id, delay) {\n" +
      "  timers[id] = self.setTimeout(() => {\n" +
      "    self.postMessage({ type: 'dt', id: id });\n" +
      "  }, delay);\n" +
      "}\n" +
      "\n" +
      "function cancelPeriodicTimer(id) {\n" +
      "  let timerId = timers[id] || 0;\n" +
      "  self.clearInterval(timerId);\n" +
      "  timers.delete(id);\n" +
      "}\n" +
      "\n" +
      "function createPeriodicTimer(id, period) {\n" +
      "  timers[id] = self.setInterval(() => {\n" +
      "    self.postMessage({ type: 'pt', id: id });\n" +
      "  }, period);\n" +
      "}\n" +
      "\n" +
      "self.onmessage = m => {\n" +
      "  if (m.data && m.data.action === '+' && m.data.type === 'dt') {\n" +
      "    createTimer(m.data.id, m.data.delay);\n" +
      "  } else if (m.data && m.data.action === '-' && m.data.type === 'dt') {\n" +
      "    cancelTimer(m.data.id);\n" +
      "  } else if (m.data && m.data.action === '+' && m.data.type === 'pt') {\n" +
      "    createPeriodicTimer(m.data.id, m.data.period);\n" +
      "  } else if (m.data && m.data.action === '-' && m.data.type === 'pt') {\n" +
      "    cancelPeriodicTimer(m.data.id);\n" +
      "  }\n" +
      "};";
    private final long _schedulerStart = System.currentTimeMillis();
    @OmitSymbol( unless = "zemeckis.use_worker_to_schedule_delayed_tasks" )
    private final Worker _worker =
      ENABLE_WORKERS ?
      new Worker(
        URL.createObjectURL(
          new Blob(
            new Blob.ConstructorBlobPartsArrayUnionType[]{ Blob.ConstructorBlobPartsArrayUnionType.of( SRC ) }
          )
        )
      ) :
      null;
    @OmitSymbol( unless = "zemeckis.use_worker_to_schedule_delayed_tasks" )
    private final Map<Double, Runnable> _workerTasks = ENABLE_WORKERS ? new HashMap<>() : null;
    @OmitSymbol( unless = "zemeckis.use_worker_to_schedule_delayed_tasks" )
    private double _nextTimerId = 1;

    {
      if ( ENABLE_WORKERS )
      {
        _worker.onmessage = this::onWorkerMessage;
      }
    }

    final long getSchedulerStart()
    {
      return _schedulerStart;
    }

    void shutdown()
    {
      if ( Zemeckis.useWorkerToScheduleDelayedTasks() )
      {
        _worker.terminate();
      }
    }

    final int now()
    {
      return (int) ( System.currentTimeMillis() - getSchedulerStart() );
    }

    final Cancelable delayedTask( @Nullable final String name, @Nonnull final Runnable task, final int delay )
    {
      if ( Zemeckis.shouldCheckApiInvariants() )
      {
        apiInvariant( () -> delay >= 0,
                      () -> "Zemeckis-0008: Zemeckis.delayedTask(...) named '" + name +
                            "' passed a negative delay. Actual value passed is " + delay );
      }
      return new TaskEntry( name, task, doDelayedTask( task, delay ) );
    }

    @Nonnull
    Cancelable doDelayedTask( @Nonnull final Runnable task, final int delay )
    {
      if ( Zemeckis.useWorkerToScheduleDelayedTasks() )
      {
        final double id = _nextTimerId++;
        _workerTasks.put( id, task );
        final JsPropertyMap<Object> message = msg( "+", "pt", id );
        message.set( "delay", delay );
        _worker.postMessage( message );
        return () -> {
          _workerTasks.remove( id );
          _worker.postMessage( msg( "-", "dt", id ) );
        };
      }
      else
      {
        final double timeoutId = DomGlobal.setTimeout( v -> task.run(), delay );
        return () -> DomGlobal.clearTimeout( timeoutId );
      }
    }

    @Nonnull
    final Cancelable periodicTask( @Nullable final String name, @Nonnull final Runnable task, final int period )
    {
      if ( Zemeckis.shouldCheckApiInvariants() )
      {
        apiInvariant( () -> period > 0,
                      () -> "Zemeckis-0009: Zemeckis.periodicTask(...) named '" + name +
                            "' passed a non-positive period. Actual value passed is " + period );
      }

      return new TaskEntry( name, task, doPeriodicTask( task, period ) );
    }

    @Nonnull
    Cancelable doPeriodicTask( @Nonnull final Runnable task, final int period )
    {
      if ( Zemeckis.useWorkerToScheduleDelayedTasks() )
      {
        final double id = _nextTimerId++;
        _workerTasks.put( id, task );
        final JsPropertyMap<Object> message = msg( "+", "pt", id );
        message.set( "period", period );
        _worker.postMessage( message );
        return () -> {
          _workerTasks.remove( id );
          _worker.postMessage( msg( "-", "pt", id ) );
        };
      }
      else
      {
        final double timeoutId = DomGlobal.setInterval( v -> task.run(), period );
        return () -> DomGlobal.clearInterval( timeoutId );
      }
    }

    @OmitSymbol( unless = "zemeckis.use_worker_to_schedule_delayed_tasks" )
    private void onWorkerMessage( @Nonnull final MessageEvent<Object> event )
    {
      if ( null != event.data )
      {
        final JsPropertyMap<Object> data = Js.asPropertyMap( event.data );
        final double id = data.getAsAny( "id" ).asDouble();
        final String type = data.getAsAny( "type" ).asString();
        if ( "dt".equals( type ) )
        {
          runTaskIfPresent( _workerTasks.remove( id ) );
        }
        else if ( "pt".equals( type ) )
        {
          runTaskIfPresent( _workerTasks.get( id ) );
        }
      }
    }

    @OmitSymbol( unless = "zemeckis.use_worker_to_schedule_delayed_tasks" )
    @Nonnull
    private JsPropertyMap<Object> msg( @Nonnull final String action, @Nonnull final String type, final double id )
    {
      return JsPropertyMap.of( "action", action, "type", type, "id", id );
    }

    @OmitSymbol( unless = "zemeckis.use_worker_to_schedule_delayed_tasks" )
    private void runTaskIfPresent( @Nullable final Runnable task )
    {
      if ( null != task )
      {
        task.run();
      }
    }
  }
}
