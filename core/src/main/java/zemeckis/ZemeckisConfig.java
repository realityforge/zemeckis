package zemeckis;

import grim.annotations.OmitType;

/**
 * Location of all compile time configuration settings for framework.
 */
@SuppressWarnings({"ConstantField", "FieldCanBeFinal"})
@OmitType
final class ZemeckisConfig {
    private static final ConfigProvider PROVIDER = new ConfigProvider();
    private static final boolean PRODUCTION_MODE = PROVIDER.isProductionMode();
    private static boolean ENABLE_NAMES = PROVIDER.areNamesEnabled();
    private static boolean PURGE_ON_RUNAWAY = PROVIDER.purgeTasksWhenRunawayDetected();
    private static boolean UNCAUGHT_ERROR_HANDLERS = PROVIDER.areUncaughtErrorHandlersEnabled();
    private static final boolean USE_TEST_SCHEDULER = PROVIDER.useTestScheduler();
    private static final boolean USE_MESSAGE_CHANNEL_TO_SCHEDULE_TASKS = PROVIDER.useMessageChannelToScheduleTasks();
    private static final boolean USE_WORKER_TO_SCHEDULE_DELAYED_TASKS = PROVIDER.useWorkerToScheduleDelayedTasks();
    private static final boolean LOG_WORKER_INTERACTIONS = PROVIDER.shouldLogWorkerInteractions();
    private static final String LOGGER_TYPE = PROVIDER.loggerType();

    private ZemeckisConfig() {}

    static boolean isDevelopmentMode() {
        return !isProductionMode();
    }

    static boolean isProductionMode() {
        return PRODUCTION_MODE;
    }

    static boolean useTestScheduler() {
        return USE_TEST_SCHEDULER;
    }

    static boolean purgeTasksWhenRunawayDetected() {
        return PURGE_ON_RUNAWAY;
    }

    static void setPurgeOnRunaway(final boolean purgeOnRunaway) {
        PURGE_ON_RUNAWAY = purgeOnRunaway;
    }

    static boolean areNamesEnabled() {
        return ENABLE_NAMES;
    }

    static void setEnableNames(final boolean enableNames) {
        ENABLE_NAMES = enableNames;
    }

    static boolean areUncaughtErrorHandlersEnabled() {
        return UNCAUGHT_ERROR_HANDLERS;
    }

    static void setEnableUncaughtErrorHandlers(final boolean enableUncaughtErrorHandlers) {
        UNCAUGHT_ERROR_HANDLERS = enableUncaughtErrorHandlers;
    }

    static boolean useMessageChannelToScheduleTasks() {
        return USE_MESSAGE_CHANNEL_TO_SCHEDULE_TASKS;
    }

    static boolean useWorkerToScheduleDelayedTasks() {
        return USE_WORKER_TO_SCHEDULE_DELAYED_TASKS;
    }

    static boolean shouldLogWorkerInteractions() {
        return LOG_WORKER_INTERACTIONS;
    }

    static String loggerType() {
        return LOGGER_TYPE;
    }

    private static final class ConfigProvider extends AbstractConfigProvider {
        @GwtIncompatible
        @Override
        boolean isProductionMode() {
            return "production".equals(System.getProperty("zemeckis.environment", "production"));
        }

        @GwtIncompatible
        @Override
        boolean areNamesEnabled() {
            return "true".equals(System.getProperty("zemeckis.enable_names", isProductionMode() ? "false" : "true"));
        }

        @GwtIncompatible
        @Override
        boolean purgeTasksWhenRunawayDetected() {
            return "true".equals(System.getProperty("zemeckis.purge_tasks_when_runaway_detected", "true"));
        }

        @GwtIncompatible
        @Override
        boolean areUncaughtErrorHandlersEnabled() {
            return "true"
                    .equals(System.getProperty(
                            "zemeckis.enable_uncaught_error_handlers", PRODUCTION_MODE ? "false" : "true"));
        }

        @GwtIncompatible
        @Override
        boolean useTestScheduler() {
            return true;
        }

        @GwtIncompatible
        @Override
        boolean useMessageChannelToScheduleTasks() {
            return "true".equals(System.getProperty("zemeckis.use_message_channel_to_schedule_tasks", "true"));
        }

        @GwtIncompatible
        @Override
        boolean useWorkerToScheduleDelayedTasks() {
            return "true".equals(System.getProperty("zemeckis.use_worker_to_schedule_delayed_tasks", "true"));
        }

        @Override
        boolean shouldLogWorkerInteractions() {
            return "true".equals(System.getProperty("zemeckis.log_worker_interactions", "false"));
        }

        @GwtIncompatible
        @Override
        String loggerType() {
            return System.getProperty("zemeckis.logger", PRODUCTION_MODE ? "basic" : "proxy");
        }
    }

    @SuppressWarnings({"unused", "StringEquality"})
    private abstract static class AbstractConfigProvider {
        boolean isProductionMode() {
            return "production" == System.getProperty("zemeckis.environment");
        }

        boolean areNamesEnabled() {
            return "true" == System.getProperty("zemeckis.enable_names");
        }

        boolean purgeTasksWhenRunawayDetected() {
            return "true" == System.getProperty("zemeckis.purge_tasks_when_runaway_detected");
        }

        boolean areUncaughtErrorHandlersEnabled() {
            return "true" == System.getProperty("zemeckis.enable_uncaught_error_handlers");
        }

        boolean useTestScheduler() {
            return "true" == System.getProperty("zemeckis.use_test_scheduler");
        }

        boolean useMessageChannelToScheduleTasks() {
            return "true" == System.getProperty("zemeckis.use_message_channel_to_schedule_tasks");
        }

        boolean useWorkerToScheduleDelayedTasks() {
            return "true" == System.getProperty("zemeckis.use_worker_to_schedule_delayed_tasks");
        }

        boolean shouldLogWorkerInteractions() {
            return "true" == System.getProperty("zemeckis.log_worker_interactions");
        }

        String loggerType() {
            /*
             * Valid values are: "none", "console" and "proxy" (for testing)
             */
            return System.getProperty("zemeckis.logger");
        }
    }
}
