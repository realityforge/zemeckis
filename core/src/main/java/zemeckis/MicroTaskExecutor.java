package zemeckis;

import elemental2.promise.Promise;

/**
 * Run tasks in next MicroTask.
 */
final class MicroTaskExecutor
  extends RoundBasedExecutor
{
  @Override
  void scheduleForActivation()
  {
    Promise.resolve( (Object) null ).then( v -> {
      activate();
      return null;
    } );
  }
}
