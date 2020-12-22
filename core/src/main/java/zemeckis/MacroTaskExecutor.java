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
    Scheduler.schedule( this::activate, 0 );
  }
}
