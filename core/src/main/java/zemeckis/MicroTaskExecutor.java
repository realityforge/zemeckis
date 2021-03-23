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
    Promise.resolve( (Object) null ).thenAccept( v -> activate() );
  }
}
