package zemeckis;

import static org.realityforge.braincheck.Guards.*;

import java.util.Objects;
import org.jetbrains.annotations.TestOnly;
import org.jspecify.annotations.Nullable;

/**
 * Base executor which other executors can extend.
 */
abstract class AbstractExecutor implements VirtualProcessorUnit.Executor {
    /**
     * The size of the circular buffer when initially created.
     */
    private static final int INITIAL_QUEUE_SIZE = 100;
    /**
     * A queue containing tasks that have been scheduled but are not yet executing.
     */
    private final CircularBuffer<TaskEntry> _taskQueue;

    private VirtualProcessorUnit.@Nullable Context _context;

    AbstractExecutor() {
        _taskQueue = new CircularBuffer<>(INITIAL_QUEUE_SIZE);
    }

    final int getQueueSize() {
        return getTaskQueue().size();
    }

    @Override
    @SuppressWarnings("Varifier")
    public final synchronized Cancelable queue(@Nullable final String name, final Runnable task) {
        final boolean needsActivation = 0 == getQueueSize();
        ensureNotQueued(name, task);
        final TaskEntry entry = new TaskEntry(name, task, null);
        _taskQueue.add(entry);
        if (needsActivation) {
            scheduleForActivation();
        }
        return entry;
    }

    @Override
    public final void queueNext(@Nullable final String name, final Runnable task) {
        ensureNotQueued(name, task);
        _taskQueue.addFirst(new TaskEntry(name, task, null));
    }

    private void ensureNotQueued(@Nullable final String name, final Runnable task) {
        if (Zemeckis.shouldCheckInvariants()) {
            invariant(
                    () -> _taskQueue.stream().noneMatch(taskEntry -> taskEntry.getTask() == task),
                    () -> "Zemeckis-0001: Attempting to queue task named '" + name + "' when task is already queued.");
        }
    }

    final CircularBuffer<TaskEntry> getTaskQueue() {
        return _taskQueue;
    }

    final void executeNextTask() {
        final TaskEntry task = Objects.requireNonNull(_taskQueue.pop());
        try {
            task.execute();
        } catch (final Throwable t) {
            Zemeckis.reportUncaughtError(t);
        }
    }

    @Override
    public void init(final VirtualProcessorUnit.Context context) {
        _context = Objects.requireNonNull(context);
    }

    @TestOnly
    @Override
    public void reset() {
        _taskQueue.clear();
        _taskQueue.truncate(INITIAL_QUEUE_SIZE);
    }

    final VirtualProcessorUnit.Context context() {
        assert null != _context;
        return Objects.requireNonNull(_context);
    }

    /**
     * Mark executor as ready for activation.
     * This typically means scheduling Executor to call activate on the correct VPU.
     */
    abstract void scheduleForActivation();
}
