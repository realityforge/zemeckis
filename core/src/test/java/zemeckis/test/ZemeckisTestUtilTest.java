package zemeckis.test;

import static org.testng.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;
import zemeckis.AbstractTest;
import zemeckis.Zemeckis;
import zemeckis.ZemeckisTestUtil;

public class ZemeckisTestUtilTest extends AbstractTest {
    @Test
    public void areNamesEnabled() {
        ZemeckisTestUtil.disableNames();
        assertFalse(Zemeckis.areNamesEnabled());
        ZemeckisTestUtil.enableNames();
        assertTrue(Zemeckis.areNamesEnabled());
    }

    @Test
    public void purgeTasksWhenRunawayDetected() {
        ZemeckisTestUtil.noPurgeTasksWhenRunawayDetected();
        assertFalse(Zemeckis.purgeTasksWhenRunawayDetected());
        ZemeckisTestUtil.purgeTasksWhenRunawayDetected();
        assertTrue(Zemeckis.purgeTasksWhenRunawayDetected());
    }

    @Test
    public void areUncaughtErrorHandlersEnabled() {
        ZemeckisTestUtil.disableUncaughtErrorHandlers();
        assertFalse(Zemeckis.areUncaughtErrorHandlersEnabled());
        ZemeckisTestUtil.enableUncaughtErrorHandlers();
        assertTrue(Zemeckis.areUncaughtErrorHandlersEnabled());
    }

    @Test
    public void resetConfigCancelsDelayedTasks() {
        final var executions = new AtomicInteger();
        Zemeckis.delayedTask(executions::incrementAndGet, 200);

        ZemeckisTestUtil.resetConfig(false);

        assertFalse(ZemeckisTestUtil.pumpNext());
        assertEquals(executions.get(), 0);
    }

    @Test
    public void resetConfigCancelsPeriodicTasks() {
        final var executions = new AtomicInteger();
        Zemeckis.periodicTask(executions::incrementAndGet, 20);
        assertTrue(ZemeckisTestUtil.pumpNext());
        assertEquals(executions.get(), 1);

        ZemeckisTestUtil.resetConfig(false);

        assertFalse(ZemeckisTestUtil.pumpNext());
        assertEquals(executions.get(), 1);
    }

    @Test
    public void pumpAllExecutesScheduledTasks() {
        final var trace = new StringBuilder();
        Zemeckis.delayedTask(() -> trace.append("B"), 20);
        Zemeckis.delayedTask(() -> trace.append("A"), 10);

        assertEquals(ZemeckisTestUtil.pumpAll(), 2);
        assertEquals(trace.toString(), "AB");
    }
}
