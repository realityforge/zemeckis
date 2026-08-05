package zemeckis;

import grim.annotations.OmitType;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Run tasks in next MacroTask.
 */
@OmitType(when = "zemeckis.use_test_scheduler")
final class MacroTaskExecutor extends RoundBasedExecutor {
    private final JsRuntime.@Nullable MessageChannel _channel =
            Zemeckis.useMessageChannelToScheduleTasks() ? new JsRuntime.MessageChannel() : null;

    MacroTaskExecutor() {
        if (Zemeckis.useMessageChannelToScheduleTasks()) {
            channel().port1().setOnmessage(m -> activate());
        }
    }

    @Override
    void scheduleForActivation() {
        if (Zemeckis.useMessageChannelToScheduleTasks()) {
            channel().port2().postMessage(null);
        } else {
            TemporalScheduler.delayedTask(Zemeckis.areNamesEnabled() ? "MacroTaskExecutor" : null, this::activate, 0);
        }
    }

    private JsRuntime.MessageChannel channel() {
        return Objects.requireNonNull(_channel);
    }
}
