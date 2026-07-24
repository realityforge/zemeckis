package zemeckis;

import static org.realityforge.braincheck.Guards.*;

import akasha.Blob;
import akasha.BlobPart;
import akasha.Console;
import akasha.MessageEvent;
import akasha.URL;
import akasha.WindowGlobal;
import akasha.Worker;
import akasha.WorkerOptions;
import grim.annotations.OmitSymbol;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import org.jetbrains.annotations.TestOnly;
import org.jspecify.annotations.Nullable;

/**
 * The scheduler is responsible for scheduling and executing tasks asynchronously.
 * The scheduler provides an "abstract asynchronous boundary" to stream operators.
 *
 * <p>The scheduler has an internal clock that represents time as a monotonically increasing
 * <code>int</code> value. The value may or may not have a direct relationship to wall-clock
 * time and the unit of the value is defined by the implementation.</p>
 */
final class TemporalScheduler {
    private static AbstractScheduler c_scheduler = createScheduler();

    private TemporalScheduler() {}

    /**
     * Return a value representing the "current time" of the scheduler.
     *
     * @return the "current time" of the scheduler.
     */
    static int now() {
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
    static Cancelable delayedTask(@Nullable final String name, final Runnable task, final int delay) {
        return c_scheduler.delayedTask(name, task, delay);
    }

    /**
     * Schedules the periodic execution of the given task with specified period.
     *
     * @param name   A human consumable name for the task. It must be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
     * @param task   the task to execute.
     * @param period the period after execution when the task should be re-executed. Must be a value greater than 0.
     * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
     */
    static Cancelable periodicTask(@Nullable final String name, final Runnable task, final int period) {
        return c_scheduler.periodicTask(name, task, period);
    }

    @TestOnly
    static void reset() {
        c_scheduler.shutdown();
        c_scheduler = createScheduler();
    }

    @OmitSymbol(unless = "zemeckis.use_test_scheduler")
    @TestOnly
    static boolean pumpNext() {
        return testScheduler().pumpNext();
    }

    @OmitSymbol(unless = "zemeckis.use_test_scheduler")
    @TestOnly
    static int pumpAll() {
        return testScheduler().pumpAll();
    }

    private static AbstractScheduler createScheduler() {
        return ZemeckisConfig.useTestScheduler() ? new TestSchedulerImpl() : new ProductionSchedulerImpl();
    }

    @OmitSymbol(unless = "zemeckis.use_test_scheduler")
    private static TestSchedulerImpl testScheduler() {
        if (!(c_scheduler instanceof TestSchedulerImpl)) {
            throw new IllegalStateException("Test scheduler is not enabled");
        }
        return (TestSchedulerImpl) c_scheduler;
    }

    private static final class TestSchedulerImpl extends AbstractScheduler {
        private static final int MAX_PUMPED_TASKS = 10_000;

        private final PriorityQueue<ScheduledTask> _tasks = new PriorityQueue<>();

        private long _now;

        private long _nextSequence;

        @Override
        void shutdown() {
            _tasks.clear();
        }

        @Override
        int now() {
            return (int) _now;
        }

        @Override
        Cancelable doDelayedTask(@Nullable final String name, final Runnable task, final int delay) {
            return schedule(task, delay, 0);
        }

        @Override
        Cancelable doPeriodicTask(@Nullable final String name, final Runnable task, final int period) {
            return schedule(task, period, period);
        }

        private Cancelable schedule(final Runnable task, final int delay, final int period) {
            final var scheduledTask = new ScheduledTask(task, _now + delay, _nextSequence++, period);
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

    private abstract static class AbstractScheduler {
        abstract void shutdown();

        abstract int now();

        final Cancelable delayedTask(@Nullable final String name, final Runnable task, final int delay) {
            if (Zemeckis.shouldCheckApiInvariants()) {
                apiInvariant(
                        () -> delay >= 0,
                        () -> "Zemeckis-0008: Zemeckis.delayedTask(...) named '" + name
                                + "' passed a negative delay. Actual value passed is " + delay);
            }
            return new TaskEntry(name, task, doDelayedTask(name, task, delay));
        }

        abstract Cancelable doDelayedTask(@Nullable String name, Runnable task, int delay);

        final Cancelable periodicTask(@Nullable final String name, final Runnable task, final int period) {
            if (Zemeckis.shouldCheckApiInvariants()) {
                apiInvariant(
                        () -> period > 0,
                        () -> "Zemeckis-0009: Zemeckis.periodicTask(...) named '" + name
                                + "' passed a non-positive period. Actual value passed is " + period);
            }

            return new TaskEntry(name, task, doPeriodicTask(name, task, period));
        }

        abstract Cancelable doPeriodicTask(@Nullable String name, Runnable task, int period);
    }

    private static final class ProductionSchedulerImpl extends AbstractScheduler {
        private static final boolean ENABLE_WORKERS = Zemeckis.useWorkerToScheduleDelayedTasks();
        private static final boolean LOG = Zemeckis.shouldLogWorkerInteractions();
        private static final String SRC = "var timers = {};\n" + "\n"
                + "function cancelTimer(id) {\n"
                + "  self.clearTimeout((timers[id] || {}).timerId || 0);\n"
                + "  delete timers[id];\n"
                + "}\n"
                + "\n"
                + "function createTimer(name, id, delay) {\n"
                + "  timers[id] = {name:name, timerId:self.setTimeout(() => {\n"
                + (!LOG ? "" : "    console.log(\"[Zemeckis-Worker] Delayed Task Tick \" + name + \": \" + id);\n")
                + "    delete timers[id];\n"
                + "    self.postMessage({ type: 'dt', id: id });\n"
                + "  }, delay)};\n"
                + "}\n"
                + "\n"
                + "function cancelPeriodicTimer(id) {\n"
                + "  self.clearInterval((timers[id] || {}).timerId || 0);\n"
                + "  delete timers[id];\n"
                + "}\n"
                + "\n"
                + "function createPeriodicTimer(name, id, period) {\n"
                + "  timers[id] = {name:name, timerId:self.setInterval(() => {\n"
                + (!LOG ? "" : "    console.log( \"[Zemeckis-Worker] Periodic Task Tick \" + name + \": \" + id );\n")
                + "    self.postMessage({ type: 'pt', id: id });\n"
                + "  }, period)};\n"
                + "}\n"
                + "\n"
                + "self.onmessage = m => {\n"
                + (!LOG
                        ? ""
                        : "  console.log(\"[Zemeckis-Worker] Timers Before Action\","
                                + " JSON.parse(JSON.stringify(timers)))\n")
                + "  if (m.data && m.data.action === '+' && m.data.type === 'dt' && m.data.id !== undefined &&"
                + " m.data.delay !== undefined) {\n"
                + (!LOG
                        ? ""
                        : "    console.log(\"[Zemeckis-Worker] Add Delayed Task '\" + m.data.name + \"': \" +"
                                + " m.data.id + \" delay=\" + m.data.delay);\n")
                + "    createTimer(m.data.name, m.data.id, m.data.delay);\n"
                + "  } else if (m.data && m.data.action === '-' && m.data.type === 'dt' && m.data.id !== undefined) {\n"
                + (!LOG
                        ? ""
                        : "    console.log(\"[Zemeckis-Worker] Remove Delayed Task '\" + m.data.name + \"': \" +"
                                + " m.data.id);\n")
                + "    cancelTimer(m.data.id);\n"
                + "  } else if (m.data && m.data.action === '+' && m.data.type === 'pt' && m.data.id !== undefined &&"
                + " m.data.period !== undefined) {\n"
                + (!LOG
                        ? ""
                        : "    console.log(\"[Zemeckis-Worker] Add Periodic Task '\" + m.data.name + \"': \" +"
                                + " m.data.id + \" delay=\" + m.data.period);\n")
                + "    createPeriodicTimer(m.data.name, m.data.id, m.data.period);\n"
                + "  } else if (m.data && m.data.action === '-' && m.data.type === 'pt' && m.data.id !== undefined) {\n"
                + (!LOG
                        ? ""
                        : "    console.log(\"[Zemeckis-Worker] Remove Periodic Task '\" + m.data.name + \"': \" +"
                                + " m.data.id);\n")
                + "    cancelPeriodicTimer(m.data.id);\n"
                + (!LOG
                        ? ""
                        : "  } else {\n" + "    console.log(\"[Zemeckis-Worker] Invalid Task request:\", m.data);\n")
                + "  }\n"
                + (!LOG
                        ? ""
                        : "  console.log(\"[Zemeckis-Worker] Timers After Action\","
                                + " JSON.parse(JSON.stringify(timers)));\n")
                + "};";
        private final long _schedulerStart = System.currentTimeMillis();

        @OmitSymbol(unless = "zemeckis.use_worker_to_schedule_delayed_tasks")
        @Nullable
        private final Worker _worker = ENABLE_WORKERS
                ? new Worker(URL.createObjectURL(new Blob(new BlobPart[] {BlobPart.of(SRC)})), createWorkerOptions())
                : null;

        @OmitSymbol(unless = "zemeckis.use_worker_to_schedule_delayed_tasks")
        @Nullable
        private final Map<Double, Runnable> _workerTasks = ENABLE_WORKERS ? new HashMap<>() : null;

        @OmitSymbol(unless = "zemeckis.use_worker_to_schedule_delayed_tasks")
        private double _nextTimerId = 1;

        {
            if (ENABLE_WORKERS) {
                worker().onmessage = this::onWorkerMessage;
            }
        }

        final long getSchedulerStart() {
            return _schedulerStart;
        }

        @Override
        void shutdown() {
            if (Zemeckis.useWorkerToScheduleDelayedTasks()) {
                worker().terminate();
            }
        }

        @Override
        int now() {
            return (int) (System.currentTimeMillis() - getSchedulerStart());
        }

        @Override
        Cancelable doDelayedTask(@Nullable final String name, final Runnable task, final int delay) {
            if (Zemeckis.useWorkerToScheduleDelayedTasks()) {
                final double id = _nextTimerId++;
                workerTasks().put(id, task);
                final JsPropertyMap<Object> message = msg(name, "+", "dt", id);
                message.set("delay", delay);
                if (LOG) {
                    Console.log("[Zemeckis-Main] Add Delayed Task '" + name + "': " + id);
                }
                worker().postMessage(message);
                return () -> {
                    workerTasks().remove(id);
                    if (LOG) {
                        Console.log("[Zemeckis-Main] Remove Delayed Task '" + name + "': " + id);
                    }
                    worker().postMessage(msg(name, "-", "dt", id));
                };
            } else {
                final int timeoutId = WindowGlobal.setTimeout(task::run, delay);
                return () -> WindowGlobal.clearTimeout(timeoutId);
            }
        }

        @Override
        Cancelable doPeriodicTask(@Nullable final String name, final Runnable task, final int period) {
            if (Zemeckis.useWorkerToScheduleDelayedTasks()) {
                final double id = _nextTimerId++;
                workerTasks().put(id, task);
                final JsPropertyMap<Object> message = msg(name, "+", "pt", id);
                message.set("period", period);
                if (LOG) {
                    Console.log("[Zemeckis-Main] Add Periodic Task '" + name + "': " + id);
                }
                worker().postMessage(message);
                return () -> {
                    workerTasks().remove(id);
                    if (LOG) {
                        Console.log("[Zemeckis-Main] Remove Periodic Task '" + name + "': " + id);
                    }
                    worker().postMessage(msg(name, "-", "pt", id));
                };
            } else {
                final int timeoutId = WindowGlobal.setInterval(task::run, period);
                return () -> WindowGlobal.clearInterval(timeoutId);
            }
        }

        @OmitSymbol(unless = "zemeckis.use_worker_to_schedule_delayed_tasks")
        private void onWorkerMessage(final MessageEvent event) {
            final Any eventData = event.data();
            if (null != eventData) {
                final JsPropertyMap<Object> data = Js.asPropertyMap(eventData);
                final double id = data.getAsAny("id").asDouble();
                final String type = data.getAsAny("type").asString();
                if ("dt".equals(type)) {
                    if (LOG) {
                        Console.log("[Zemeckis-Main] Delayed Task Tick: " + id);
                    }
                    runTaskIfPresent(workerTasks().remove(id));
                } else if ("pt".equals(type)) {
                    if (LOG) {
                        Console.log("[Zemeckis-Main] Periodic Task Tick: " + id);
                    }
                    runTaskIfPresent(workerTasks().get(id));
                }
            }
        }

        @OmitSymbol(unless = "zemeckis.use_worker_to_schedule_delayed_tasks")
        @SuppressWarnings("Varifier")
        private JsPropertyMap<Object> msg(
                @Nullable String name, final String action, final String type, final double id) {
            final JsPropertyMap<Object> msg = JsPropertyMap.of("action", action, "type", type, "id", id);
            if (null != name) {
                msg.set("name", name);
            }
            return msg;
        }

        @OmitSymbol(unless = "zemeckis.use_worker_to_schedule_delayed_tasks")
        private void runTaskIfPresent(@Nullable final Runnable task) {
            if (null != task) {
                task.run();
            }
        }

        @OmitSymbol(unless = "zemeckis.use_worker_to_schedule_delayed_tasks")
        private Worker worker() {
            return Objects.requireNonNull(_worker);
        }

        @OmitSymbol(unless = "zemeckis.use_worker_to_schedule_delayed_tasks")
        private Map<Double, Runnable> workerTasks() {
            return Objects.requireNonNull(_workerTasks);
        }

        @OmitSymbol(unless = "zemeckis.use_worker_to_schedule_delayed_tasks")
        private WorkerOptions createWorkerOptions() {
            return WorkerOptions.of().name("ZemeckisTimer");
        }
    }
}
