package zemeckis.examples.dom.repeating_task;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;
import javax.annotation.Nonnull;
import zemeckis.Zemeckis;

public final class Example
  implements EntryPoint
{
  @Nonnull
  private static final String[] COLORS = { "color: #006AEB;", "color: #c143eb;" };

  @Override
  public void onModuleLoad()
  {
    new Timer( 1, 2000 );
    new Timer( 2, 10000 );
  }

  private static final class Timer
  {
    private final int _id;
    private final int _period;
    private int _lastTime = -1;

    Timer( final int id, final int period )
    {
      _id = id;
      _period = period;
      Zemeckis.periodicTask( this::tick, _period );
    }

    void tick()
    {
      final int now = Zemeckis.now();
      final int delta = -1 == _lastTime ? 0 : now - _lastTime;
      _lastTime = now;
      DomGlobal.console.log( "%c task " + _id + " Period=" + _period + " Delta=" + delta,
                             COLORS[ _id % COLORS.length ] );
    }
  }
}
