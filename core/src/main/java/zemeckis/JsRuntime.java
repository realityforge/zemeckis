package zemeckis;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import org.jspecify.annotations.Nullable;

final class JsRuntime {
    private JsRuntime() {}

    @JsMethod(namespace = JsPackage.GLOBAL, name = "clearInterval")
    static native void clearInterval(int id);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "clearTimeout")
    static native void clearTimeout(int id);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "console.log")
    static native void log(Object message);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "requestAnimationFrame")
    static native int requestAnimationFrame(AnimationFrameCallback callback);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "requestIdleCallback")
    static native int requestIdleCallback(IdleRequestCallback callback);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "setInterval")
    static native int setInterval(TimerHandler handler, int timeout);

    @JsMethod(namespace = JsPackage.GLOBAL, name = "setTimeout")
    static native int setTimeout(TimerHandler handler, int timeout);

    static WorkerOptions workerOptions(final String name) {
        final WorkerOptions options = Js.uncheckedCast(JsPropertyMap.of());
        options.setName(name);
        return options;
    }

    @FunctionalInterface
    @JsFunction
    interface AnimationFrameCallback {
        void onInvoke(double time);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Blob")
    static final class Blob {
        Blob(final String[] blobParts) {}
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "IdleDeadline")
    static final class IdleDeadline {
        native double timeRemaining();
    }

    @FunctionalInterface
    @JsFunction
    interface IdleRequestCallback {
        void onInvoke(IdleDeadline deadline);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "MessageChannel")
    static final class MessageChannel {
        MessageChannel() {}

        @JsProperty(name = "port1")
        native MessagePort port1();

        @JsProperty(name = "port2")
        native MessagePort port2();
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "MessageEvent")
    static final class MessageEvent {
        @JsProperty(name = "data")
        native Any data();
    }

    @FunctionalInterface
    @JsFunction
    interface MessageEventHandler {
        void onInvoke(MessageEvent event);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "MessagePort")
    static final class MessagePort {
        private MessagePort() {}

        native void postMessage(@Nullable Object message);

        @JsProperty(name = "onmessage")
        native void setOnmessage(MessageEventHandler handler);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Promise")
    static final class Promise {
        static native Promise resolve(@Nullable Object value);

        @JsMethod(name = "then")
        native Promise thenAccept(PromiseCallback callback);
    }

    @FunctionalInterface
    @JsFunction
    interface PromiseCallback {
        void onInvoke(@Nullable Object value);
    }

    @FunctionalInterface
    @JsFunction
    interface TimerHandler {
        void onInvoke();
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "URL")
    static final class URL {
        private URL() {}

        static native String createObjectURL(Blob object);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Worker")
    static final class Worker {
        Worker(final String scriptURL, final WorkerOptions options) {}

        native void postMessage(Object message);

        @JsProperty(name = "onmessage")
        native void setOnmessage(MessageEventHandler handler);

        native void terminate();
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "WorkerOptions")
    interface WorkerOptions {
        @JsProperty
        void setName(String name);
    }
}
