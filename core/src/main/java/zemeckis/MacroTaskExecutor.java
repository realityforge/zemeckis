package zemeckis;

import elemental2.dom.MessageChannel;

/**
 * Run tasks in next MacroTask.
 */
final class MacroTaskExecutor
  extends RoundBasedExecutor
{
  private final MessageChannel _channel = Zemeckis.useMessageChannelToScheduleTasks() ? new MessageChannel() : null;

  MacroTaskExecutor()
  {
    if ( Zemeckis.useMessageChannelToScheduleTasks() )
    {
      _channel.port1.onmessage = m -> activate();
    }
  }

  @Override
  void scheduleForActivation()
  {
    if ( Zemeckis.useMessageChannelToScheduleTasks() )
    {
      _channel.port2.postMessage( null );
    }
    else
    {
      TemporalScheduler.delayedTask( Zemeckis.areNamesEnabled() ? "MacroTaskExecutor" : null, this::activate, 0 );
    }
  }
}
