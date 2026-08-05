package zemeckis;

import grim.annotations.OmitType;

/**
 * Run tasks in next Idle callbacks.
 */
@OmitType(when = "zemeckis.use_test_scheduler")
final class OnIdleExecutor extends DeadlineBasedExecutor {
    @Override
    void scheduleForActivation() {
        JsRuntime.requestIdleCallback(deadline -> context().activate(() -> executeTasks(deadline::timeRemaining)));
    }
}
