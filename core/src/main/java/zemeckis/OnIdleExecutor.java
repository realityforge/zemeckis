package zemeckis;

import akasha.WindowGlobal;

/**
 * Run tasks in next Idle callbacks.
 */
final class OnIdleExecutor
  extends DeadlineBasedExecutor
{
  @Override
  void scheduleForActivation()
  {
    WindowGlobal.requestIdleCallback( deadline -> context().activate( () -> executeTasks( deadline::timeRemaining ) ) );
  }
}
