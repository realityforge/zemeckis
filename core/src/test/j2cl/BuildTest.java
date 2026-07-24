package zemeckis.j2cl;

import jsinterop.annotations.JsType;
import zemeckis.Zemeckis;
import zemeckis.ZemeckisTestUtil;

@JsType(namespace = "zemeckis.j2cl")
public final class BuildTest {
    private BuildTest() {}

    public static void scheduleTask() {
        Zemeckis.delayedTask(() -> {}, 1);
    }

    public static int pumpAll() {
        return ZemeckisTestUtil.pumpAll();
    }

    public static int scheduleAndPump() {
        final int[] executions = {0};
        Zemeckis.delayedTask(() -> executions[0]++, 1);
        Zemeckis.macroTask(() -> executions[0]++);
        Zemeckis.microTask(() -> executions[0]++);
        Zemeckis.animationFrame(() -> executions[0]++);
        Zemeckis.afterFrame(() -> executions[0]++);
        Zemeckis.onIdle(() -> executions[0]++);
        return 10 * ZemeckisTestUtil.pumpAll() + executions[0];
    }
}
