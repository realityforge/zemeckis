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
                            "Zemeckis-0008: Zemeckis.delayedTask(...) named 'DelayedTask@2' passed a negative delay. Actual value passed is -1" );

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
    final Cancelable token =
      Zemeckis.delayedTask( () -> errors.add( "Unexpected task execution" ), 20 );
    token.cancel();
    assertFalse( latch.await( 100, TimeUnit.MILLISECONDS ) );
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
    assertEquals( schedule.toString(), "PeriodicTask@0" );
    task.set( schedule );

    assertInvariantFailure( () -> Zemeckis.periodicTask( "P2",
                                                         () -> errors.add( "Scheduled task that has a bad delay." ),
                                                         -1 ),
                            "Zemeckis-0009: Zemeckis.periodicTask(...) named 'P2' passed a non-positive period. Actual value passed is -1" );

    assertTrue( latch.await( 1, TimeUnit.SECONDS ) );
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

    final String name1 = randomString();
    final String name2 = randomString();
    final String name3 = randomString();
    final String name4 = randomString();
    final String name5 = randomString();

    final int count = 10;
    final CountDownLatch latch = new CountDownLatch( count );
    final Cancelable cancelable1 =
      Zemeckis.macroTask( () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.macroTaskVpu() );
        latch.countDown();
      } );
    final Cancelable cancelable2 =
      Zemeckis.macroTask( name1, () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.macroTaskVpu() );
        latch.countDown();
      } );
    final Cancelable cancelable3 =
      Zemeckis.microTask( () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.microTaskVpu() );
        latch.countDown();
      } );
    final Cancelable cancelable4 =
      Zemeckis.microTask( name2, () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.microTaskVpu() );
        latch.countDown();
      } );
    final Cancelable cancelable5 =
      Zemeckis.animationFrame( () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.animationFrameVpu() );
        latch.countDown();
      } );
    final Cancelable cancelable6 =
      Zemeckis.animationFrame( name3, () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.animationFrameVpu() );
        latch.countDown();
      } );
    final Cancelable cancelable7 =
      Zemeckis.afterFrame( () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.afterFrameVpu() );
        latch.countDown();
      } );
    final Cancelable cancelable8 =
      Zemeckis.afterFrame( name4, () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.afterFrameVpu() );
        latch.countDown();
      } );
    final Cancelable cancelable9 =
      Zemeckis.onIdle( () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.onIdleVpu() );
        latch.countDown();
      } );
    final Cancelable cancelable10 =
      Zemeckis.onIdle( name5, () -> {
        assertTrue( Zemeckis.isVpuActivated() );
        assertEquals( Zemeckis.currentVpu(), Zemeckis.onIdleVpu() );
        latch.countDown();
      } );

    assertFalse( Zemeckis.isVpuActivated() );
    assertNull( Zemeckis.currentVpu() );

    assertTrue( latch.await( 1, TimeUnit.SECONDS ) );
    assertEquals( latch.getCount(), 0 );
    assertEquals( cancelable1.toString(), "MacroTask@0" );
    assertEquals( cancelable2.toString(), name1 );
    assertEquals( cancelable3.toString(), "MicroTask@1" );
    assertEquals( cancelable4.toString(), name2 );
    assertEquals( cancelable5.toString(), "AnimationFrameTask@2" );
    assertEquals( cancelable6.toString(), name3 );
    assertEquals( cancelable7.toString(), "AfterFrameTask@3" );
    assertEquals( cancelable8.toString(), name4 );
    assertEquals( cancelable9.toString(), "OnIdleTask@4" );
    assertEquals( cancelable10.toString(), name5 );
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

    //noinspection ResultOfMethodCallIgnored
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
      .add( new TaskEntry( "A", () -> trace.add( "A" ), null ) );

    Zemeckis.becomeMacroTask( randomString(), () -> {
      assertTrue( Zemeckis.isVpuActivated() );
      assertEquals( Zemeckis.currentVpu(), Zemeckis.macroTaskVpu() );
      trace.add( "*" );
      Zemeckis.macroTask( () -> trace.add( "B" ) );
      assertInvariantFailure( () -> Zemeckis.becomeMacroTask( "MyCTask", () -> trace.add( "C" ) ),
                              "Zemeckis-0012: Zemeckis.becomeMacroTask(...) invoked for the task named 'MyCTask' but the VirtualProcessorUnit named 'Macro' is already active" );
    } );

    assertFalse( Zemeckis.isVpuActivated() );
    assertNull( Zemeckis.currentVpu() );

    assertEquals( String.join( "", trace ), "*AB" );
  }
}
