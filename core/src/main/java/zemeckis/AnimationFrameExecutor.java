package zemeckis;

import elemental2.dom.DomGlobal;

/**
 * Run tasks in next AnimationFrame.
 */
final class AnimationFrameExecutor
  extends RoundBasedExecutor
{
  @Override
  void scheduleForActivation()
  {
    DomGlobal.requestAnimationFrame( v -> activate() );
  }
}
