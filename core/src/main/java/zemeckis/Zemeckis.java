package zemeckis;

import grim.annotations.OmitClinit;
import grim.annotations.OmitSymbol;
import grim.annotations.OmitType;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;
import org.realityforge.braincheck.BrainCheckConfig;
import static org.realityforge.braincheck.Guards.*;

/**
 * The class is provides the access to global configuration settings as well as methods for scheduling
 * and executing tasks asynchronously.
 *
 * <p>Zemeckis has an internal clock that represents time as a monotonically increasing
 * <code>int</code> value. The value may or may not have a direct relationship to wall-clock
 * time and the unit of the value is defined by the implementation.</p>
 *
 * <p>The scheduler assigns tasks to different {@link VirtualProcessorUnit} (VPU) instances that run at
 * different phases in the browser lifecycle. Tasks can be scheduled on the:</p>
 * <ul>
 * <li>"Macro" task VPU. The normal phase where event callbacks, timer timeouts, message channel handlers etc. are invoked.</li>
 * <li>"Micro" task VPU. Invoked immediately after the current "Macro" or "Micro" task.</li>
 * <li>"animationFrame" VPU. Invoked prior to the next browser render.</li>
 * <li>"afterFrame" VPU. Invoked after the next browser render.</li>
 * <li>"onIdle" VPU. Invoked when the browser is idle.</li>
 * </ul>
 */
@OmitClinit
public final class Zemeckis
{
  private Zemeckis()
  {
  }

  /**
   * Return true if user should pass names into API methods, false if should pass null.
   *
   * @return true if user should pass names into API methods, false if should pass null.
   */
  public static boolean areNamesEnabled()
  {
    return ZemeckisConfig.areNamesEnabled();
  }

  /**
   * Return true if uncaught error handlers are enabled.
   *
   * @return true if uncaught error handlers are enabled, false otherwise.
   */
  public static boolean areUncaughtErrorHandlersEnabled()
  {
    return ZemeckisConfig.areUncaughtErrorHandlersEnabled();
  }

  /**
   * Return true if invariants will be checked.
   *
   * @return true if invariants will be checked.
   */
  public static boolean shouldCheckInvariants()
  {
    return BrainCheckConfig.checkInvariants();
  }

  /**
   * Return true if apiInvariants will be checked.
   *
   * @return true if apiInvariants will be checked.
   */
  public static boolean shouldCheckApiInvariants()
  {
    return BrainCheckConfig.checkApiInvariants();
  }

  /**
   * Return true if active tasks will be purged if the scheduler is still running after the maximum number of rounds.
   *
   * @return true if active tasks will be purged if the scheduler is still running after the maximum number of rounds.
   */
  public static boolean purgeTasksWhenRunawayDetected()
  {
    return ZemeckisConfig.purgeTasksWhenRunawayDetected();
  }

  /**
   * Add error handler to the list of error handlers called.
   * The handler should not already be in the list. This method should NOT be called if
   * {@link #areUncaughtErrorHandlersEnabled()} returns false.
   *
   * @param handler the error handler.
   */
  public static void addUncaughtErrorHandler( @Nonnull final UncaughtErrorHandler handler )
  {
    UncaughtErrorHandlerSupport.get().addUncaughtErrorHandler( handler );
  }

  /**
   * Remove error handler from list of existing error handlers.
   * The handler should already be in the list. This method should NOT be called if
   * {@link #areUncaughtErrorHandlersEnabled()} returns false.
   *
   * @param handler the error handler.
   */
  public static void removeUncaughtErrorHandler( @Nonnull final UncaughtErrorHandler handler )
  {
    UncaughtErrorHandlerSupport.get().removeUncaughtErrorHandler( handler );
  }

  /**
   * Report an uncaught error in stream.
   *
   * @param error the error.
   */
  public static void reportUncaughtError( @Nonnull final Throwable error )
  {
    if ( areUncaughtErrorHandlersEnabled() )
    {
      UncaughtErrorHandlerSupport.get().onUncaughtError( error );
    }
  }

  /**
   * Return a value representing the "current time" of the scheduler.
   *
   * @return the "current time" of the scheduler.
   */
  public static int now()
  {
    return TemporalScheduler.now();
  }

