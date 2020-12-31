package zemeckis;

import java.util.ArrayList;
import java.util.List;
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
  @Nonnull
  private final List<Throwable> _uncaughtExceptions = new ArrayList<>();
  private boolean _allowUncaughtExceptions;

  @BeforeMethod
  protected void beforeTest()
  {
    BrainCheckTestUtil.resetConfig( false );
    ZemeckisTestUtil.resetConfig( false );
    _logger.getEntries().clear();
    _uncaughtExceptions.clear();
    Zemeckis.addUncaughtErrorHandler( _uncaughtExceptions::add );
    ZemeckisTestUtil.setLogger( _logger );
  }

  @AfterMethod
  protected void afterTest()
  {
    if ( !_allowUncaughtExceptions && !_uncaughtExceptions.isEmpty() )
    {
      _uncaughtExceptions.forEach( Throwable::printStackTrace );
      fail( "Uncaught exceptions executing tasks" );
    }
    BrainCheckTestUtil.resetConfig( true );
    ZemeckisTestUtil.resetConfig( true );
  }

  protected final void allowUncaughtExceptions()
  {
    _allowUncaughtExceptions = true;
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

  protected final void assertDefaultToStringWhenNamesDisabled( @Nonnull final Object object )
  {
    ZemeckisTestUtil.disableNames();
    assertDefaultToString( object );
    ZemeckisTestUtil.enableNames();
  }

  protected final void assertDefaultToString( @Nonnull final Object object )
  {
    assertEquals( object.toString(), object.getClass().getName() + "@" + Integer.toHexString( object.hashCode() ) );
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
