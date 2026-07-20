package zemeckis.test;

import static org.testng.Assert.*;

import org.testng.annotations.Test;
import zemeckis.AbstractTest;
import zemeckis.Zemeckis;
import zemeckis.ZemeckisTestUtil;

public class ZemeckisTestUtilTest extends AbstractTest {
    @Test
    public void areNamesEnabled() {
        ZemeckisTestUtil.disableNames();
        assertFalse(Zemeckis.areNamesEnabled());
        ZemeckisTestUtil.enableNames();
        assertTrue(Zemeckis.areNamesEnabled());
    }

    @Test
    public void purgeTasksWhenRunawayDetected() {
        ZemeckisTestUtil.noPurgeTasksWhenRunawayDetected();
        assertFalse(Zemeckis.purgeTasksWhenRunawayDetected());
        ZemeckisTestUtil.purgeTasksWhenRunawayDetected();
        assertTrue(Zemeckis.purgeTasksWhenRunawayDetected());
    }

    @Test
    public void areUncaughtErrorHandlersEnabled() {
        ZemeckisTestUtil.disableUncaughtErrorHandlers();
        assertFalse(Zemeckis.areUncaughtErrorHandlersEnabled());
        ZemeckisTestUtil.enableUncaughtErrorHandlers();
        assertTrue(Zemeckis.areUncaughtErrorHandlersEnabled());
    }
}
