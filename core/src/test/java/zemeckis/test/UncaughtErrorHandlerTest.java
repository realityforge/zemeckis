package zemeckis.test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;
import zemeckis.AbstractTest;
import zemeckis.Zemeckis;
import zemeckis.ZemeckisTestUtil;
import zemeckis.TestLogger;
import zemeckis.UncaughtErrorHandler;
import static org.testng.Assert.*;

public class UncaughtErrorHandlerTest
  extends AbstractTest
{
  @Test
  public void basicOperation()
  {
    allowUncaughtExceptions();
    final Throwable throwable = new IllegalStateException();

    final AtomicInteger callCount = new AtomicInteger();

    final UncaughtErrorHandler handler = ( throwableArg ) -> {
      callCount.incrementAndGet();
      assertEquals( throwableArg, throwable );
    };
    Zemeckis.addUncaughtErrorHandler( handler );

    Zemeckis.reportUncaughtError( throwable );

    assertEquals( callCount.get(), 1 );

    Zemeckis.reportUncaughtError( throwable );

    assertEquals( callCount.get(), 2 );

    Zemeckis.removeUncaughtErrorHandler( handler );

    Zemeckis.reportUncaughtError( throwable );

    // Not called again
    assertEquals( callCount.get(), 2 );
  }

  @Test
  public void addUncaughtErrorHandler_alreadyExists()
  {
    final UncaughtErrorHandler handler = e -> {
    };
    Zemeckis.addUncaughtErrorHandler( handler );

    assertInvariantFailure( () -> Zemeckis.addUncaughtErrorHandler( handler ),
                            "Zemeckis-0006: Attempting to add handler " + handler + " that is already in " +
                            "the list of error handlers." );
  }

  @Test
  public void removeUncaughtErrorHandler_noExists()
  {
    final UncaughtErrorHandler handler = e -> {
    };

    assertInvariantFailure( () -> Zemeckis.removeUncaughtErrorHandler( handler ),
                            "Zemeckis-0007: Attempting to remove handler " + handler + " that is not in " +
                            "the list of error handlers." );
  }

  @Test
  public void multipleHandlers()
  {
    allowUncaughtExceptions();
    final Throwable throwable = new IllegalStateException();

    final AtomicInteger callCount1 = new AtomicInteger();
    final AtomicInteger callCount2 = new AtomicInteger();
    final AtomicInteger callCount3 = new AtomicInteger();

    Zemeckis.addUncaughtErrorHandler( e -> callCount1.incrementAndGet() );
    Zemeckis.addUncaughtErrorHandler( e -> callCount2.incrementAndGet() );
    Zemeckis.addUncaughtErrorHandler( e -> callCount3.incrementAndGet() );

    Zemeckis.reportUncaughtError( throwable );

    assertEquals( callCount1.get(), 1 );
    assertEquals( callCount2.get(), 1 );
    assertEquals( callCount3.get(), 1 );

    Zemeckis.reportUncaughtError( throwable );

    assertEquals( callCount1.get(), 2 );
    assertEquals( callCount2.get(), 2 );
    assertEquals( callCount3.get(), 2 );
  }

  @Test
  public void onUncaughtError_whereOneHandlerGeneratesError()
  {
    allowUncaughtExceptions();
    final Throwable throwable = new IllegalStateException();

    final AtomicInteger callCount1 = new AtomicInteger();
    final AtomicInteger callCount3 = new AtomicInteger();

    final RuntimeException exception = new RuntimeException( "X" );

    final UncaughtErrorHandler handler2 = e -> {
      throw exception;
    };
    Zemeckis.addUncaughtErrorHandler( e -> callCount1.incrementAndGet() );
    Zemeckis.addUncaughtErrorHandler( handler2 );
    Zemeckis.addUncaughtErrorHandler( e -> callCount3.incrementAndGet() );

    Zemeckis.reportUncaughtError( throwable );

    assertEquals( callCount1.get(), 1 );
    assertEquals( callCount3.get(), 1 );

    final ArrayList<TestLogger.LogEntry> entries = getTestLogger().getEntries();
    assertEquals( entries.size(), 1 );
    final TestLogger.LogEntry entry1 = entries.get( 0 );
    assertEquals( entry1.getMessage(),
                  "Exception when notifying error handler '" + handler2 + "' of '" + throwable + "' error." );
    assertEquals( entry1.getThrowable(), exception );

    Zemeckis.reportUncaughtError( throwable );

    assertEquals( callCount1.get(), 2 );
    assertEquals( callCount3.get(), 2 );

    assertEquals( getTestLogger().getEntries().size(), 2 );
  }

  @Test
  public void onUncaughtError_whereOneHandlerGeneratesError_but_Zemeckis_areNamesEnabled_is_false()
  {
    allowUncaughtExceptions();
    ZemeckisTestUtil.disableNames();

    final Throwable throwable = new IllegalStateException();

    final RuntimeException exception = new RuntimeException( "X" );

    final UncaughtErrorHandler handler2 = e -> {
      throw exception;
    };
    Zemeckis.addUncaughtErrorHandler( handler2 );

    Zemeckis.reportUncaughtError( throwable );

    final ArrayList<TestLogger.LogEntry> entries = getTestLogger().getEntries();
    assertEquals( entries.size(), 1 );
    final TestLogger.LogEntry entry1 = entries.get( 0 );
    assertEquals( entry1.getMessage(), "Error triggered when invoking UncaughtErrorHandler.onUncaughtError()" );
    assertEquals( entry1.getThrowable(), exception );

    Zemeckis.reportUncaughtError( throwable );

    assertEquals( getTestLogger().getEntries().size(), 2 );
  }

  @Test
  public void addUncaughtErrorHandler_errorHandlersDisabled()
  {
    allowUncaughtExceptions();
    ZemeckisTestUtil.disableUncaughtErrorHandlers();

    final UncaughtErrorHandler handler = e -> {
    };

    assertInvariantFailure( () -> Zemeckis.addUncaughtErrorHandler( handler ),
                            "Zemeckis-0011: UncaughtErrorHandlerSupport.get() invoked when Zemeckis.areUncaughtErrorHandlersEnabled() returns false." );

    // This should produce no error and will be silently omitted
    Zemeckis.reportUncaughtError( new IllegalStateException() );
  }
}
