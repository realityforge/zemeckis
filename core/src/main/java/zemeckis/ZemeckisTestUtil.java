package zemeckis;

import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.TestOnly;

/**
 * Utility class for interacting with Zemeckis config settings in tests.
 */
@GwtIncompatible
@TestOnly
public final class ZemeckisTestUtil
{
  private ZemeckisTestUtil()
  {
  }

  /**
   * Interface to intercept log messages emitted by runtime.
   */
  public interface Logger
  {
    void log( @Nonnull String message, @Nullable Throwable throwable );
  }

  /**
   * Reset the state of zemeckis config to either production or development state.
   *
   * @param productionMode true to set it to production mode configuration, false to set it to development mode config.
   */
  @SuppressWarnings( "ConstantConditions" )
  public static void resetConfig( final boolean productionMode )
  {
    if ( ZemeckisConfig.isProductionMode() )
    {
      /*
       * This should really never happen but if it does add assertion (so code stops in debugger) or
       * failing that throw an exception.
       */
      assert !ZemeckisConfig.isProductionMode();
      throw new IllegalStateException( "Unable to reset config as Zemeckis is in production mode" );
    }

    if ( productionMode )
    {
      disableNames();
      disableUncaughtErrorHandlers();
    }
    else
    {
      enableNames();
      enableUncaughtErrorHandlers();
    }
    purgeTasksWhenRunawayDetected();
    resetState();
  }

  /**
   * Reset the state of Zemeckis.
   * This occasionally needs to be invoked after changing configuration settings in tests.
   */
  private static void resetState()
  {
    setLogger( null );
    TemporalScheduler.reset();
    UncaughtErrorHandlerSupport.reset();
  }

  /**
   * Specify logger to use to capture logging in tests
   *
   * @param logger the logger.
   */
  public static void setLogger( @Nullable final Logger logger )
  {
    /*
     * This should really never happen but if it does add assertion (so code stops in debugger).
     */
    assert !ZemeckisConfig.isProductionMode();
    if ( ZemeckisConfig.isDevelopmentMode() )
    {
      final ZemeckisLogger.ProxyLogger proxyLogger = (ZemeckisLogger.ProxyLogger) ZemeckisLogger.getLogger();
      proxyLogger.setLogger( null == logger ? null : logger::log );
    }
  }

  /**
   * Set `zemeckis.enable_names` setting to true.
   */
  public static void enableNames()
  {
    setEnableNames( true );
  }

  /**
   * Set `zemeckis.enable_names` setting to false.
   */
  public static void disableNames()
  {
    setEnableNames( false );
  }

  /**
   * Configure the `zemeckis.enable_names` setting.
   *
   * @param value the setting.
   */
  private static void setEnableNames( final boolean value )
  {
    setConstant( "ENABLE_NAMES", value );
  }

  /**
   * Set `zemeckis.purge_tasks_when_runaway_detected` setting to true.
   */
  public static void purgeTasksWhenRunawayDetected()
  {
    setPurgeTasksWhenRunawayDetected( true );
  }

  /**
   * Set `zemeckis.purge_tasks_when_runaway_detected` setting to false.
   */
  public static void noPurgeTasksWhenRunawayDetected()
  {
    setPurgeTasksWhenRunawayDetected( false );
  }

  /**
   * Configure the `zemeckis.purge_tasks_when_runaway_detected` setting.
   *
   * @param value the setting.
   */
  private static void setPurgeTasksWhenRunawayDetected( final boolean value )
  {
    setConstant( "PURGE_ON_RUNAWAY", value );
  }

  /**
   * Set `zemeckis.enable_uncaught_error_handlers` setting to true.
   */
  public static void enableUncaughtErrorHandlers()
  {
    setEnableUncaughtErrorHandlers( true );
  }

  /**
   * Set `zemeckis.enable_uncaught_error_handlers` setting to false.
   */
  public static void disableUncaughtErrorHandlers()
  {
    setEnableUncaughtErrorHandlers( false );
  }

  /**
   * Configure the `zemeckis.enable_uncaught_error_handlers` setting.
   *
   * @param value the setting.
   */
  private static void setEnableUncaughtErrorHandlers( final boolean value )
  {
    setConstant( "UNCAUGHT_ERROR_HANDLERS", value );
  }

  /**
   * Set the specified field name on ZemeckisConfig.
   */
  @SuppressWarnings( "ConstantConditions" )
  private static void setConstant( @Nonnull final String fieldName, final boolean value )
  {
    if ( !ZemeckisConfig.isProductionMode() )
    {
      try
      {
        final Field field = ZemeckisConfig.class.getDeclaredField( fieldName );
        field.setAccessible( true );
        field.set( null, value );
      }
      catch ( final NoSuchFieldException | IllegalAccessException e )
      {
        throw new IllegalStateException( "Unable to change constant " + fieldName, e );
      }
    }
    else
    {
      /*
       * This should not happen but if it does then just fail with an assertion or error.
       */
      assert !ZemeckisConfig.isProductionMode();
      throw new IllegalStateException( "Unable to change constant " +
                                       fieldName +
                                       " as Zemeckis is in production mode" );
    }
  }
}
