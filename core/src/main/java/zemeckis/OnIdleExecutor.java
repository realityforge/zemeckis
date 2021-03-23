package zemeckis;

import akasha.Global;

/**
 * Run tasks in next Idle callbacks.
 */
final class OnIdleExecutor
  extends DeadlineBasedExecutor
{
  @Override
  void scheduleForActivation()
  {
    Global.requestIdleCallback( deadline -> context().activate( () -> executeTasks( deadline::timeRemaining ) ) );
  }
}
