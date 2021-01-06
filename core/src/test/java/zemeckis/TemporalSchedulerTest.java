package zemeckis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class TemporalSchedulerTest
  extends AbstractTest
{
  @Test
  public void basicOperation_delayedTask()
    throws Exception
  {
    final List<String> errors = new ArrayList<>();
    final int count = 2;
    final CountDownLatch latch = new CountDownLatch( count );
    final int start = TemporalScheduler.now();
    TemporalScheduler.delayedTask( randomString(), () -> {
      final int now = TemporalScheduler.now() - start;
      if ( now <= 19 )
      {
        errors.add( "Scheduled task 1 executed before expected" );
      }
      latch.countDown();
    }, 20 );

    TemporalScheduler.delayedTask( randomString(), () -> {
      final int now = TemporalScheduler.now() - start;
      if ( now <= 39 )
      {
        errors.add( "Scheduled task 2 executed before expected" );
      }
      latch.countDown();
    }, 40 );
    assertInvariantFailure( () -> TemporalScheduler
                              .delayedTask( "D33", () -> errors.add( "Scheduled task 4 that has a bad delay." ), -1 ),
                            "Zemeckis-0008: Zemeckis.delayedTask(...) named 'D33' passed a negative delay. Actual value passed is -1" );

    assertTrue( latch.await( 1, TimeUnit.SECONDS ) );
    assertEquals( latch.getCount(), 0 );
    if ( !errors.isEmpty() )
    {
      fail( "Errors detected in other threads:\n" + String.join( "\n", errors ) );
    }
  }

  @Test
  public void delayedTask_canceled()
    throws Exception
  {
    final List<String> errors = new ArrayList<>();
    final int count = 1;
    final CountDownLatch latch = new CountDownLatch( count );
    final String name = randomString();
    final Cancelable token =
      TemporalScheduler.delayedTask( name, () -> errors.add( "Unexpected task execution" ), 20 );
    assertEquals( token.toString(), name );
    token.cancel();
    assertFalse( latch.await( 100, TimeUnit.MILLISECONDS ) );
    assertEquals( latch.getCount(), count );
    if ( !errors.isEmpty() )
    {
      fail( "Errors detected in other threads:\n" + String.join( "\n", errors ) );
    }
  }

  @Test
  public void basicOperation_doScheduleAtFixedRate()
    throws Exception
  {
    final List<String> errors = new ArrayList<>();
    final int count = 2;
    final AtomicInteger current = new AtomicInteger();
    final AtomicReference<Cancelable> task = new AtomicReference<>();
    final CountDownLatch latch = new CountDownLatch( count );
    final String name = randomString();
    final Cancelable schedule =
      TemporalScheduler.periodicTask( name, () -> {
        latch.countDown();
        if ( current.incrementAndGet() >= count )
        {
          task.get().cancel();
        }
      }, 20 );
    task.set( schedule );
    assertEquals( schedule.toString(), name );

    assertInvariantFailure( () -> TemporalScheduler
                              .periodicTask( "P1", () -> errors.add( "Scheduled task that has a bad delay." ), -1 ),
                            "Zemeckis-0009: Zemeckis.periodicTask(...) named 'P1' passed a non-positive period. Actual value passed is -1" );

    assertTrue( latch.await( 1, TimeUnit.SECONDS ) );
    assertEquals( latch.getCount(), 0 );
    if ( !errors.isEmpty() )
    {
      fail( "Errors detected in other threads:\n" + String.join( "\n", errors ) );
    }
  }
}
