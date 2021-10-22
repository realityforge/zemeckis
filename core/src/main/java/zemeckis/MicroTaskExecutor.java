package zemeckis;

import akasha.promise.Promise;

/**
 * Run tasks in next MicroTask.
 */
final class MicroTaskExecutor
  extends RoundBasedExecutor
{
  @Override
  void scheduleForActivation()
  {
    // In the future we should use akasha.WindowGlobal.queueMicrotask(  );
    Promise.resolve( (Object) null ).thenAccept( v -> activate() );
  }
}
