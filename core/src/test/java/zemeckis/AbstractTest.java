package zemeckis;

import java.util.Random;
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
  private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
  @Nonnull
  private static final Random c_random = new Random();
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

  protected final int randomInt()
  {
    return getRandom().nextInt();
  }

  @Nonnull
  protected final Random getRandom()
  {
    return c_random;
  }

  @Nonnull
  protected final String randomString()
  {
    return randomString( 12 );
  }

  @SuppressWarnings( "SameParameterValue" )
  @Nonnull
  protected final String randomString( final int stringLength )
  {
    final StringBuilder sb = new StringBuilder( stringLength );
    for ( int i = 0; i < stringLength; i++ )
    {
      sb.append( CHARS.charAt( Math.abs( randomInt() % CHARS.length() ) ) );
    }
    return sb.toString();
  }
}
