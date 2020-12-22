package zemeckis;

import javax.annotation.Nonnull;
import org.realityforge.braincheck.BrainCheckTestUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import static org.testng.Assert.*;

@Listeners( MessageCollector.class )
public abstract class AbstractTest
{
  @Nonnull
  private final TestLogger _logger = new TestLogger();

  @BeforeMethod
  protected void beforeTest()
  {
    BrainCheckTestUtil.resetConfig( false );
    ZemeckisTestUtil.resetConfig( false );
    _logger.getEntries().clear();
    ZemeckisTestUtil.setLogger( _logger );
  }

  @AfterMethod
  protected void afterTest()
  {
    BrainCheckTestUtil.resetConfig( true );
    ZemeckisTestUtil.resetConfig( true );
  }

  @Nonnull
  protected final TestLogger getTestLogger()
  {
    return _logger;
  }

  protected final void assertInvariantFailure( @Nonnull final ThrowingRunnable throwingRunnable,
                                               @Nonnull final String message )
  {
    assertEquals( expectThrows( IllegalStateException.class, throwingRunnable ).getMessage(), message );
  }
}
