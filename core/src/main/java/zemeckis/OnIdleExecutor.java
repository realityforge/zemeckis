package zemeckis;

import akasha.WindowGlobal;
import grim.annotations.OmitType;

/**
 * Run tasks in next Idle callbacks.
 */
@OmitType(when = "zemeckis.use_test_scheduler")
final class OnIdleExecutor extends DeadlineBasedExecutor {
    @Override
    void scheduleForActivation() {
        WindowGlobal.requestIdleCallback(deadline -> context().activate(() -> executeTasks(deadline::timeRemaining)));
    }
}
