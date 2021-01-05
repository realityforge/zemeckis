package zemeckis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class SchedulerTest
  extends AbstractTest
{
  @Test
  public void accessors()
  {
    assertEquals( Scheduler.macroTaskVpu(), VirtualProcessorUnitsHolder.macroTaskVpu() );
    assertEquals( Scheduler.microTaskVpu(), VirtualProcessorUnitsHolder.microTaskVpu() );
    assertEquals( Scheduler.animationFrameVpu(), VirtualProcessorUnitsHolder.animationFrameVpu() );
    assertEquals( Scheduler.animationFrameVpu(), VirtualProcessorUnitsHolder.animationFrameVpu() );
    assertEquals( Scheduler.onIdleVpu(), VirtualProcessorUnitsHolder.onIdleVpu() );
    assertEquals( Scheduler.now(), Scheduler.now() );
  }

  @Test
  public void delayedTask()
    throws Exception
  {
    final List<String> errors = new ArrayList<>();
    final int count = 2;
    final CountDownLatch latch = new CountDownLatch( count );
    final int start = Scheduler.now();
    Scheduler.delayedTask( () -> {
      final int now = Scheduler.now() - start;
      if ( now <= 19 )
      {
        errors.add( "Scheduled task 1 executed before expected" );
      }
      latch.countDown();
    }, 20 );

    Scheduler.delayedTask( () -> {
      final int now = Scheduler.now() - start;
      if ( now <= 39 )
      {
        errors.add( "Scheduled task 2 executed before expected" );
      }
      latch.countDown();
    }, 40 );
    assertInvariantFailure( () -> Scheduler.delayedTask( () -> errors.add( "Scheduled task 4 that has a bad delay." ),
                                                         -1 ),
                            "Zemeckis-0008: Scheduler.delayedTask(...) passed a negative delay. Actual value passed is -1" );

    latch.await( 1, TimeUnit.SECONDS );
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
    final Cancelable token =
      Scheduler.delayedTask( () -> errors.add( "Unexpected task execution" ), 20 );
    token.cancel();
    latch.await( 100, TimeUnit.MILLISECONDS );
    assertEquals( latch.getCount(), count );
    if ( !errors.isEmpty() )
    {
      fail( "Errors detected in other threads:\n" + String.join( "\n", errors ) );
    }
  }

  @Test
  public void periodicTask()
    throws Exception
  {
    final List<String> errors = new ArrayList<>();
    final int count = 2;
    final AtomicInteger current = new AtomicInteger();
    final AtomicReference<Cancelable> task = new AtomicReference<>();
    final CountDownLatch latch = new CountDownLatch( count );
    final Cancelable schedule =
      Scheduler.periodicTask( () -> {
        latch.countDown();
        if ( current.incrementAndGet() >= count )
        {
          task.get().cancel();
        }
      }, 20 );
    task.set( schedule );

    assertInvariantFailure( () -> Scheduler
                              .periodicTask( () -> errors.add( "Scheduled task that has a bad delay." ),
                                             -1 ),
                            "Zemeckis-0009: Scheduler.periodicTask(...) passed a non-positive period. Actual value passed is -1" );

    latch.await( 1, TimeUnit.SECONDS );
    assertEquals( latch.getCount(), 0 );
    if ( !errors.isEmpty() )
    {
      fail( "Errors detected in other threads:\n" + String.join( "\n", errors ) );
    }
  }

  @Test
  public void vpu()
    throws Exception
  {
    assertFalse( Scheduler.isVpuActivated() );
    assertNull( Scheduler.currentVpu() );

    final int count = 5;
    final CountDownLatch latch = new CountDownLatch( count );
    Scheduler.macroTask( () -> {
      assertTrue( Scheduler.isVpuActivated() );
      assertEquals( Scheduler.currentVpu(), Scheduler.macroTaskVpu() );
      latch.countDown();
    } );
    Scheduler.microTask( () -> {
      assertTrue( Scheduler.isVpuActivated() );
      assertEquals( Scheduler.currentVpu(), Scheduler.microTaskVpu() );
      latch.countDown();
    } );
    Scheduler.animationFrame( () -> {
      assertTrue( Scheduler.isVpuActivated() );
      assertEquals( Scheduler.currentVpu(), Scheduler.animationFrameVpu() );
      latch.countDown();
    } );
    Scheduler.afterFrame( () -> {
      assertTrue( Scheduler.isVpuActivated() );
      assertEquals( Scheduler.currentVpu(), Scheduler.afterFrameVpu() );
      latch.countDown();
    } );
    Scheduler.onIdle( () -> {
      assertTrue( Scheduler.isVpuActivated() );
      assertEquals( Scheduler.currentVpu(), Scheduler.onIdleVpu() );
      latch.countDown();
    } );

    assertFalse( Scheduler.isVpuActivated() );
    assertNull( Scheduler.currentVpu() );

    latch.await( 1, TimeUnit.SECONDS );
    assertEquals( latch.getCount(), 0 );
  }

  @Test
  public void becomeMacroTask()
  {
    final List<String> trace = new ArrayList<>();
    assertFalse( Scheduler.isVpuActivated() );
    assertNull( Scheduler.currentVpu() );

    ( (AbstractExecutor) Scheduler.macroTaskVpu().getExecutor() ).getTaskQueue().add( () -> trace.add( "A" ) );

    Scheduler.becomeMacroTask( () -> {
      assertTrue( Scheduler.isVpuActivated() );
      assertEquals( Scheduler.currentVpu(), Scheduler.macroTaskVpu() );
      trace.add( "*" );
      Scheduler.macroTask( () -> trace.add( "B" ) );
      assertInvariantFailure( () -> Scheduler.becomeMacroTask( () -> trace.add( "C" ) ),
                              "Zemeckis-0012: Scheduler.becomeMacroTask(...) invoked but the VirtualProcessorUnit named 'macro' is already active" );
    } );

    assertFalse( Scheduler.isVpuActivated() );
    assertNull( Scheduler.currentVpu() );

    assertEquals( String.join( "", trace ), "*AB" );
  }
}
