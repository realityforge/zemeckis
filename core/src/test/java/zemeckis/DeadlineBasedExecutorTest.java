package zemeckis;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class DeadlineBasedExecutorTest
  extends AbstractTest
{
  @Test
  public void basicOperation()
  {
    final TestDeadlineBasedExecutor executor = new TestDeadlineBasedExecutor();
    executor.init( VirtualProcessorUnit.ActivationFn::invoke );
    final NoopTask task1 = new NoopTask();
    final NoopTask task2 = new NoopTask();
    final NoopTask task3 = new NoopTask();
    executor.queue( task1 );
    executor.queue( task2 );
    executor.queue( task3 );

    assertEquals( executor.getScheduleForActivationCount(), 1 );

    executor.activate();

    assertEquals( executor.getScheduleForActivationCount(), 2 );
    assertEquals( executor.getTaskQueue().size(), 3 );
    assertEquals( task1.getRunCount(), 0 );
    assertEquals( task2.getRunCount(), 0 );
    assertEquals( task3.getRunCount(), 0 );

    executor.executeTasks( () -> 0 );

    assertEquals( executor.getScheduleForActivationCount(), 3 );
    assertEquals( executor.getTaskQueue().size(), 3 );
    assertEquals( task1.getRunCount(), 0 );
    assertEquals( task2.getRunCount(), 0 );
    assertEquals( task3.getRunCount(), 0 );

    final List<Integer> timeRemaining = new ArrayList<>();
    timeRemaining.add( 4 );
    timeRemaining.add( 1 );
    executor.executeTasks( () -> timeRemaining.isEmpty() ? 0 : timeRemaining.remove( 0 ) );

    assertEquals( executor.getScheduleForActivationCount(), 4 );
    assertEquals( executor.getTaskQueue().size(), 1 );
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 0 );

    executor.executeTasks( () -> 1 );

    assertEquals( executor.getScheduleForActivationCount(), 4 );
    assertEquals( executor.getTaskQueue().size(), 0 );
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 1 );
  }

  private static final class TestDeadlineBasedExecutor
    extends DeadlineBasedExecutor
  {
    private int _scheduleForActivationCount;

    @Override
    void scheduleForActivation()
    {
      _scheduleForActivationCount++;
    }

    int getScheduleForActivationCount()
    {
      return _scheduleForActivationCount;
    }
  }
}
