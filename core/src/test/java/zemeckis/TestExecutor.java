package zemeckis;

final class TestExecutor
  extends AbstractExecutor
{
  private int _activateCount;
  private int _scheduleCount;

  @Override
  public void activate()
  {
    _activateCount++;
  }

  @Override
  void scheduleForActivation()
  {
    _scheduleCount++;
  }

  int getActivateCount()
  {
    return _activateCount;
  }

  int getScheduleCount()
  {
    return _scheduleCount;
  }
}
