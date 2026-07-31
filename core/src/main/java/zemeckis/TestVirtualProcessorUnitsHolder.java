package zemeckis;

import static org.realityforge.braincheck.Guards.*;

import java.util.Objects;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

/**
 * Browser-independent VPU holder used by the J2CL test-scheduler target.
 */
final class VirtualProcessorUnitsHolder {
    private VirtualProcessorUnitsHolder() {}

    static VirtualProcessorUnit macroTaskVpu() {
        return MacroTaskVPU.VPU;
    }

    static VirtualProcessorUnit microTaskVpu() {
        return MicroTaskVPU.VPU;
    }

    static VirtualProcessorUnit animationFrameVpu() {
        return AnimationFrameVPU.VPU;
    }

    static VirtualProcessorUnit afterFrameVpu() {
        return AfterFrameVPU.VPU;
    }

    static VirtualProcessorUnit onIdleVpu() {
        return OnIdleVPU.VPU;
    }

    @Nullable
    static VirtualProcessorUnit currentVpu() {
        return CurrentVPU.current();
    }

    static synchronized boolean isVpuActivated() {
        return CurrentVPU.isVpuActivated();
    }

    static synchronized void activate(
            final VirtualProcessorUnit processorUnit, final VirtualProcessorUnit.ActivationFn activationFn) {
        CurrentVPU.activate(processorUnit);
        try {
            activationFn.invoke();
        } finally {
            CurrentVPU.deactivate(processorUnit);
        }
    }

    @TestOnly
    static synchronized void reset() {
        CurrentVPU.c_current = null;
        macroTaskVpu().getExecutor().reset();
        microTaskVpu().getExecutor().reset();
        animationFrameVpu().getExecutor().reset();
        afterFrameVpu().getExecutor().reset();
        onIdleVpu().getExecutor().reset();
    }

    private static VirtualProcessorUnit createVpu(final String name) {
        return new VirtualProcessorUnit(Zemeckis.areNamesEnabled() ? name : null, new TestTaskExecutor());
    }

    private static final class MacroTaskVPU {
        private MacroTaskVPU() {}

        private static final VirtualProcessorUnit VPU = createVpu("Macro");
    }

    private static final class MicroTaskVPU {
        private MicroTaskVPU() {}

        private static final VirtualProcessorUnit VPU = createVpu("Micro");
    }

    private static final class AnimationFrameVPU {
        private AnimationFrameVPU() {}

        private static final VirtualProcessorUnit VPU = createVpu("AnimationFrame");
    }

    private static final class AfterFrameVPU {
        private AfterFrameVPU() {}

        private static final VirtualProcessorUnit VPU = createVpu("AfterFrame");
    }

    private static final class OnIdleVPU {
        private OnIdleVPU() {}

        private static final VirtualProcessorUnit VPU = createVpu("OnIdle");
    }

    @VisibleForTesting
    static final class CurrentVPU {
        @Nullable
        private static VirtualProcessorUnit c_current = null;

        private CurrentVPU() {}

        @Nullable
        private static VirtualProcessorUnit current() {
            return c_current;
        }

        private static boolean isVpuActivated() {
            return null != c_current;
        }

        @VisibleForTesting
        static void activate(final VirtualProcessorUnit processorUnit) {
            Objects.requireNonNull(processorUnit);
            if (Zemeckis.shouldCheckInvariants()) {
                invariant(
                        () -> null == c_current,
                        () -> "Zemeckis-0004: Attempting to activate VirtualProcessorUnit named '" + processorUnit
                                + "' but an existing VirtualProcessorUnit named '" + c_current + "' is activated");
            }
            c_current = processorUnit;
        }

        @VisibleForTesting
        static void deactivate(final VirtualProcessorUnit processorUnit) {
            Objects.requireNonNull(processorUnit);
            if (Zemeckis.shouldCheckInvariants()) {
                invariant(
                        () -> processorUnit == c_current,
                        () -> "Zemeckis-0005: Attempting to deactivate VirtualProcessorUnit named '" + processorUnit
                                + "' but no VirtualProcessorUnit is activated");
            }
            c_current = null;
        }
    }
}
