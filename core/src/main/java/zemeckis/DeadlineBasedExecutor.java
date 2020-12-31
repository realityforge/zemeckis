package zemeckis;

import javax.annotation.Nullable;
import jsinterop.annotations.JsFunction;

/**
 * This executor runs tasks until a deadline has been reached.
 */
abstract class DeadlineBasedExecutor
  extends AbstractExecutor
{
  /**
   * The minimum time required to run a task.
   */
  private static final int MIN_TASK_TIME = 1;

  @FunctionalInterface
  @JsFunction
  interface DeadlineFunction
  {
    int getTimeRemaining();
  }

  /**
   * Returns true if the executor should yield and return control to invoker.
   *
   * @param function the function that specifies deadline, if any. If null the deadline is considered to have passed.
   * @return true to yield to caller, false to continue executing tasks.
   */
  private boolean shouldYield( @Nullable final DeadlineFunction function )
  {
    return null == function || function.getTimeRemaining() < MIN_TASK_TIME;
  }

  @Override
  public void activate()
  {
    context().activate( () -> executeTasks( null ) );
  }

  /**
   * Run tasks until deadline exceeded or all tasks completed.
   *
   * @param function the function that specifies deadline, if any. If null the deadline is considered to have passed.
   */
  void executeTasks( @Nullable final DeadlineFunction function )
  {
    int queueSize;
    while ( 0 != ( queueSize = getQueueSize() ) && !shouldYield( function ) )
    {
      executeNextTask();
    }
    if ( 0 != queueSize )
    {
      scheduleForActivation();
    }
  }
}
