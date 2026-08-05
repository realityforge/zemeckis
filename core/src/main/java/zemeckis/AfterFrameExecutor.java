package zemeckis;

import grim.annotations.OmitType;

/**
 * Run tasks after the next browser render frame.
 * The is inspired by techniques described in Nolan Lawson's
 * <a href="https://nolanlawson.com/2018/09/25/accurately-measuring-layout-on-the-web/">blog post</a>,
 * implemented in <a href="https://github.com/andrewiggins/afterframe">AfterFrame</a>, react's scheduler
 * and <a href="https://mobile.twitter.com/_developit/status/1081681351122829325">tweeted</a> about by
 * developit.
 */
@OmitType(when = "zemeckis.use_test_scheduler")
final class AfterFrameExecutor extends RoundBasedExecutor {
    private final JsRuntime.MessageChannel _channel = new JsRuntime.MessageChannel();

    AfterFrameExecutor() {
        _channel.port1().setOnmessage(m -> activate());
    }

    @Override
    void scheduleForActivation() {
        Zemeckis.animationFrame(() -> _channel.port2().postMessage(null));
    }
}
