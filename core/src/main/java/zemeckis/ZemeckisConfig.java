package zemeckis;

import javax.annotation.Nonnull;

/**
 * Location of all compile time configuration settings for framework.
 */
final class ZemeckisConfig
{
  @Nonnull
  private static final ConfigProvider PROVIDER = new ConfigProvider();
  private static final boolean PRODUCTION_MODE = PROVIDER.isProductionMode();
  private static boolean ENABLE_NAMES = PROVIDER.areNamesEnabled();
  private static boolean PURGE_ON_RUNAWAY = PROVIDER.purgeTasksWhenRunawayDetected();
  private static boolean UNCAUGHT_ERROR_HANDLERS = PROVIDER.areUncaughtErrorHandlersEnabled();
  private static final String LOGGER_TYPE = PROVIDER.loggerType();

  private ZemeckisConfig()
  {
  }

  static boolean isDevelopmentMode()
  {
    return !isProductionMode();
  }

  static boolean isProductionMode()
  {
    return PRODUCTION_MODE;
  }

  static boolean isJvm()
  {
    return true;
  }

  static boolean purgeTasksWhenRunawayDetected()
  {
    return PURGE_ON_RUNAWAY;
  }

  static boolean areNamesEnabled()
  {
    return ENABLE_NAMES;
  }

  static boolean areUncaughtErrorHandlersEnabled()
  {
    return UNCAUGHT_ERROR_HANDLERS;
  }

  static String loggerType()
  {
    return LOGGER_TYPE;
  }

  private static final class ConfigProvider
    extends AbstractConfigProvider
  {
    @GwtIncompatible
    @Override
    boolean isProductionMode()
    {
      return "production".equals( System.getProperty( "zemeckis.environment", "production" ) );
    }

    @GwtIncompatible
    @Override
    boolean areNamesEnabled()
    {
      return "true".equals( System.getProperty( "zemeckis.enable_names", isProductionMode() ? "false" : "true" ) );
    }

    @GwtIncompatible
    @Override
    boolean purgeTasksWhenRunawayDetected()
    {
      return "true".equals( System.getProperty( "zemeckis.purge_tasks_when_runaway_detected", "true" ) );
    }

    @GwtIncompatible
    @Override
    boolean areUncaughtErrorHandlersEnabled()
    {
      return "true".equals( System.getProperty( "zemeckis.enable_uncaught_error_handlers",
                                                PRODUCTION_MODE ? "false" : "true" ) );
    }

    @GwtIncompatible
    @Override
    String loggerType()
    {
      return System.getProperty( "zemeckis.logger", PRODUCTION_MODE ? "basic" : "proxy" );
    }
  }

  @SuppressWarnings( { "unused", "StringEquality" } )
  private static abstract class AbstractConfigProvider
  {
    boolean isProductionMode()
    {
      return "production" == System.getProperty( "zemeckis.environment" );
    }

    boolean areNamesEnabled()
    {
      return "true" == System.getProperty( "zemeckis.enable_names" );
    }

    boolean purgeTasksWhenRunawayDetected()
    {
      return "true" == System.getProperty( "zemeckis.purge_tasks_when_runaway_detected" );
    }

    boolean areUncaughtErrorHandlersEnabled()
    {
      return "true" == System.getProperty( "zemeckis.enable_uncaught_error_handlers" );
    }

    String loggerType()
    {
      /*
       * Valid values are: "none", "console" and "proxy" (for testing)
       */
      return System.getProperty( "zemeckis.logger" );
    }
  }
}
