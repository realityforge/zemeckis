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
    TemporalScheduler.delayedTask( Zemeckis.areNamesEnabled() ? "MacroTaskExecutor" : null, this::activate, 0 );
  }
}
