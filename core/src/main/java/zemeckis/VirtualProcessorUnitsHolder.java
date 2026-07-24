package zemeckis;

import static org.realityforge.braincheck.Guards.*;

import java.util.Objects;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

/**
 * A container that holds separate inner classes for each VPU that Zemeckis supports.
 * The whole purpose of this dance is to avoid the creation of &lt;clinit&gt; sections
 * to improve code optimizers chances of dead code removal.
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

    /**
     * Return the current VirtualProcessorUnit.
     *
     * @return the VirtualProcessorUnit.
     */
    @Nullable
    static VirtualProcessorUnit currentVpu() {
        return CurrentVPU.current();
    }

    /**
     * Return true if there is a current VPU activated.
     *
     * @return true if there is a current VPU activated.
     */
    static synchronized boolean isVpuActivated() {
        return CurrentVPU.isVpuActivated();
    }

    /**
     * Activate the VirtualProcessorUnit.
     * This involves setting current unit, invoking the activation function and clearing the current unit.
     * It is an error to invoke this method if there is already a current unit.
     *
     * @param processorUnit the VirtualProcessorUnit.
     * @param activationFn  the activation function.
     * @see VirtualProcessorUnit.Context#activate(VirtualProcessorUnit.ActivationFn)
     */
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

    private static final class MacroTaskVPU {
        private MacroTaskVPU() {}

        private static final VirtualProcessorUnit VPU =
                new VirtualProcessorUnit(Zemeckis.areNamesEnabled() ? "Macro" : null, new MacroTaskExecutor());
    }

    private static final class MicroTaskVPU {
        private MicroTaskVPU() {}

        private static final VirtualProcessorUnit VPU = new VirtualProcessorUnit(
                Zemeckis.areNamesEnabled() ? "Micro" : null,
                ZemeckisConfig.useTestScheduler() ? new MacroTaskExecutor() : new MicroTaskExecutor());
    }

    private static final class AnimationFrameVPU {
        private AnimationFrameVPU() {}

        private static final VirtualProcessorUnit VPU = new VirtualProcessorUnit(
                Zemeckis.areNamesEnabled() ? "AnimationFrame" : null,
                ZemeckisConfig.useTestScheduler() ? new MacroTaskExecutor() : new AnimationFrameExecutor());
    }

    private static final class AfterFrameVPU {
        private AfterFrameVPU() {}

        private static final VirtualProcessorUnit VPU = new VirtualProcessorUnit(
                Zemeckis.areNamesEnabled() ? "AfterFrame" : null,
                ZemeckisConfig.useTestScheduler() ? new MacroTaskExecutor() : new AfterFrameExecutor());
    }

    private static final class OnIdleVPU {
        private OnIdleVPU() {}

        private static final VirtualProcessorUnit VPU = new VirtualProcessorUnit(
                Zemeckis.areNamesEnabled() ? "OnIdle" : null,
                ZemeckisConfig.useTestScheduler() ? new MacroTaskExecutor() : new OnIdleExecutor());
    }

    /**
     * A utility class that contains reference to singleton VPU that is currently active.
     */
    @VisibleForTesting
    static final class CurrentVPU {
        @Nullable
        private static VirtualProcessorUnit c_current = null;

        private CurrentVPU() {}

        /**
         * Return the current VirtualProcessorUnit.
         *
         * @return the VirtualProcessorUnit.
         */
        @Nullable
        private static VirtualProcessorUnit current() {
            return c_current;
        }

        /**
         * Return true if there is a current VPU activated.
         *
         * @return true if there is a current VPU activated.
         */
        private static boolean isVpuActivated() {
            return null != c_current;
        }

        /**
         * Set the current VirtualProcessorUnit.
         * The {@link VirtualProcessorUnit} should call this during an activation.
         *
         * @param processorUnit the VirtualProcessorUnit.
         */
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

        /**
         * Clear the current VirtualProcessorUnit.
         * The {@link VirtualProcessorUnit} should call this after an activation is completed.
         *
         * @param processorUnit the VirtualProcessorUnit.
         */
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