  /**
   * Schedules the execution of the given task after a specified delay.
   *
   * @param task  the task to execute.
   * @param delay the delay before the task should execute. Must be a value greater than 0.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable delayedTask( @Nonnull final Runnable task, final int delay )
  {
    return delayedTask( null, task, delay );
  }

  /**
   * Schedules the execution of the given task after a specified delay.
   *
   * @param name  A human consumable name for the task. It may be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task  the task to execute.
   * @param delay the delay before the task should execute. Must be a value greater than 0.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable delayedTask( @Nullable final String name, @Nonnull final Runnable task, final int delay )
  {
    final String actualName = generateName( "DelayedTask", name );
    return TemporalScheduler.delayedTask( actualName, () -> becomeMacroTask( actualName, task ), delay );
  }

  /**
   * Schedules the periodic execution of the given task with specified period.
   *
   * @param task   the task to execute.
   * @param period the period after execution when the task should be re-executed. Must be a value greater than 0.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable periodicTask( @Nonnull final Runnable task, final int period )
  {
    return periodicTask( null, task, period );
  }

  /**
   * Schedules the periodic execution of the given task with specified period.
   *
   * @param name   A human consumable name for the task. It may be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task   the task to execute.
   * @param period the period after execution when the task should be re-executed. Must be a value greater than 0.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable periodicTask( @Nullable final String name, @Nonnull final Runnable task, final int period )
  {
    final String actualName = generateName( "PeriodicTask", name );
    return TemporalScheduler.periodicTask( actualName, () -> becomeMacroTask( actualName, task ), period );
  }

  /**
   * Return true if there is a current VPU activated.
   *
   * @return true if there is a current VPU activated.
   */
  public static boolean isVpuActivated()
  {
    return VirtualProcessorUnitsHolder.isVpuActivated();
  }

  /**
   * Return the current VPU.
   *
   * @return the current VPU.
   */
  @Nullable
  public static VirtualProcessorUnit currentVpu()
  {
    return VirtualProcessorUnitsHolder.currentVpu();
  }

  /**
   * Queue the task to execute in the next "macro" task.
   * This is the default VPU in the browser and indicates tasks that are scheduled via a
   * call to <code>setTimeout(callback,0)</code>.
   *
   * @param task the task.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable macroTask( @Nonnull final Runnable task )
  {
    return macroTask( null, task );
  }

  /**
   * Queue the task to execute in the next "macro" task.
   * This is the default VPU in the browser and indicates tasks that are scheduled via a
   * call to <code>setTimeout(callback,0)</code>.
   *
   * @param name A human consumable name for the task. It may be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task the task.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable macroTask( @Nullable final String name, @Nonnull final Runnable task )
  {
    return macroTaskVpu().queue( generateName( "MacroTask", name ), task );
  }

  /**
   * Return the "macro" task VPU.
   *
   * @return the "macro" task VPU.
   * @see #macroTask(Runnable)
   */
  @Nonnull
  public static VirtualProcessorUnit macroTaskVpu()
  {
    return VirtualProcessorUnitsHolder.macroTaskVpu();
  }

  /**
   * Run specified task now by queuing on MacroTask VPU and activating VPU.
   * This is invoked when the browser has triggered a macro task as a result of a callback of some kind.
   * The specified task is added to the start of the macroTask queue but any tasks present on the queue
   * will be invoked after the specified task.
   *
   * @param name A human consumable name for the task. It may be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task the task.
   */
  @VisibleForTesting
  static void becomeMacroTask( @Nullable final String name, @Nonnull final Runnable task )
  {
    if ( shouldCheckApiInvariants() )
    {
      apiInvariant( () -> !isVpuActivated(),
                    () -> "Zemeckis-0012: Zemeckis.becomeMacroTask(...) invoked for the task named '" + name +
                          "' but the VirtualProcessorUnit named '" + currentVpu() + "' is already active" );
    }
    final VirtualProcessorUnit.Executor executor = macroTaskVpu().getExecutor();
    executor.queueNext( name, Objects.requireNonNull( task ) );
    executor.activate();
  }

  /**
   * Queue the task to execute in the next "micro" task.
   * The "micro" tasks are those that the browser executes after the current "macro" or "micro" task.
   * This VPU schedules an activation via a call to {@code Promise.resolve().then( v -> callback() )}.
   *
   * @param task the task.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable microTask( @Nonnull final Runnable task )
  {
    return microTask( null, task );
  }

  /**
   * Queue the task to execute in the next "micro" task.
   * The "micro" tasks are those that the browser executes after the current "macro" or "micro" task.
   * This VPU schedules an activation via a call to {@code Promise.resolve().then( v -> callback() )}.
   *
   * @param name A human consumable name for the task. It may be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task the task.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable microTask( @Nullable final String name, @Nonnull final Runnable task )
  {
    return microTaskVpu().queue( generateName( "MicroTask", name ), task );
  }

  /**
   * Return the "micro" task VPU.
   *
   * @return the "micro" task VPU.
   * @see #microTask(Runnable)
   */
  @Nonnull
  public static VirtualProcessorUnit microTaskVpu()
  {
    return VirtualProcessorUnitsHolder.microTaskVpu();
  }

