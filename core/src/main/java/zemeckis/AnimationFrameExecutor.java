package zemeckis;

import akasha.Global;

/**
 * Run tasks in next AnimationFrame.
 */
final class AnimationFrameExecutor
  extends RoundBasedExecutor
{
  @Override
  void scheduleForActivation()
  {
    Global.requestAnimationFrame( v -> activate() );
  }
}
