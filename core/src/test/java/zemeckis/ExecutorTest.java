package zemeckis;

import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
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
    final CircularBuffer<TaskEntry> taskQueue = executor.getTaskQueue();

    final String name1 = randomString();
    final String name2 = randomString();

    final Runnable task1 = new NoopTask();
    final Runnable task2 = new NoopTask();
    final Runnable task3 = new NoopTask();

    assertEquals( executor.getQueueSize(), 0 );
    final Cancelable cancelable1 = executor.queue( name1, task1 );
    assertEquals( cancelable1.toString(), name1 );
    assertEquals( executor.getQueueSize(), 1 );
    assertTaskAt( taskQueue, 0, task1 );

    final Cancelable cancelable2 = executor.queue( name2, task2 );
    assertEquals( cancelable2.toString(), name2 );
    assertEquals( executor.getQueueSize(), 2 );
    assertTaskAt( taskQueue, 0, task1 );
    assertTaskAt( taskQueue, 1, task2 );

    executor.queueNext( randomString(), task3 );
    assertEquals( executor.getQueueSize(), 3 );

    assertTaskAt( taskQueue, 0, task3 );
    assertTaskAt( taskQueue, 1, task1 );
    assertTaskAt( taskQueue, 2, task2 );
  }

  @Test
  public void queue_whenAlreadyPresent()
  {
    final TestExecutor executor = new TestExecutor();
    final CircularBuffer<TaskEntry> taskQueue = executor.getTaskQueue();

    final Runnable task = new NoopTask();

    assertEquals( executor.getQueueSize(), 0 );
    executor.queue( randomString(), task );
    assertEquals( executor.getQueueSize(), 1 );
    assertTaskAt( taskQueue, 0, task );

    assertInvariantFailure( () -> executor.queue( "MyTask1", task ),
                            "Zemeckis-0001: Attempting to queue task named 'MyTask1' when task is already queued." );
    assertInvariantFailure( () -> executor.queueNext( "MyTask2", task ),
                            "Zemeckis-0001: Attempting to queue task named 'MyTask2' when task is already queued." );
  }

  @Test
  public void executeNextTask()
  {
    final TestExecutor executor = new TestExecutor();
    final CircularBuffer<TaskEntry> taskQueue = executor.getTaskQueue();
    final NoopTask task1 = new NoopTask();
    final NoopTask task2 = new NoopTask();
    final NoopTask task3 = new NoopTask();
    final NoopTask task4 = new NoopTask();

    assertEquals( executor.getQueueSize(), 0 );
    executor.queue( randomString(), task1 );
    executor.queue( randomString(), task2 );
    executor.queue( randomString(), task3 );
    final Cancelable cancelable4 = executor.queue( randomString(), task4 );
    assertEquals( executor.getQueueSize(), 4 );
    assertTaskAt( taskQueue, 0, task1 );
    assertTaskAt( taskQueue, 1, task2 );
    assertTaskAt( taskQueue, 2, task3 );
    assertTaskAt( taskQueue, 3, task4 );
    assertEquals( task1.getRunCount(), 0 );
    assertEquals( task2.getRunCount(), 0 );
    assertEquals( task3.getRunCount(), 0 );
    assertEquals( task4.getRunCount(), 0 );

    executor.executeNextTask();
    assertEquals( executor.getQueueSize(), 3 );
    assertTaskAt( taskQueue, 0, task2 );
    assertTaskAt( taskQueue, 1, task3 );
    assertTaskAt( taskQueue, 2, task4 );
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 0 );
    assertEquals( task3.getRunCount(), 0 );
    assertEquals( task4.getRunCount(), 0 );

    executor.executeNextTask();
    assertEquals( executor.getQueueSize(), 2 );
    assertTaskAt( taskQueue, 0, task3 );
    assertTaskAt( taskQueue, 1, task4 );
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 0 );
    assertEquals( task4.getRunCount(), 0 );

    executor.executeNextTask();
    assertEquals( executor.getQueueSize(), 1 );
    assertTaskAt( taskQueue, 0, task4 );
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 1 );
    assertEquals( task4.getRunCount(), 0 );

    // Cancel task4 so that the runnable is not actually run
    cancelable4.cancel();
    cancelable4.cancel();

    executor.executeNextTask();
    assertEquals( executor.getQueueSize(), 0 );
    assertEquals( task1.getRunCount(), 1 );
    assertEquals( task2.getRunCount(), 1 );
    assertEquals( task3.getRunCount(), 1 );
    assertEquals( task4.getRunCount(), 0 );
  }

  @Test
  public void executeNextTask_tasksThrowsException()
  {
    allowUncaughtExceptions();

    final TestExecutor executor = new TestExecutor();
    final CircularBuffer<TaskEntry> taskQueue = executor.getTaskQueue();
    final AtomicInteger runCount = new AtomicInteger();
    final AtomicInteger errorCount = new AtomicInteger();
    final String errorMessage = randomString();
    final Runnable task1 = () -> {
      runCount.incrementAndGet();
      throw new IllegalStateException( errorMessage );
    };

    assertEquals( executor.getQueueSize(), 0 );
    executor.queue( randomString(), task1 );
    assertEquals( executor.getQueueSize(), 1 );
    assertTaskAt( taskQueue, 0, task1 );

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

  private void assertTaskAt( @Nonnull final CircularBuffer<TaskEntry> taskQueue, final int index, @Nonnull final Runnable task )
  {
    final TaskEntry entry = taskQueue.get( index );
    assertNotNull( entry );
    assertEquals( entry.getTask(), task );
  }
}
