package zemeckis;

import static org.realityforge.braincheck.Guards.*;

import grim.annotations.OmitSymbol;
import java.util.Objects;
import java.util.PriorityQueue;
import org.jetbrains.annotations.TestOnly;
import org.jspecify.annotations.Nullable;

/**
 * Browser-independent temporal scheduler used by the J2CL test-scheduler target.
 */
@SuppressWarnings("Varifier")
final class TemporalScheduler {
    private static TestScheduler c_scheduler = new TestScheduler();

    private TemporalScheduler() {}

    static int now() {
        return c_scheduler.now();
    }

    static Cancelable delayedTask(@Nullable final String name, final Runnable task, final int delay) {
        return c_scheduler.delayedTask(name, task, delay);
    }

    static Cancelable periodicTask(@Nullable final String name, final Runnable task, final int period) {
        return c_scheduler.periodicTask(name, task, period);
    }

    @TestOnly
    static void reset() {
        c_scheduler.shutdown();
        c_scheduler = new TestScheduler();
    }

    @OmitSymbol(unless = "zemeckis.use_test_scheduler")
    @TestOnly
    static boolean pumpNext() {
        return c_scheduler.pumpNext();
    }

    @OmitSymbol(unless = "zemeckis.use_test_scheduler")
    @TestOnly
    static int pumpAll() {
        return c_scheduler.pumpAll();
    }

    private static final class TestScheduler {
        private static final int MAX_PUMPED_TASKS = 10_000;

        private final PriorityQueue<ScheduledTask> _tasks = new PriorityQueue<>();

        private long _now;

        private long _nextSequence;

        private void shutdown() {
            _tasks.clear();
        }

        private int now() {
            return (int) _now;
        }

        private Cancelable delayedTask(@Nullable final String name, final Runnable task, final int delay) {
            if (Zemeckis.shouldCheckApiInvariants()) {
                apiInvariant(
                        () -> delay >= 0,
                        () -> "Zemeckis-0008: Zemeckis.delayedTask(...) named '" + name
                                + "' passed a negative delay. Actual value passed is " + delay);
            }
            return new TaskEntry(name, task, schedule(task, delay, 0));
        }

        private Cancelable periodicTask(@Nullable final String name, final Runnable task, final int period) {
            if (Zemeckis.shouldCheckApiInvariants()) {
                apiInvariant(
                        () -> period > 0,
                        () -> "Zemeckis-0009: Zemeckis.periodicTask(...) named '" + name
                                + "' passed a non-positive period. Actual value passed is " + period);
            }
            return new TaskEntry(name, task, schedule(task, period, period));
        }

        private Cancelable schedule(final Runnable task, final int delay, final int period) {
            final ScheduledTask scheduledTask = new ScheduledTask(task, _now + delay, _nextSequence++, period);
            _tasks.add(scheduledTask);
            return scheduledTask;
        }

        private boolean pumpNext() {
            final ScheduledTask task = nextTask();
            if (null == task) {
                return false;
            }

            _tasks.remove();
            _now = task.getDueTime();
            task.execute();
            if (task.isPeriodic() && !task.isCanceled()) {
                task.reschedule(_nextSequence++);
                _tasks.add(task);
            }
            return true;
        }

        private int pumpAll() {
            int count = 0;
            while (count < MAX_PUMPED_TASKS && pumpNext()) {
                count++;
            }
            if (null != nextTask()) {
                throw new IllegalStateException(
                        "Unable to pump all tasks as more than " + MAX_PUMPED_TASKS + " tasks were executed");
            }
            return count;
        }

        @Nullable
        private ScheduledTask nextTask() {
            ScheduledTask task = _tasks.peek();
            while (null != task && task.isCanceled()) {
                _tasks.remove();
                task = _tasks.peek();
            }
            return task;
        }
    }

    private static final class ScheduledTask implements Cancelable, Comparable<ScheduledTask> {
        private final Runnable _task;
        private final int _period;
        private long _dueTime;
        private long _sequence;
        private boolean _canceled;

        private ScheduledTask(final Runnable task, final long dueTime, final long sequence, final int period) {
            _task = Objects.requireNonNull(task);
            _dueTime = dueTime;
            _sequence = sequence;
            _period = period;
        }

        @Override
        public void cancel() {
            _canceled = true;
        }

        @Override
        public int compareTo(final ScheduledTask other) {
            final int result = Long.compare(_dueTime, other._dueTime);
            return 0 != result ? result : Long.compare(_sequence, other._sequence);
        }

        private long getDueTime() {
            return _dueTime;
        }

        private boolean isCanceled() {
            return _canceled;
        }

        private boolean isPeriodic() {
            return 0 != _period;
        }

        private void execute() {
            _task.run();
        }

        private void reschedule(final long sequence) {
            _dueTime += _period;
            _sequence = sequence;
        }
    }
}
