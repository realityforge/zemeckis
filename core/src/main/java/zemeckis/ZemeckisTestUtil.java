package zemeckis;

import grim.annotations.OmitSymbol;
import org.jetbrains.annotations.TestOnly;
import org.jspecify.annotations.Nullable;

/**
 * Utility class for interacting with Zemeckis config settings in tests.
 */
@TestOnly
public final class ZemeckisTestUtil {
    private ZemeckisTestUtil() {}

    /**
     * Interface to intercept log messages emitted by runtime.
     */
    public interface Logger {
        /**
         * Log message and exception.
         *
         * @param message the message.
         * @param throwable the exception or null.
         */
        void log(String message, @Nullable Throwable throwable);
    }

    /**
     * Reset the state of zemeckis config to either production or development state.
     *
     * @param productionMode true to set it to production mode configuration, false to set it to development mode config.
     */
    @SuppressWarnings("ConstantConditions")
    public static void resetConfig(final boolean productionMode) {
        if (ZemeckisConfig.isProductionMode()) {
            /*
             * This should really never happen but if it does add assertion (so code stops in debugger) or
             * failing that throw an exception.
             */
            assert !ZemeckisConfig.isProductionMode();
            throw new IllegalStateException("Unable to reset config as Zemeckis is in production mode");
        }

        if (productionMode) {
            disableNames();
            disableUncaughtErrorHandlers();
        } else {
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
    private static void resetState() {
        TemporalScheduler.reset();
        setLogger(null);
        Zemeckis.reset();
        UncaughtErrorHandlerSupport.reset();
        VirtualProcessorUnitsHolder.reset();
    }

    /**
     * Specify logger to use to capture logging in tests
     *
     * @param logger the logger.
     */
    public static void setLogger(@Nullable final Logger logger) {
        /*
         * This should really never happen but if it does add assertion (so code stops in debugger).
         */
        assert !ZemeckisConfig.isProductionMode();
        if (ZemeckisConfig.isDevelopmentMode()) {
            final ZemeckisLogger.ProxyLogger proxyLogger = (ZemeckisLogger.ProxyLogger) ZemeckisLogger.getLogger();
            proxyLogger.setLogger(null == logger ? null : logger::log);
        }
    }

    /**
     * Set `zemeckis.enable_names` setting to true.
     */
    public static void enableNames() {
        ZemeckisConfig.setEnableNames(true);
    }

    /**
     * Set `zemeckis.enable_names` setting to false.
     */
    public static void disableNames() {
        ZemeckisConfig.setEnableNames(false);
    }

    /**
     * Set `zemeckis.purge_tasks_when_runaway_detected` setting to true.
     */
    public static void purgeTasksWhenRunawayDetected() {
        ZemeckisConfig.setPurgeOnRunaway(true);
    }

    /**
     * Set `zemeckis.purge_tasks_when_runaway_detected` setting to false.
     */
    public static void noPurgeTasksWhenRunawayDetected() {
        ZemeckisConfig.setPurgeOnRunaway(false);
    }

    /**
     * Set `zemeckis.enable_uncaught_error_handlers` setting to true.
     */
    public static void enableUncaughtErrorHandlers() {
        ZemeckisConfig.setEnableUncaughtErrorHandlers(true);
    }

    /**
     * Set `zemeckis.enable_uncaught_error_handlers` setting to false.
     */
    public static void disableUncaughtErrorHandlers() {
        ZemeckisConfig.setEnableUncaughtErrorHandlers(false);
    }

    /**
     * Execute the next task scheduled by the deterministic test scheduler, advancing its clock to the task's due time.
     *
     * @return true if a task was executed, false if no task was scheduled.
     * @throws IllegalStateException if the deterministic test scheduler is not enabled.
     */
    @OmitSymbol(unless = "zemeckis.use_test_scheduler")
    public static boolean pumpNext() {
        return TemporalScheduler.pumpNext();
    }

    /**
     * Execute scheduled JVM tasks until no tasks remain.
     *
     * @return the number of tasks executed.
     * @throws IllegalStateException if the deterministic test scheduler is not enabled or does not become empty after
     *     executing 10,000 tasks.
     */
    @OmitSymbol(unless = "zemeckis.use_test_scheduler")
    public static int pumpAll() {
        return TemporalScheduler.pumpAll();
    }
}
