package zemeckis;

import grim.annotations.OmitType;
import javax.annotation.Nonnull;

/**
 * Location of all compile time configuration settings for framework.
 */
@SuppressWarnings( "FieldMayBeFinal" )
@OmitType
final class ZemeckisConfig
{
  @Nonnull
  private static final ConfigProvider PROVIDER = new ConfigProvider();
  private static final boolean PRODUCTION_MODE = PROVIDER.isProductionMode();
  private static boolean ENABLE_NAMES = PROVIDER.areNamesEnabled();
  private static boolean PURGE_ON_RUNAWAY = PROVIDER.purgeTasksWhenRunawayDetected();
  private static boolean UNCAUGHT_ERROR_HANDLERS = PROVIDER.areUncaughtErrorHandlersEnabled();
  private static boolean USE_MESSAGE_CHANNEL_TO_SCHEDULE_TASKS = PROVIDER.useMessageChannelToScheduleTasks();
  private static boolean USE_WORKER_TO_SCHEDULE_DELAYED_TASKS = PROVIDER.useWorkerToScheduleDelayedTasks();
  private static final boolean LOG_WORKER_INTERACTIONS = PROVIDER.shouldLogWorkerInteractions();
  @Nonnull
  private static final String LOGGER_TYPE = PROVIDER.loggerType();
  private static final boolean JVM = PROVIDER.isJvm();

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
    return JVM;
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

  static boolean useMessageChannelToScheduleTasks()
  {
    return USE_MESSAGE_CHANNEL_TO_SCHEDULE_TASKS;
  }

  static boolean useWorkerToScheduleDelayedTasks()
  {
    return USE_WORKER_TO_SCHEDULE_DELAYED_TASKS;
  }

  static boolean shouldLogWorkerInteractions()
  {
    return LOG_WORKER_INTERACTIONS;
  }

  @Nonnull
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
    boolean useMessageChannelToScheduleTasks()
    {
      return "true".equals( System.getProperty( "zemeckis.use_message_channel_to_schedule_tasks", "true" ) );
    }

    @GwtIncompatible
    @Override
    boolean useWorkerToScheduleDelayedTasks()
    {
      return "true".equals( System.getProperty( "zemeckis.use_worker_to_schedule_delayed_tasks", "true" ) );
    }

    boolean shouldLogWorkerInteractions()
    {
      return "true".equals( System.getProperty( "zemeckis.log_worker_interactions", "false" ) );
    }

    @GwtIncompatible
    @Nonnull
    @Override
    String loggerType()
    {
      return System.getProperty( "zemeckis.logger", PRODUCTION_MODE ? "basic" : "proxy" );
    }

    @GwtIncompatible
    @Override
    boolean isJvm()
    {
      return true;
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

    boolean useMessageChannelToScheduleTasks()
    {
      return "true" == System.getProperty( "zemeckis.use_message_channel_to_schedule_tasks" );
    }

    boolean useWorkerToScheduleDelayedTasks()
    {
      return "true" == System.getProperty( "zemeckis.use_worker_to_schedule_delayed_tasks" );
    }

    boolean shouldLogWorkerInteractions()
    {
      return "true" == System.getProperty( "zemeckis.log_worker_interactions" );
    }

    @Nonnull
    String loggerType()
    {
      /*
       * Valid values are: "none", "console" and "proxy" (for testing)
       */
      return System.getProperty( "zemeckis.logger" );
    }

    boolean isJvm()
    {
      return false;
    }
  }
}
