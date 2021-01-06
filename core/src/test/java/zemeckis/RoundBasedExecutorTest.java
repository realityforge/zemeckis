package zemeckis;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.realityforge.braincheck.BrainCheckTestUtil;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class RoundBasedExecutorTest
  extends AbstractTest
{
  @Test
  public void construct()
  {
    final RoundBasedExecutor executor = new TestExecutor( 2 );

    assertEquals( executor.getMaxRounds(), 2 );
    assertEquals( executor.getCurrentRound(), 0 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 0 );
    assertFalse( executor.areTasksExecuting() );
  }

  @Test
  public void runNextTask()
  {
    final RoundBasedExecutor executor = new TestExecutor( 2 );

    final NoopTask task1 = new NoopTask();
    final NoopTask task2 = new NoopTask();
    final NoopTask task3 = new NoopTask();
    executor.queue( randomString(), task1 );
    executor.queue( randomString(), task2 );
    executor.queue( randomString(), task3 );

    assertEquals( executor.getMaxRounds(), 2 );

    assertFalse( executor.areTasksExecuting() );
    assertEquals( executor.getCurrentRound(), 0 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 0 );
    assertEquals( executor.getQueueSize(), 3 );

    // task executions
    assertEquals( task1.getRunCount(), 0 );
    assertEquals( task2.getRunCount(), 0 );
    assertEquals( task3.getRunCount(), 0 );

    assertTrue( executor.runNextTask() );

    assertTrue( executor.areTasksExecuting() );
    assertEquals( executor.getCurrentRound(), 1 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 2 );
    assertEquals( executor.getQueueSize(), 2 );

    // task executions
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 0 );
    assertEquals( task3.getRunCount(), 0 );

    assertTrue( executor.runNextTask() );

    assertTrue( executor.areTasksExecuting() );
    assertEquals( executor.getCurrentRound(), 1 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 1 );
    assertEquals( executor.getQueueSize(), 1 );

    // task executions
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 0 );

    // Now we schedule some tasks again to push execution into round 2
    executor.queue( randomString(), task1 );
    executor.queue( randomString(), task2 );

    assertEquals( executor.getQueueSize(), 3 );

    assertTrue( executor.runNextTask() );

    assertTrue( executor.areTasksExecuting() );
    assertEquals( executor.getCurrentRound(), 1 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 0 );
    assertEquals( executor.getQueueSize(), 2 );

    // task executions
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 1 );

    assertTrue( executor.runNextTask() );

    assertTrue( executor.areTasksExecuting() );
    assertEquals( executor.getCurrentRound(), 2 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 1 );
    assertEquals( executor.getQueueSize(), 1 );

    // task executions
    assertEquals( task1.getRunCount(), 2 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 1 );

    assertTrue( executor.runNextTask() );

    assertTrue( executor.areTasksExecuting() );
    assertEquals( executor.getCurrentRound(), 2 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 0 );
    assertEquals( executor.getQueueSize(), 0 );

    // task executions
    assertEquals( task1.getRunCount(), 2 );
    assertEquals( task2.getRunCount(), 2 );
    assertEquals( task3.getRunCount(), 1 );

    assertFalse( executor.runNextTask() );

    assertFalse( executor.areTasksExecuting() );
  }

  @Test
  public void activate()
  {
    final RoundBasedExecutor executor = new TestExecutor( 2 );
    executor.init( VirtualProcessorUnit.ActivationFn::invoke );

    final NoopTask task1 = new NoopTask();
    final NoopTask task2 = new NoopTask();
    final NoopTask task3 = new NoopTask();
    executor.queue( randomString(), task1 );
    executor.queue( randomString(), task2 );
    executor.queue( randomString(), task3 );

    assertEquals( executor.getMaxRounds(), 2 );
    assertEquals( executor.getCurrentRound(), 0 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 0 );
    assertEquals( executor.getQueueSize(), 3 );

    // task executions
    assertEquals( task1.getRunCount(), 0 );
    assertEquals( task2.getRunCount(), 0 );
    assertEquals( task3.getRunCount(), 0 );

    assertFalse( executor.areTasksExecuting() );

    executor.activate();

    assertFalse( executor.areTasksExecuting() );

    // task executions
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 1 );

    assertEquals( executor.getCurrentRound(), 0 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 0 );
    assertEquals( executor.getQueueSize(), 0 );
  }

  @Test
  public void executeTasks()
  {
    final RoundBasedExecutor executor = new TestExecutor( 2 );

    final NoopTask task1 = new NoopTask();
    final NoopTask task2 = new NoopTask();
    final NoopTask task3 = new NoopTask();
    executor.queue( randomString(), task1 );
    executor.queue( randomString(), task2 );
    executor.queue( randomString(), task3 );

    assertEquals( executor.getMaxRounds(), 2 );
    assertEquals( executor.getCurrentRound(), 0 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 0 );
    assertEquals( executor.getQueueSize(), 3 );

    // task executions
    assertEquals( task1.getRunCount(), 0 );
    assertEquals( task2.getRunCount(), 0 );
    assertEquals( task3.getRunCount(), 0 );

    assertFalse( executor.areTasksExecuting() );

    executor.executeTasks();

    assertFalse( executor.areTasksExecuting() );

    // task executions
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 1 );

    assertEquals( executor.getCurrentRound(), 0 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 0 );
    assertEquals( executor.getQueueSize(), 0 );

    assertFalse( executor.runNextTask() );
  }

  @Test
  public void executeTasks_invoking_onRunawayReactionsDetected()
  {
    ZemeckisTestUtil.purgeTasksWhenRunawayDetected();

    final RoundBasedExecutor executor = new TestExecutor( 2 );

    final AtomicInteger task1CallCount = new AtomicInteger();
    final AtomicReference<Runnable> taskRef = new AtomicReference<>();
    final String name = randomString();
    final Runnable task = () -> {
      task1CallCount.incrementAndGet();
      executor.queue( name, taskRef.get() );
    };
    taskRef.set( task );

    executor.queue( name, task );

    assertEquals( executor.getMaxRounds(), 2 );
    assertEquals( executor.getCurrentRound(), 0 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 0 );
    assertEquals( executor.getQueueSize(), 1 );

    assertInvariantFailure( executor::executeTasks,
                            "Zemeckis-0010: Runaway task(s) detected. Tasks still running after 2 " +
                            "rounds. Current tasks include: [" + name + "]" );

    // Ensure tasks purged
    assertEquals( executor.getQueueSize(), 0 );

    assertEquals( task1CallCount.get(), 2 );
  }

  @Test
  public void executeTasks_invoking_onRunawayReactionsDetected_noInvariantsEnabled()
  {
    ZemeckisTestUtil.purgeTasksWhenRunawayDetected();
    BrainCheckTestUtil.setCheckInvariants( false );

    final RoundBasedExecutor executor = new TestExecutor( 2 );

    final AtomicInteger task1CallCount = new AtomicInteger();
    final AtomicReference<Runnable> taskRef = new AtomicReference<>();
    final Runnable task = () -> {
      task1CallCount.incrementAndGet();
      executor.queue( randomString(), taskRef.get() );
    };
    taskRef.set( task );

    executor.queue( randomString(), task );

    assertEquals( executor.getMaxRounds(), 2 );
    assertEquals( executor.getCurrentRound(), 0 );
    assertEquals( executor.getRemainingTasksInCurrentRound(), 0 );
    assertEquals( executor.getQueueSize(), 1 );

    executor.executeTasks();

    // Ensure tasks purged
    assertEquals( executor.getQueueSize(), 0 );

    assertEquals( task1CallCount.get(), 2 );
  }

  @Test
  public void onRunawayReactionsDetected()
  {
    ZemeckisTestUtil.purgeTasksWhenRunawayDetected();

    final RoundBasedExecutor executor = new TestExecutor( 2 );

    final String name = randomString();
    final Runnable task = new NoopTask();
    executor.queue( name, task );

    assertInvariantFailure( executor::onRunawayTasksDetected,
                            "Zemeckis-0010: Runaway task(s) detected. Tasks still running after 2 " +
                            "rounds. Current tasks include: [" + name + "]" );

    // Ensure tasks purged
    assertEquals( executor.getQueueSize(), 0 );
  }

  @Test
  public void onRunawayReactionsDetected_noPurgeConfigured()
  {
    ZemeckisTestUtil.noPurgeTasksWhenRunawayDetected();

    final RoundBasedExecutor executor = new TestExecutor( 2 );

    final String name = randomString();
    final Runnable task = new NoopTask();
    executor.queue( name, task );

    assertInvariantFailure( executor::onRunawayTasksDetected,
                            "Zemeckis-0010: Runaway task(s) detected. Tasks still running after 2 " +
                            "rounds. Current tasks include: [" + name + "]" );

    // Ensure tasks not purged
    assertEquals( executor.getQueueSize(), 1 );
  }
}
