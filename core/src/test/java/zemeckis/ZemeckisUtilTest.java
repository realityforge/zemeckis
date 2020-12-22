package zemeckis;

import java.io.IOException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ZemeckisUtilTest
{
  @Test
  public void safeGetString()
  {
    assertEquals( ZemeckisUtil.safeGetString( () -> "My String" ), "My String" );
  }

  @Test
  public void safeGetString_generatesError()
  {
    final String text = ZemeckisUtil.safeGetString( () -> {
      throw new RuntimeException( "X" );
    } );
    assertTrue( text.startsWith( "Exception generated whilst attempting to get supplied message.\n" +
                                 "java.lang.RuntimeException: X\n" ) );
  }

  @Test
  public void throwableToString()
  {
    final String text = ZemeckisUtil.throwableToString( new RuntimeException( "X" ) );
    assertTrue( text.startsWith( "java.lang.RuntimeException: X\n" ) );
  }

  @Test
  public void throwableToString_with_NestedThrowable()
  {
    final RuntimeException exception =
      new RuntimeException( "X", new IOException( "Y" ) );
    final String text = ZemeckisUtil.throwableToString( exception );
    assertTrue( text.startsWith( "java.lang.RuntimeException: X\n" ) );
    assertTrue( text.contains( "\nCaused by: java.io.IOException: Y\n" ) );
  }
}
