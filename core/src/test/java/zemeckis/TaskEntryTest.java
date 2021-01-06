package zemeckis;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class TaskEntryTest
  extends AbstractTest
{
  @Test
  public void basicOperation()
  {
    final String name = randomString();
    final NoopTask task = new NoopTask();
    final Cancelable cancelAction = null;
    final TaskEntry entry = new TaskEntry( name, task, cancelAction );
    assertEquals( entry.getTask(), task );
    assertEquals( entry.getCancelAction(), cancelAction );
    assertEquals( entry.toString(), name );

    assertEquals( task.getRunCount(), 0 );
    entry.execute();
    assertEquals( task.getRunCount(), 1 );

    assertNull( entry.getTask() );
    assertNull( entry.getCancelAction() );

    // No matter how many executes it is a one and done deal
    entry.execute();
    entry.execute();
    entry.execute();
    entry.execute();
    entry.execute();

    assertEquals( task.getRunCount(), 1 );

    assertNull( entry.getTask() );
    assertNull( entry.getCancelAction() );

    assertDefaultToStringWhenNamesDisabled( entry );
  }

  @Test
  public void constructWithNameWhenNamesDisabled()
  {
    ZemeckisTestUtil.disableNames();
    final String name = randomString();
    assertInvariantFailure( () -> new TaskEntry( name, new NoopTask(), null ),
                            "Zemeckis-0013: Task passed a name '" + name +
                            "' but Zemeckis.areNamesEnabled() is false" );
  }

  @Test
  public void cancel()
  {
    final String name = randomString();
    final NoopTask task = new NoopTask();
    final AtomicInteger cancelCount = new AtomicInteger();
    final Cancelable cancelAction = cancelCount::incrementAndGet;
    final TaskEntry entry = new TaskEntry( name, task, cancelAction );
    assertEquals( entry.getTask(), task );
    assertEquals( entry.getCancelAction(), cancelAction );
    assertEquals( entry.toString(), name );

    entry.cancel();

    assertNull( entry.getTask() );
    assertNull( entry.getCancelAction() );
    assertEquals( cancelCount.get(), 1 );

    // Second cancel is ignored
    entry.cancel();

    assertNull( entry.getTask() );
    assertNull( entry.getCancelAction() );
    assertEquals( cancelCount.get(), 1 );

    // execute ignored after cancel
    entry.execute();
    assertEquals( task.getRunCount(), 0 );
  }
}
