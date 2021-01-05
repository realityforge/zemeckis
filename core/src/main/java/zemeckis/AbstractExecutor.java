package zemeckis;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.TestOnly;
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
  private final CircularBuffer<TaskEntry> _taskQueue;
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
  @Nonnull
  public final synchronized Cancelable queue( @Nonnull final Runnable task )
  {
    final boolean needsActivation = 0 == getQueueSize();
    ensureNotQueued( task );
    final TaskEntry entry = new TaskEntry( task );
    _taskQueue.add( entry );
    if ( needsActivation )
    {
      scheduleForActivation();
    }
    return entry;
  }

  @Override
  public final void queueNext( @Nonnull final Runnable task )
  {
    ensureNotQueued( task );
    _taskQueue.addFirst( new TaskEntry( task ) );
  }

  private void ensureNotQueued( @Nonnull final Runnable task )
  {
    if ( Zemeckis.shouldCheckInvariants() )
    {
      invariant( () -> _taskQueue.stream().noneMatch( taskEntry -> taskEntry.getTask() == task ),
                 () -> "Zemeckis-0001: Attempting to queue task " + task + " when task is already queued." );
    }
  }

  @Nonnull
  final CircularBuffer<TaskEntry> getTaskQueue()
  {
    return _taskQueue;
  }

  final void executeNextTask()
  {
    final TaskEntry task = _taskQueue.pop();
    assert null != task;
    try
    {
      task.execute();
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

  @TestOnly
  @Override
  public void reset()
  {
    _taskQueue.clear();
    _taskQueue.truncate( INITIAL_QUEUE_SIZE );
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
