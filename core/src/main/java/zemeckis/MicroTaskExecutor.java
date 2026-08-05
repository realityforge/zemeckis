package zemeckis;

import grim.annotations.OmitType;

/**
 * Run tasks in next MicroTask.
 */
@OmitType(when = "zemeckis.use_test_scheduler")
final class MicroTaskExecutor extends RoundBasedExecutor {
    @Override
    void scheduleForActivation() {
        // In the future we should use akasha.WindowGlobal.queueMicrotask(  );
        JsRuntime.Promise.resolve(null).thenAccept(v -> activate());
    }
}
