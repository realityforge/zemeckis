package zemeckis;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class TaskEntry
  implements Cancelable
{
  @Nullable
  private Runnable _task;

  TaskEntry( @Nonnull final Runnable task )
  {
    _task = Objects.requireNonNull( task );
  }

  @Nullable
  Runnable getTask()
  {
    return _task;
  }

  void execute()
  {
    if ( null != _task )
    {
      _task.run();
    }
  }

  @Override
  public void cancel()
  {
    _task = null;
  }

  @Override
  public String toString()
  {
    if ( Zemeckis.areNamesEnabled() )
    {
      return null == _task ? "-" : _task.toString();
    }
    else
    {
      return super.toString();
    }
  }
}
