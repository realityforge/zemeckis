package zemeckis;

import grim.annotations.OmitType;

/**
 * Run tasks in next AnimationFrame.
 */
@OmitType(when = "zemeckis.use_test_scheduler")
final class AnimationFrameExecutor extends RoundBasedExecutor {
    @Override
    void scheduleForActivation() {
        JsRuntime.requestAnimationFrame(v -> activate());
    }
}
