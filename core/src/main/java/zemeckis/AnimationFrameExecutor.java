package zemeckis;

import akasha.WindowGlobal;

/**
 * Run tasks in next AnimationFrame.
 */
final class AnimationFrameExecutor
  extends RoundBasedExecutor
{
  @Override
  void scheduleForActivation()
  {
    WindowGlobal.requestAnimationFrame( v -> activate() );
  }
}