  /**
   * Queue the task to execute in the next "animationFrame".
   * The "animationFrame" occurs prior to the next render frame.
   * This VPU schedules an activation via a call to <code>requestAnimationFrame( callback )</code>.
   *
   * @param task the task.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable animationFrame( @Nonnull final Runnable task )
  {
    return animationFrame( null, task );
  }

  /**
   * Queue the task to execute in the next "animationFrame".
   * The "animationFrame" occurs prior to the next render frame.
   * This VPU schedules an activation via a call to <code>requestAnimationFrame( callback )</code>.
   *
   * @param name A human consumable name for the task. It may be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task the task.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable animationFrame( @Nullable final String name, @Nonnull final Runnable task )
  {
    return animationFrameVpu().queue( generateName( "AnimationFrameTask", name ), task );
  }

  /**
   * Return the "animationFrame" VPU.
   *
   * @return the "animationFrame" VPU.
   * @see #microTask(Runnable)
   */
  @Nonnull
  public static VirtualProcessorUnit animationFrameVpu()
  {
    return VirtualProcessorUnitsHolder.animationFrameVpu();
  }

  /**
   * Queue the task to execute after the next browser render.
   * The "afterFrame" tasks are invoked after the next frames render by responding to a message on a
   * MessageChannel that is sent in {@code requestAnimationFrame()}.
   *
   * @param task the task.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable afterFrame( @Nonnull final Runnable task )
  {
    return afterFrame( null, task );
  }

  /**
   * Queue the task to execute after the next browser render.
   * The "afterFrame" tasks are invoked after the next frames render by responding to a message on a
   * MessageChannel that is sent in {@code requestAnimationFrame()}.
   *
   * @param name A human consumable name for the task. It may be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task the task.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable afterFrame( @Nullable final String name, @Nonnull final Runnable task )
  {
    return afterFrameVpu().queue( generateName( "AfterFrameTask", name ), task );
  }

  /**
   * Return the "afterFrame" VPU.
   *
   * @return the "afterFrame" VPU.
   * @see #microTask(Runnable)
   */
  @Nonnull
  public static VirtualProcessorUnit afterFrameVpu()
  {
    return VirtualProcessorUnitsHolder.afterFrameVpu();
  }

  /**
   * Queue the task to execute when the browser is idle.
   * The browser activates the onIdle VirtualProcessorUnit when idle and will pass the
   * duration for which the VPU may run. The VPU will execute tasks as long as there is tasks
   * queued and the deadline has not been reached after which the VPU will return control to the
   * browser. Unlike other VPUs, when the onIdle VirtualProcessorUnit completes the activation, there
   * may still be tasks in the queue and if there is the VPU will re-schedule itself for another activation.
   * This VPU schedules an activation via a call to <code>requestIdleCallback( callback )</code>.
   *
   * @param task the task.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable onIdle( @Nonnull final Runnable task )
  {
    return onIdle( null, task );
  }

  /**
   * Queue the task to execute when the browser is idle.
   * The browser activates the onIdle VirtualProcessorUnit when idle and will pass the
   * duration for which the VPU may run. The VPU will execute tasks as long as there is tasks
   * queued and the deadline has not been reached after which the VPU will return control to the
   * browser. Unlike other VPUs, when the onIdle VirtualProcessorUnit completes the activation, there
   * may still be tasks in the queue and if there is the VPU will re-schedule itself for another activation.
   * This VPU schedules an activation via a call to <code>requestIdleCallback( callback )</code>.
   *
   * @param name A human consumable name for the task. It may be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task the task.
   * @return the {@link Cancelable} instance that can be used to cancel execution of the task.
   */
  @Nonnull
  public static Cancelable onIdle( @Nullable final String name, @Nonnull final Runnable task )
  {
    return onIdleVpu().queue( generateName( "OnIdleTask", name ), task );
  }

  /**
   * Return the "onIdle" VPU.
   *
   * @return the "onIdle" VPU.
   * @see #microTask(Runnable)
   */
  @Nonnull
  public static VirtualProcessorUnit onIdleVpu()
  {
    return VirtualProcessorUnitsHolder.onIdleVpu();
  }

  /**
   * Build name for task.
   * If {@link Zemeckis#areNamesEnabled()} returns false then this method will return null, otherwise the specified
   * name will be returned or a name synthesized from the prefix and a running number if no name is specified.
   *
   * @param prefix the prefix used if this method needs to generate name.
   * @param name   the name specified by the user.
   * @return the name.
   */
  @OmitSymbol( unless = "zemeckis.enable_names" )
  @Nullable
  static String generateName( @Nonnull final String prefix, @Nullable final String name )
  {
    return Zemeckis.areNamesEnabled() ?
           null != name ? name : prefix + "@" + IdContainer.c_nextTaskId++ :
           null;
  }

  @TestOnly
  static void reset()
  {
    IdContainer.c_nextTaskId = 0;
  }

  @OmitType( unless = "zemeckis.enable_names" )
  private static final class IdContainer
  {
    /**
     * Id of next node to be created.
     * This is only used if {@link Zemeckis#areNamesEnabled()} returns true but no name has been supplied.
     */
    @OmitSymbol( unless = "zemeckis.enable_names" )
    private static int c_nextTaskId = 1;

  }
}
