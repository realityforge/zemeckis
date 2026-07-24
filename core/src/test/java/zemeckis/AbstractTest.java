package zemeckis;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.realityforge.braincheck.BrainCheckTestUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(MessageCollector.class)
public abstract class AbstractTest {
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random c_random = new Random();
    private final TestLogger _logger = new TestLogger();
    private final List<Throwable> _uncaughtExceptions = new ArrayList<>();
    private boolean _allowUncaughtExceptions;

    @BeforeMethod
    protected void beforeTest() {
        BrainCheckTestUtil.resetConfig(false);
        ZemeckisTestUtil.resetConfig(false);
        _logger.getEntries().clear();
        _uncaughtExceptions.clear();
        Zemeckis.addUncaughtErrorHandler(_uncaughtExceptions::add);
        ZemeckisTestUtil.setLogger(_logger);
    }

    @AfterMethod
    protected void afterTest() {
        if (!_allowUncaughtExceptions && !_uncaughtExceptions.isEmpty()) {
            _uncaughtExceptions.forEach(Throwable::printStackTrace);
            fail("Uncaught exceptions executing tasks");
        }
        BrainCheckTestUtil.resetConfig(true);
        ZemeckisTestUtil.resetConfig(true);
    }

    protected final void allowUncaughtExceptions() {
        _allowUncaughtExceptions = true;
    }

    protected final TestLogger getTestLogger() {
        return _logger;
    }

    protected final void assertInvariantFailure(final ThrowingRunnable throwingRunnable, final String message) {
        assertEquals(expectThrows(IllegalStateException.class, throwingRunnable).getMessage(), message);
    }

    protected final void assertDefaultToStringWhenNamesDisabled(final Object object) {
        ZemeckisTestUtil.disableNames();
        assertDefaultToString(object);
        ZemeckisTestUtil.enableNames();
    }

    protected final void assertDefaultToString(final Object object) {
        assertEquals(object.toString(), object.getClass().getName() + "@" + Integer.toHexString(object.hashCode()));
    }

    protected final int randomInt() {
        return getRandom().nextInt();
    }

    protected final Random getRandom() {
        return c_random;
    }

    protected final String randomString() {
        return randomString(12);
    }

    @SuppressWarnings("SameParameterValue")
    protected final String randomString(final int stringLength) {
        final StringBuilder sb = new StringBuilder(stringLength);
        for (int i = 0; i < stringLength; i++) {
            sb.append(CHARS.charAt(Math.abs(randomInt() % CHARS.length())));
        }
        return sb.toString();
    }
}
