package zemeckis;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.testng.annotations.Test;

public final class ZemeckisTest extends AbstractTest {
    @Test
    public void accessors() {
        assertEquals(Zemeckis.macroTaskVpu(), VirtualProcessorUnitsHolder.macroTaskVpu());
        assertEquals(Zemeckis.microTaskVpu(), VirtualProcessorUnitsHolder.microTaskVpu());
        assertEquals(Zemeckis.animationFrameVpu(), VirtualProcessorUnitsHolder.animationFrameVpu());
        assertEquals(Zemeckis.animationFrameVpu(), VirtualProcessorUnitsHolder.animationFrameVpu());
        assertEquals(Zemeckis.onIdleVpu(), VirtualProcessorUnitsHolder.onIdleVpu());
        Zemeckis.now();
    }

    @Test
    public void delayedTask() {
        final var trace = new StringBuilder();
        Zemeckis.delayedTask(
                () -> {
                    assertEquals(Zemeckis.now(), 20);
                    assertTrue(Zemeckis.isVpuActivated());
                    assertEquals(Zemeckis.currentVpu(), Zemeckis.macroTaskVpu());
                    trace.append("A");
                },
                20);

        Zemeckis.delayedTask(
                () -> {
                    assertEquals(Zemeckis.now(), 40);
                    assertTrue(Zemeckis.isVpuActivated());
                    assertEquals(Zemeckis.currentVpu(), Zemeckis.macroTaskVpu());
                    trace.append("B");
                },
                40);
        assertInvariantFailure(
                () -> Zemeckis.delayedTask(() -> trace.append("X"), -1),
                "Zemeckis-0008: Zemeckis.delayedTask(...) named 'DelayedTask@2' passed a negative delay. Actual value"
                        + " passed is -1");

        assertEquals(ZemeckisTestUtil.pumpAll(), 2);
        assertEquals(trace.toString(), "AB");
    }

    @Test
    public void delayedTask_canceled() {
        final var trace = new StringBuilder();
        final Cancelable token = Zemeckis.delayedTask(() -> trace.append("X"), 20);
        token.cancel();
        assertFalse(ZemeckisTestUtil.pumpNext());
        assertEquals(trace.toString(), "");
    }

    @Test
    public void periodicTask() {
        final int count = 2;
        final var current = new AtomicInteger();
        final AtomicReference<Cancelable> task = new AtomicReference<>();
        final Cancelable schedule = Zemeckis.periodicTask(
                () -> {
                    assertTrue(Zemeckis.isVpuActivated());
                    assertEquals(Zemeckis.currentVpu(), Zemeckis.macroTaskVpu());
                    if (current.incrementAndGet() >= count) {
                        Objects.requireNonNull(task.get()).cancel();
                    }
                },
                20);
        assertEquals(schedule.toString(), "PeriodicTask@0");
        task.set(schedule);

        assertInvariantFailure(
                () -> Zemeckis.periodicTask("P2", () -> fail("Scheduled task with an invalid period executed"), -1),
                "Zemeckis-0009: Zemeckis.periodicTask(...) named 'P2' passed a non-positive period. Actual value"
                        + " passed is -1");

        assertEquals(ZemeckisTestUtil.pumpAll(), count);
        assertEquals(current.get(), count);
    }

