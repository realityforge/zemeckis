package zemeckis;

import elemental2.dom.DomGlobal;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
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
   * @param task  the task to execute.
   * @param delay the delay before the task should execute. Must not be a negative value.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  static Cancelable delayedTask( @Nonnull final Runnable task, final int delay )
  {
    return c_scheduler.delayedTask( task, delay );
  }

  /**
   * Schedules the periodic execution of the given task with specified period.
   *
   * @param task   the task to execute.
   * @param period the period after execution when the task should be re-executed. Must be a value greater than 0.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  static Cancelable periodicTask( @Nonnull final Runnable task, final int period )
  {
    return c_scheduler.periodicTask( task, period );
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
    private final long _schedulerStart = System.currentTimeMillis();

    final long getSchedulerStart()
    {
      return _schedulerStart;
    }

    void shutdown()
    {
    }

    final int now()
    {
      return (int) ( System.currentTimeMillis() - getSchedulerStart() );
    }

    final Cancelable delayedTask( @Nonnull final Runnable task, final int delay )
    {
      if ( Zemeckis.shouldCheckApiInvariants() )
      {
        apiInvariant( () -> delay >= 0,
                      () -> "Zemeckis-0008: Scheduler.delayedTask(...) passed a negative delay. " +
                            "Actual value passed is " + delay );
      }
      return doDelayedTask( task, delay );
    }

    @Nonnull
    Cancelable doDelayedTask( @Nonnull final Runnable task, final int delay )
    {
      final double timeoutId = DomGlobal.setTimeout( v -> task.run(), delay );
      return () -> DomGlobal.clearTimeout( timeoutId );
    }

    @Nonnull
    final Cancelable periodicTask( @Nonnull final Runnable task, final int period )
    {
      if ( Zemeckis.shouldCheckApiInvariants() )
      {
        apiInvariant( () -> period > 0,
                      () -> "Zemeckis-0009: Scheduler.periodicTask(...) passed a non-positive period. " +
                            "Actual value passed is " + period );
      }
      return doPeriodicTask( task, period );
    }

    @Nonnull
    Cancelable doPeriodicTask( @Nonnull final Runnable task, final int period )
    {
      final double timeoutId = DomGlobal.setInterval( v -> task.run(), period );
      return () -> DomGlobal.clearInterval( timeoutId );
    }
  }
}
