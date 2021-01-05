package zemeckis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class ZemeckisTest
  extends AbstractTest
{
  @Test
  public void accessors()
  {
    assertEquals( Zemeckis.macroTaskVpu(), VirtualProcessorUnitsHolder.macroTaskVpu() );
    assertEquals( Zemeckis.microTaskVpu(), VirtualProcessorUnitsHolder.microTaskVpu() );
    assertEquals( Zemeckis.animationFrameVpu(), VirtualProcessorUnitsHolder.animationFrameVpu() );
    assertEquals( Zemeckis.animationFrameVpu(), VirtualProcessorUnitsHolder.animationFrameVpu() );
    assertEquals( Zemeckis.onIdleVpu(), VirtualProcessorUnitsHolder.onIdleVpu() );
    assertEquals( Zemeckis.now(), Zemeckis.now() );
  }

  @Test
  public void delayedTask()
    throws Exception
  {
    final List<String> errors = new ArrayList<>();
    final int count = 2;
    final CountDownLatch latch = new CountDownLatch( count );
    final int start = Zemeckis.now();
    Zemeckis.delayedTask( () -> {
      final int now = Zemeckis.now() - start;
      if ( now <= 19 )
      {
        errors.add( "Scheduled task 1 executed before expected" );
      }
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.macroTaskVpu() );
      latch.countDown();
    }, 20 );

    Zemeckis.delayedTask( () -> {
      final int now = Zemeckis.now() - start;
      if ( now <= 39 )
      {
        errors.add( "Scheduled task 2 executed before expected" );
      }
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.macroTaskVpu() );
      latch.countDown();
    }, 40 );
    assertInvariantFailure( () -> Zemeckis.delayedTask( () -> errors.add( "Scheduled task 4 that has a bad delay." ),
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
      Zemeckis.delayedTask( () -> errors.add( "Unexpected task execution" ), 20 );
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
      Zemeckis.periodicTask( () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.macroTaskVpu() );
        latch.countDown();
        if ( current.incrementAndGet() >= count )
        {
          task.get().cancel();
        }
      }, 20 );
    task.set( schedule );

    assertInvariantFailure( () -> Zemeckis.periodicTask( () -> errors.add( "Scheduled task that has a bad delay." ),
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
    assertFalse( Zemeckis.isVpuActivated() );
    assertNull( Zemeckis.currentVpu() );

    final int count = 5;
    final CountDownLatch latch = new CountDownLatch( count );
    Zemeckis.macroTask( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.macroTaskVpu() );
      latch.countDown();
    } );
    Zemeckis.microTask( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.microTaskVpu() );
      latch.countDown();
    } );
    Zemeckis.animationFrame( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.animationFrameVpu() );
      latch.countDown();
    } );
    Zemeckis.afterFrame( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.afterFrameVpu() );
      latch.countDown();
    } );
    Zemeckis.onIdle( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.onIdleVpu() );
      latch.countDown();
    } );

    assertFalse( Zemeckis.isVpuActivated() );
    assertNull( Zemeckis.currentVpu() );

    latch.await( 1, TimeUnit.SECONDS );
    assertEquals( latch.getCount(), 0 );
  }

  @Test
  public void canceledTaskNoRun()
    throws Exception
  {
    assertFalse( Zemeckis.isVpuActivated() );
    assertNull( Zemeckis.currentVpu() );

    final int count = 5;
    final CountDownLatch latch = new CountDownLatch( count );
    Zemeckis.macroTask( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.macroTaskVpu() );
      sleep();
      latch.countDown();
    } ).cancel();
    Zemeckis.microTask( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.microTaskVpu() );
      sleep();
      latch.countDown();
    } ).cancel();
    Zemeckis.animationFrame( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.animationFrameVpu() );
      sleep();
      latch.countDown();
    } ).cancel();
    Zemeckis.afterFrame( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.afterFrameVpu() );
      sleep();
      latch.countDown();
    } ).cancel();
    Zemeckis.onIdle( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.onIdleVpu() );
      sleep();
      latch.countDown();
    } ).cancel();

    assertFalse( Zemeckis.isVpuActivated() );
    assertNull( Zemeckis.currentVpu() );

    latch.await( 100, TimeUnit.MILLISECONDS );
    // The latch should be 4 or 5. 4 if the scheduled task runs before the
    // cancel can be invoked. But the scheduler only has one thread so it
    // will then sleep for 10ms which will ensure all the other tasks can
    // be cancelled
    assertTrue( latch.getCount() >= 4 );
  }

  private void sleep()
  {
    try
    {
      Thread.sleep( 10 );
    }
    catch ( final InterruptedException ignored )
    {
    }
  }

  @Test
  public void becomeMacroTask()
  {
    final List<String> trace = new ArrayList<>();
    assertFalse( Zemeckis.isVpuActivated() );
    assertNull( Zemeckis.currentVpu() );

    ( (AbstractExecutor) Zemeckis.macroTaskVpu().getExecutor() )
      .getTaskQueue()
      .add( new TaskEntry( () -> trace.add( "A" ) ) );

    Zemeckis.becomeMacroTask( () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.macroTaskVpu() );
      trace.add( "*" );
      Zemeckis.macroTask( () -> trace.add( "B" ) );
      assertInvariantFailure( () -> Zemeckis.becomeMacroTask( () -> trace.add( "C" ) ),
                              "Zemeckis-0012: Scheduler.becomeMacroTask(...) invoked but the VirtualProcessorUnit named 'macro' is already active" );
    } );

    assertFalse( Zemeckis.isVpuActivated() );
    assertNull( Zemeckis.currentVpu() );

    assertEquals( String.join( "", trace ), "*AB" );
  }
}
