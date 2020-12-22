package zemeckis;

import javax.annotation.Nonnull;
import org.realityforge.braincheck.BrainCheckConfig;

/**
 * Provide access to global configuration settings.
 */
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
}
