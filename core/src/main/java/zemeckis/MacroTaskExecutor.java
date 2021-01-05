package zemeckis;

/**
 * Run tasks in next MacroTask.
 */
final class MacroTaskExecutor
  extends RoundBasedExecutor
{
  @Override
  void scheduleForActivation()
  {
    Scheduler.delayedTask( this::activate, 0 );
  }
}