    @Test
    public void vpu() {
        assertFalse(Zemeckis.isVpuActivated());
        assertNull(Zemeckis.currentVpu());

        final String name1 = randomString();
        final String name2 = randomString();
        final String name3 = randomString();
        final String name4 = randomString();
        final String name5 = randomString();

        final var trace = new StringBuilder();
        final Cancelable cancelable1 = Zemeckis.macroTask(() -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.macroTaskVpu());
            trace.append("A");
        });
        final Cancelable cancelable2 = Zemeckis.macroTask(name1, () -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.macroTaskVpu());
            trace.append("B");
        });
        final Cancelable cancelable3 = Zemeckis.microTask(() -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.microTaskVpu());
            trace.append("C");
        });
        final Cancelable cancelable4 = Zemeckis.microTask(name2, () -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.microTaskVpu());
            trace.append("D");
        });
        final Cancelable cancelable5 = Zemeckis.animationFrame(() -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.animationFrameVpu());
            trace.append("E");
        });
        final Cancelable cancelable6 = Zemeckis.animationFrame(name3, () -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.animationFrameVpu());
            trace.append("F");
        });
        final Cancelable cancelable7 = Zemeckis.afterFrame(() -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.afterFrameVpu());
            trace.append("G");
        });
        final Cancelable cancelable8 = Zemeckis.afterFrame(name4, () -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.afterFrameVpu());
            trace.append("H");
        });
        final Cancelable cancelable9 = Zemeckis.onIdle(() -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.onIdleVpu());
            trace.append("I");
        });
        final Cancelable cancelable10 = Zemeckis.onIdle(name5, () -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.onIdleVpu());
            trace.append("J");
        });

        assertFalse(Zemeckis.isVpuActivated());
        assertNull(Zemeckis.currentVpu());

        assertEquals(ZemeckisTestUtil.pumpAll(), 5);
        assertEquals(trace.toString(), "ABCDEFGHIJ");
        assertEquals(cancelable1.toString(), "MacroTask@0");
        assertEquals(cancelable2.toString(), name1);
        assertEquals(cancelable3.toString(), "MicroTask@1");
        assertEquals(cancelable4.toString(), name2);
        assertEquals(cancelable5.toString(), "AnimationFrameTask@2");
        assertEquals(cancelable6.toString(), name3);
        assertEquals(cancelable7.toString(), "AfterFrameTask@3");
        assertEquals(cancelable8.toString(), name4);
        assertEquals(cancelable9.toString(), "OnIdleTask@4");
        assertEquals(cancelable10.toString(), name5);
    }

    @Test
    public void canceledTaskNoRun() {
        assertFalse(Zemeckis.isVpuActivated());
        assertNull(Zemeckis.currentVpu());

        final var trace = new StringBuilder();
        Zemeckis.macroTask(() -> {
                    assertTrue(Zemeckis.isVpuActivated());
                    assertEquals(Zemeckis.currentVpu(), Zemeckis.macroTaskVpu());
                    trace.append("A");
                })
                .cancel();
        Zemeckis.microTask(() -> {
                    assertTrue(Zemeckis.isVpuActivated());
                    assertEquals(Zemeckis.currentVpu(), Zemeckis.microTaskVpu());
                    trace.append("B");
                })
                .cancel();
        Zemeckis.animationFrame(() -> {
                    assertTrue(Zemeckis.isVpuActivated());
                    assertEquals(Zemeckis.currentVpu(), Zemeckis.animationFrameVpu());
                    trace.append("C");
                })
                .cancel();
        Zemeckis.afterFrame(() -> {
                    assertTrue(Zemeckis.isVpuActivated());
                    assertEquals(Zemeckis.currentVpu(), Zemeckis.afterFrameVpu());
                    trace.append("D");
                })
                .cancel();
        Zemeckis.onIdle(() -> {
                    assertTrue(Zemeckis.isVpuActivated());
                    assertEquals(Zemeckis.currentVpu(), Zemeckis.onIdleVpu());
                    trace.append("E");
                })
                .cancel();

        assertFalse(Zemeckis.isVpuActivated());
        assertNull(Zemeckis.currentVpu());

        assertEquals(ZemeckisTestUtil.pumpAll(), 5);
        assertEquals(trace.toString(), "");
    }

    @Test
    public void becomeMacroTask() {
        final List<String> trace = new ArrayList<>();
        assertFalse(Zemeckis.isVpuActivated());
        assertNull(Zemeckis.currentVpu());

        ((AbstractExecutor) Zemeckis.macroTaskVpu().getExecutor())
                .getTaskQueue()
                .add(new TaskEntry("A", () -> trace.add("A"), null));

        Zemeckis.becomeMacroTask(randomString(), () -> {
            assertTrue(Zemeckis.isVpuActivated());
            assertEquals(Zemeckis.currentVpu(), Zemeckis.macroTaskVpu());
            trace.add("*");
            Zemeckis.macroTask(() -> trace.add("B"));
            assertInvariantFailure(
                    () -> Zemeckis.becomeMacroTask("MyCTask", () -> trace.add("C")),
                    "Zemeckis-0012: Zemeckis.becomeMacroTask(...) invoked for the task named 'MyCTask' but the"
                            + " VirtualProcessorUnit named 'Macro' is already active");
        });

        assertFalse(Zemeckis.isVpuActivated());
        assertNull(Zemeckis.currentVpu());

        assertEquals(String.join("", trace), "*AB");
    }
}
