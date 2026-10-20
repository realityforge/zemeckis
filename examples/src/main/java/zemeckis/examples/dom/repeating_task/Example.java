package zemeckis.examples.dom.repeating_task;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import zemeckis.Zemeckis;

public final class Example {
    private Example() {}

    private static final String[] COLORS = {"color: #006AEB;", "color: #c143eb;"};

    @JsMethod
    public static void main() {
        new Timer(1, 2000);
        new Timer(2, 10000);
    }

    private static final class Timer {
        private final int _id;
        private final int _period;
        private int _lastTime = -1;

        Timer(final int id, final int period) {
            _id = id;
            _period = period;
            Zemeckis.periodicTask(this::tick, _period);
        }

        void tick() {
            final int now = Zemeckis.now();
            final int delta = -1 == _lastTime ? 0 : now - _lastTime;
            _lastTime = now;
            logToConsole("%c task " + _id + " Period=" + _period + " Delta=" + delta, COLORS[_id % COLORS.length]);
        }
    }

    @JsMethod(namespace = JsPackage.GLOBAL, name = "console.log")
    private static native void logToConsole(String message, String style);
}
