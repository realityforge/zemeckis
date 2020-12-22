package zemeckis;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.realityforge.braincheck.BrainCheckConfig;
import static org.realityforge.braincheck.Guards.*;

/**
 * Class supporting the propagation of errors for UncaughtErrorHandler callback to multiple error handlers.
 */
final class UncaughtErrorHandlerSupport
  implements UncaughtErrorHandler
{
  @Nonnull
  private static UncaughtErrorHandlerSupport INSTANCE = new UncaughtErrorHandlerSupport();
  /**
   * The list of error handlers to call when an error is received.
   */
  @Nonnull
  private final List<UncaughtErrorHandler> _errorHandlers = new ArrayList<>();

  @Nonnull
  static UncaughtErrorHandlerSupport get()
  {
    if ( Zemeckis.shouldCheckInvariants() )
    {
      invariant( Zemeckis::areUncaughtErrorHandlersEnabled,
                 () -> "Zemeckis-0011: UncaughtErrorHandlerSupport.get() invoked when Zemeckis.areUncaughtErrorHandlersEnabled() returns false." );
    }
    return INSTANCE;
  }

  static void reset()
  {
    INSTANCE = new UncaughtErrorHandlerSupport();
  }

  private UncaughtErrorHandlerSupport()
  {
  }

  /**
   * Add error handler to the list of error handlers called.
   * The handler should not already be in the list.
   *
   * @param handler the error handler.
   */
  void addUncaughtErrorHandler( @Nonnull final UncaughtErrorHandler handler )
  {
    if ( Zemeckis.shouldCheckApiInvariants() )
    {
      apiInvariant( () -> !_errorHandlers.contains( handler ),
                    () -> "Zemeckis-0006: Attempting to add handler " + handler + " that is already in " +
                          "the list of error handlers." );
    }
    _errorHandlers.add( Objects.requireNonNull( handler ) );
  }

  /**
   * Remove error handler from list of existing error handlers.
   * The handler should already be in the list.
   *
   * @param handler the error handler.
   */
  void removeUncaughtErrorHandler( @Nonnull final UncaughtErrorHandler handler )
  {
    if ( Zemeckis.shouldCheckApiInvariants() )
    {
      apiInvariant( () -> _errorHandlers.contains( handler ),
                    () -> "Zemeckis-0007: Attempting to remove handler " + handler + " that is not in " +
                          "the list of error handlers." );
    }
    _errorHandlers.remove( Objects.requireNonNull( handler ) );
  }

  @Override
  public void onUncaughtError( @Nonnull final Throwable error )
  {
    for ( final UncaughtErrorHandler errorHandler : _errorHandlers )
    {
      try
      {
        errorHandler.onUncaughtError( error );
      }
      catch ( final Throwable nestedError )
      {
        if ( Zemeckis.areNamesEnabled() && BrainCheckConfig.verboseErrorMessages() )
        {
          final String message =
            ZemeckisUtil.safeGetString( () -> "Exception when notifying error handler '" + errorHandler +
                                              "' of '" + error + "' error." );
          ZemeckisLogger.log( message, nestedError );
        }
        else
        {
          ZemeckisLogger.log( "Error triggered when invoking UncaughtErrorHandler.onUncaughtError()", nestedError );
        }
      }
    }
  }
}
