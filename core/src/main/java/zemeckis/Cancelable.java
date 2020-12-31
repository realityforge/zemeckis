package zemeckis;

/**
 * A task that can be cancelled.
 */
public interface Cancelable
{
  /**
   * Cancel the task.
   */
  void cancel();
}
