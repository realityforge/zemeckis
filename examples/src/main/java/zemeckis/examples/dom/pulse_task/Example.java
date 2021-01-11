package zemeckis.examples.dom.pulse_task;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import zemeckis.Cancelable;
import zemeckis.Zemeckis;

public final class Example
  implements EntryPoint
{
  @Nonnull
  private static final String[] COLORS = { "color: #006AEB;", "color: #c143eb;" };

  @Override
  public void onModuleLoad()
  {
    createTimerThatCancelsSelfAndRestartsAfterDelay();
  }

  private static void createTimerThatCancelsSelfAndRestartsAfterDelay()
  {
    final int[] counter = new int[ 1 ];
    // Every 500ms tick and after 5 ticks pause for 5 seconds and start again
    new Timer( 3, 500, c -> {
      counter[ 0 ]++;
      if ( counter[ 0 ] > 4 )
      {
        c.cancel();
        Zemeckis.delayedTask( Example::createTimerThatCancelsSelfAndRestartsAfterDelay, 5000 );
      }
    } );
  }

  private static final class Timer
  {
    private final int _id;
    private final int _period;
    @Nonnull
    private final Consumer<Cancelable> _action;
    @Nonnull
    private final Cancelable _self;
    private int _lastTime = -1;

    Timer( final int id, final int period, @Nonnull final Consumer<Cancelable> action )
    {
      _id = id;
      _period = period;
      _action = Objects.requireNonNull( action );
      _self = Zemeckis.periodicTask( this::tick, _period );
    }

    void tick()
    {
      final int now = Zemeckis.now();
      final int delta = -1 == _lastTime ? 0 : now - _lastTime;
      _lastTime = now;
      DomGlobal.console.log( "%c task " + _id + " Period=" + _period + " Delta=" + delta,
                             COLORS[ _id % COLORS.length ] );
      _action.accept( _self );
    }
  }
}
