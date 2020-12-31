package zemeckis.examples.dom.basic;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import zemeckis.Scheduler;

public final class Example
  implements EntryPoint
{
  @Nonnull
  private static final Map<String, String> STYLES = new HashMap<String, String>()
  {
    {
      put( "macro", "color: #006AEB; font-weight: normal;" );
      put( "micro", "color: #c143eb; font-weight: normal;" );
      put( "animationFrame", "color: #FFBA49; font-weight: normal;" );
      put( "afterFrame", "color: #10a210; font-weight: normal;" );
      put( "onIdle", "color: #A18008; font-weight: normal;" );
    }
  };

  @Override
  public void onModuleLoad()
  {
    // AfterFrame schedules a macro task after the next render
    Scheduler.afterFrame( () -> log( "afterFrame", 1 ) );
    // Schedule a task immediately preceding next render
    Scheduler.animationFrame( () -> log( "animationFrame", 1 ) );

    // Schedule a macro task
    Scheduler.macroTask( () -> {
      log( "macro", 1 );

      // Schedule a micro task after the current macro task
      Scheduler.microTask( () -> log( "micro", 1 ) );
      // Schedule a micro task after the current macro task. This will run in
      // the same micro task invocation as the previous task
      Scheduler.microTask( () -> log( "micro", 2 ) );

      // Schedule a macro task - this will run in the current macro task
      Scheduler.macroTask( () -> log( "macro", 2 ) );
    } );

    // Schedule a task to execute when the browser is idle
    Scheduler.onIdle( () -> log( "onIdle", 1 ) );
  }

  private static void log( @Nonnull final String type, final int id )
  {
    DomGlobal.console.log( "%c" + type + " task " + id, STYLES.get( type ) );
  }
}
