package zemeckis;

import static org.testng.Assert.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.testng.annotations.Test;

public final class TemporalSchedulerTest extends AbstractTest {
    @Test
    public void basicOperation_delayedTask() {
        final var trace = new StringBuilder();
        TemporalScheduler.delayedTask(
                randomString(),
                () -> {
                    assertEquals(TemporalScheduler.now(), 20);
                    trace.append("A");
                },
                20);

        TemporalScheduler.delayedTask(
                randomString(),
                () -> {
                    assertEquals(TemporalScheduler.now(), 40);
                    trace.append("B");
                },
                40);
        assertInvariantFailure(
                () -> TemporalScheduler.delayedTask("D33", () -> trace.append("X"), -1),
                "Zemeckis-0008: Zemeckis.delayedTask(...) named 'D33' passed a negative delay. Actual value passed is"
                        + " -1");

        assertEquals(TemporalScheduler.pumpAll(), 2);
        assertEquals(trace.toString(), "AB");
        assertEquals(TemporalScheduler.now(), 40);
    }

    @Test
    public void delayedTask_canceled() {
        final var trace = new StringBuilder();
        final String name = randomString();
        final Cancelable token = TemporalScheduler.delayedTask(name, () -> trace.append("X"), 20);
        assertEquals(token.toString(), name);
        token.cancel();
        assertFalse(TemporalScheduler.pumpNext());
        assertEquals(trace.toString(), "");
        assertEquals(TemporalScheduler.now(), 0);
    }

    @Test
    public void basicOperation_doScheduleAtFixedRate() {
        final int count = 2;
        final var current = new AtomicInteger();
        final AtomicReference<Cancelable> task = new AtomicReference<>();
        final String name = randomString();
        final Cancelable schedule = TemporalScheduler.periodicTask(
                name,
                () -> {
                    if (current.incrementAndGet() >= count) {
                        Objects.requireNonNull(task.get()).cancel();
                    }
                },
                20);
        task.set(schedule);
        assertEquals(schedule.toString(), name);

        assertInvariantFailure(
                () -> TemporalScheduler.periodicTask(
                        "P1", () -> fail("Scheduled task with an invalid period executed"), -1),
                "Zemeckis-0009: Zemeckis.periodicTask(...) named 'P1' passed a non-positive period. Actual value"
                        + " passed is -1");

        assertEquals(TemporalScheduler.pumpAll(), count);
        assertEquals(current.get(), count);
        assertEquals(TemporalScheduler.now(), 40);
    }

    @Test
    public void tasksWithTheSameDueTimeExecuteInInsertionOrder() {
        final var trace = new StringBuilder();
        TemporalScheduler.delayedTask(randomString(), () -> trace.append("A"), 20);
        TemporalScheduler.delayedTask(randomString(), () -> trace.append("B"), 20);
        TemporalScheduler.delayedTask(randomString(), () -> trace.append("C"), 20);

        assertEquals(TemporalScheduler.pumpAll(), 3);
        assertEquals(trace.toString(), "ABC");
    }

    @Test
    public void pumpAllDetectsRunawayPeriodicTask() {
        TemporalScheduler.periodicTask(randomString(), () -> {}, 20);

        final IllegalStateException exception = expectThrows(IllegalStateException.class, TemporalScheduler::pumpAll);
        assertEquals(exception.getMessage(), "Unable to pump all tasks as more than 10000 tasks were executed");
    }
}
