package zemeckis;


/**
 * Interface defining handler invoked when an unexpected error occurs.
 */
@FunctionalInterface
public interface UncaughtErrorHandler
{
  /**
   * Callback invoked when an unexpected error occurs.
   *
   * @param error the exception.
   */
  void onUncaughtError( Throwable error );
}
