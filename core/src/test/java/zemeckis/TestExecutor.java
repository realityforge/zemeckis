package zemeckis;

final class TestExecutor
  extends RoundBasedExecutor
{
  private int _scheduleCount;

  TestExecutor()
  {
  }

  TestExecutor( final int maxRounds )
  {
    super( maxRounds );
  }

  @Override
  void scheduleForActivation()
  {
    _scheduleCount++;
  }

  int getScheduleCount()
  {
    return _scheduleCount;
  }
}
