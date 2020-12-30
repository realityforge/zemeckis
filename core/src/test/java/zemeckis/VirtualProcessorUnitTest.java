package zemeckis;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class VirtualProcessorUnitTest
  extends AbstractTest
{
  @Test
  public void basicOperation()
  {
    final String name = randomString();
    final TestExecutor executor = new TestExecutor();
    final CircularBuffer<Runnable> taskQueue = executor.getTaskQueue();
    final VirtualProcessorUnit vpu = new VirtualProcessorUnit( name, executor );
    assertEquals( vpu.getName(), name );
    assertEquals( vpu.getExecutor(), executor );
    assertEquals( vpu.toString(), name );

    final Runnable task = new NoopTask();
    assertEquals( taskQueue.size(), 0 );
    assertEquals( executor.getScheduleCount(), 0 );
    vpu.queue( task );
    assertEquals( taskQueue.size(), 1 );
    assertEquals( executor.getScheduleCount(), 1 );
    assertEquals( taskQueue.peek(), task );
  }

  @Test
  public void noNamesWhenNamesDisabled()
  {
    final String name = randomString();
    final VirtualProcessorUnit vpu = new VirtualProcessorUnit( name, new TestExecutor() );
    assertDefaultToStringWhenNamesDisabled( vpu );

    ZemeckisTestUtil.disableNames();
    assertInvariantFailure( vpu::getName,
                            "Zemeckis-0003: VirtualProcessorUnit.getName() invoked when Zemeckis.areNamesEnabled() is false" );

    assertInvariantFailure( () -> new VirtualProcessorUnit( name, new TestExecutor() ),
                            "Zemeckis-0002: VirtualProcessorUnit passed a name '" + name +
                            "' but Zemeckis.areNamesEnabled() is false" );
  }
}
