package zemeckis;

import java.util.ArrayList;
import org.jspecify.annotations.Nullable;

public final class TestLogger implements ZemeckisTestUtil.Logger {
    public static final class LogEntry {
        private final String _message;

        @Nullable
        private final Throwable _throwable;

        LogEntry(final String message, @Nullable final Throwable throwable) {
            _message = message;
            _throwable = throwable;
        }

        public String getMessage() {
            return _message;
        }

        @Nullable
        public Throwable getThrowable() {
            return _throwable;
        }
    }

    private final ArrayList<LogEntry> _entries = new ArrayList<>();

    @Override
    public void log(final String message, @Nullable final Throwable throwable) {
        _entries.add(new LogEntry(message, throwable));
    }

    public ArrayList<LogEntry> getEntries() {
        return _entries;
    }
}
