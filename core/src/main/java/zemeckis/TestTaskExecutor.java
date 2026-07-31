package zemeckis;

/**
 * Run tasks through the deterministic temporal scheduler.
 */
final class TestTaskExecutor extends RoundBasedExecutor {
    @Override
    void scheduleForActivation() {
        TemporalScheduler.delayedTask(Zemeckis.areNamesEnabled() ? "TestTaskExecutor" : null, this::activate, 0);
    }
}
