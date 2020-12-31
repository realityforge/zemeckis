package zemeckis;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class ExecutorTest
  extends AbstractTest
{
  @Test
  public void basicSetup()
  {
    final TestExecutor executor = new TestExecutor();

    assertFalse( executor.areTasksExecuting() );
    assertEquals( executor.getScheduleCount(), 0 );

    assertEquals( executor.getQueueSize(), 0 );
    assertEquals( executor.getTaskQueue().size(), 0 );
    assertEquals( executor.getTaskQueue().getCapacity(), 100 );

    //noinspection ResultOfMethodCallIgnored
    assertThrows( AssertionError.class, executor::context );

    final VirtualProcessorUnit.Context context = VirtualProcessorUnit.ActivationFn::invoke;
    executor.init( context );

    assertEquals( executor.context(), context );
  }

  @Test
  public void queue()
  {
    final TestExecutor executor = new TestExecutor();
    final CircularBuffer<Runnable> taskQueue = executor.getTaskQueue();

    final Runnable task1 = new NoopTask();
    final Runnable task2 = new NoopTask();
    final Runnable task3 = new NoopTask();

    assertEquals( executor.getQueueSize(), 0 );
    executor.queue( task1 );
    assertEquals( executor.getQueueSize(), 1 );
    assertEquals( taskQueue.get( 0 ), task1 );

    executor.queue( task2 );
    assertEquals( executor.getQueueSize(), 2 );
    assertEquals( taskQueue.get( 0 ), task1 );
    assertEquals( taskQueue.get( 1 ), task2 );

    executor.queueNext( task3 );
    assertEquals( executor.getQueueSize(), 3 );
    assertEquals( taskQueue.get( 0 ), task3 );
    assertEquals( taskQueue.get( 1 ), task1 );
    assertEquals( taskQueue.get( 2 ), task2 );
  }

  @Test
  public void queue_whenAlreadyPresent()
  {
    final TestExecutor executor = new TestExecutor();
    final CircularBuffer<Runnable> taskQueue = executor.getTaskQueue();

    final Runnable task = new NoopTask();

    assertEquals( executor.getQueueSize(), 0 );
    executor.queue( task );
    assertEquals( executor.getQueueSize(), 1 );
    assertEquals( taskQueue.get( 0 ), task );

    assertInvariantFailure( () -> executor.queue( task ),
                            "Zemeckis-0001: Attempting to queue task " + task +
                            " when task is already queued." );
    assertInvariantFailure( () -> executor.queueNext( task ),
                            "Zemeckis-0001: Attempting to queue task " + task +
                            " when task is already queued." );
  }

  @Test
  public void executeNextTask()
  {
    final TestExecutor executor = new TestExecutor();
    final CircularBuffer<Runnable> taskQueue = executor.getTaskQueue();
    final NoopTask task1 = new NoopTask();
    final NoopTask task2 = new NoopTask();
    final NoopTask task3 = new NoopTask();

    assertEquals( executor.getQueueSize(), 0 );
    executor.queue( task1 );
    executor.queue( task2 );
    executor.queue( task3 );
    assertEquals( executor.getQueueSize(), 3 );
    assertEquals( taskQueue.get( 0 ), task1 );
    assertEquals( taskQueue.get( 1 ), task2 );
    assertEquals( taskQueue.get( 2 ), task3 );
    assertEquals( task1.getRunCount(), 0 );
    assertEquals( task2.getRunCount(), 0 );
    assertEquals( task3.getRunCount(), 0 );

    executor.executeNextTask();
    assertEquals( executor.getQueueSize(), 2 );
    assertEquals( taskQueue.get( 0 ), task2 );
    assertEquals( taskQueue.get( 1 ), task3 );
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 0 );
    assertEquals( task3.getRunCount(), 0 );

    executor.executeNextTask();
    assertEquals( executor.getQueueSize(), 1 );
    assertEquals( taskQueue.get( 0 ), task3 );
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 0 );

    executor.executeNextTask();
    assertEquals( executor.getQueueSize(), 0 );
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 1 );
  }

  @Test
  public void executeNextTask_tasksThrowsException()
  {
    allowUncaughtExceptions();

    final TestExecutor executor = new TestExecutor();
    final CircularBuffer<Runnable> taskQueue = executor.getTaskQueue();
    final AtomicInteger runCount = new AtomicInteger();
    final AtomicInteger errorCount = new AtomicInteger();
    final String errorMessage = randomString();
    final Runnable task1 = () -> {
      runCount.incrementAndGet();
      throw new IllegalStateException( errorMessage );
    };

    assertEquals( executor.getQueueSize(), 0 );
    executor.queue( task1 );
    assertEquals( executor.getQueueSize(), 1 );
    assertEquals( taskQueue.get( 0 ), task1 );

    Zemeckis.addUncaughtErrorHandler( e -> {
      errorCount.incrementAndGet();
      assertTrue( e instanceof IllegalStateException );
      assertEquals( e.getMessage(), errorMessage );
    } );

    assertEquals( runCount.get(), 0 );
    assertEquals( errorCount.get(), 0 );

    executor.executeNextTask();
    assertEquals( executor.getQueueSize(), 0 );

    assertEquals( runCount.get(), 1 );
    assertEquals( errorCount.get(), 1 );
  }
}
