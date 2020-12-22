package zemeckis;

import elemental2.dom.DomGlobal;

/**
 * Run tasks in next Idle callbacks.
 */
final class OnIdleExecutor
  extends DeadlineBasedExecutor
{
  @Override
  final void scheduleForActivation()
  {
    DomGlobal.requestIdleCallback( deadline -> context().activate( () -> executeTasks( () -> (int) deadline.timeRemaining() ) ) );
  }
}
