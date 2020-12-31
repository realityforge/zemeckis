package zemeckis;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.realityforge.braincheck.Guards.*;

/**
 * Base executor which other executors can extend.
 */
abstract class AbstractExecutor
  implements VirtualProcessorUnit.Executor
{
  /**
   * The size of the circular buffer when initially created.
   */
  private static final int INITIAL_QUEUE_SIZE = 100;
  /**
   * A queue containing tasks that have been scheduled but are not yet executing.
   */
  @Nonnull
  private final CircularBuffer<Runnable> _taskQueue;
  @Nullable
  private VirtualProcessorUnit.Context _context;

  AbstractExecutor()
  {
    _taskQueue = new CircularBuffer<>( INITIAL_QUEUE_SIZE );
  }

  final int getQueueSize()
  {
    return getTaskQueue().size();
  }

  @Override
  public void queue( @Nonnull final Runnable task )
  {
    ensureNotQueued( task );
    _taskQueue.add( Objects.requireNonNull( task ) );
  }

  @Override
  public final void queueNext( @Nonnull final Runnable task )
  {
    ensureNotQueued( task );
    _taskQueue.addFirst( Objects.requireNonNull( task ) );
  }

  private void ensureNotQueued( @Nonnull final Runnable task )
  {
    if ( Zemeckis.shouldCheckInvariants() )
    {
      invariant( () -> !_taskQueue.contains( task ),
                 () -> "Zemeckis-0001: Attempting to queue task " + task + " when task is already queued." );
    }
  }

  @Nonnull
  final CircularBuffer<Runnable> getTaskQueue()
  {
    return _taskQueue;
  }

  final void executeNextTask()
  {
    final Runnable task = _taskQueue.pop();
    assert null != task;
    try
    {
      task.run();
    }
    catch ( final Throwable t )
    {
      Zemeckis.reportUncaughtError( t );
    }
  }

  @Override
  public void init( @Nonnull final VirtualProcessorUnit.Context context )
  {
    _context = Objects.requireNonNull( context );
  }

  @Nonnull
  final VirtualProcessorUnit.Context context()
  {
    assert null != _context;
    return _context;
  }

  /**
   * Mark executor as ready for activation.
   * This typically means scheduling Executor to call activate on the correct VPU.
   */
  abstract void scheduleForActivation();
}
