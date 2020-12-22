package zemeckis;

final class NoopTask
  implements Runnable
{
  private int _runCount;

  @Override
  public void run()
  {
    _runCount++;
  }

  int getRunCount()
  {
    return _runCount;
  }
}
