package zemeckis;

import java.io.File;
import javax.annotation.Nonnull;
import org.realityforge.braincheck.AbstractTestNGMessageCollector;
import org.realityforge.braincheck.GuardMessageCollector;
import static org.testng.Assert.*;

public final class MessageCollector
  extends AbstractTestNGMessageCollector
{
  @Override
  protected boolean shouldCheckDiagnosticMessages()
  {
    return System.getProperty( "zemeckis.check_diagnostic_messages", "true" ).equals( "true" );
  }

  @Nonnull
  @Override
  protected GuardMessageCollector createCollector()
  {
    final boolean saveIfChanged = "true".equals( System.getProperty( "zemeckis.output_fixture_data", "false" ) );
    final String fixtureDir = System.getProperty( "zemeckis.diagnostic_messages_file" );
    assertNotNull( fixtureDir,
                   "Expected System.getProperty( \"zemeckis.diagnostic_messages_file\" ) to return location of diagnostic messages file" );
    return new GuardMessageCollector( "Zemeckis", new File( fixtureDir ), saveIfChanged, true, false );
  }
}
