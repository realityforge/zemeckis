package zemeckis.test;

import static org.testng.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    public void resetConfigCancelsDelayedTasks() throws InterruptedException {
        final var executions = new CountDownLatch(1);
        Zemeckis.delayedTask(executions::countDown, 200);

        ZemeckisTestUtil.resetConfig(false);

        assertFalse(executions.await(500, TimeUnit.MILLISECONDS));
    }

    @Test
    public void resetConfigCancelsPeriodicTasks() throws InterruptedException {
        final var executions = new AtomicInteger();
        final var initialExecution = new CountDownLatch(1);
        Zemeckis.periodicTask(
                () -> {
                    executions.incrementAndGet();
                    initialExecution.countDown();
                },
                20);
        assertTrue(initialExecution.await(1, TimeUnit.SECONDS));

        ZemeckisTestUtil.resetConfig(false);
        final int executionCount = executions.get();

        Thread.sleep(100);
        assertEquals(executions.get(), executionCount);
    }

    @Test
    public void resetConfigWaitsForInFlightTasks() throws InterruptedException, ExecutionException {
        final var taskStarted = new CountDownLatch(1);
        final var taskInterrupted = new CountDownLatch(1);
        final var releaseTask = new CountDownLatch(1);
        final var taskCompleted = new CountDownLatch(1);
        Zemeckis.delayedTask(
                () -> {
                    taskStarted.countDown();
                    awaitUninterruptibly(releaseTask, taskInterrupted);
                    taskCompleted.countDown();
                },
                0);
        assertTrue(taskStarted.await(1, TimeUnit.SECONDS));

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            final var reset = executor.submit(() -> ZemeckisTestUtil.resetConfig(false));
            assertTrue(taskInterrupted.await(1, TimeUnit.SECONDS));
            assertFalse(reset.isDone());

            releaseTask.countDown();
            reset.get();
        } finally {
            releaseTask.countDown();
            executor.shutdownNow();
        }
        assertEquals(taskCompleted.getCount(), 0);
    }

    private static void awaitUninterruptibly(final CountDownLatch latch, final CountDownLatch interruptedLatch) {
        boolean interrupted = false;
        while (true) {
            try {
                latch.await();
                break;
            } catch (final InterruptedException ignored) {
                interrupted = true;
                interruptedLatch.countDown();
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
