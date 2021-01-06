package zemeckis;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.realityforge.braincheck.Guards.*;

final class TaskEntry
  implements Cancelable
{
  /**
   * A human consumable name for the task. It should be non-null if {@link Zemeckis#areNamesEnabled()} returns
   * true and <tt>null</tt> otherwise.
   */
  @Nullable
  private final String _name;
  @Nullable
  private Runnable _task;
  @Nullable
  private Cancelable _cancelAction;

  /**
   * Create a task entry.
   *
   * @param name         A human consumable name for the task. It should be non-null if {@link Zemeckis#areNamesEnabled()} returns true and <tt>null</tt> otherwise.
   * @param task         the task.
   * @param cancelAction the code to call to cancel pending task side-effects.
   */
  TaskEntry( @Nullable final String name, @Nonnull final Runnable task, @Nullable final Cancelable cancelAction )
  {
    if ( Zemeckis.shouldCheckApiInvariants() )
    {
      apiInvariant( () -> Zemeckis.areNamesEnabled() || null == name,
                    () -> "Zemeckis-0013: Task passed a name '" + name + "' but Zemeckis.areNamesEnabled() is false" );
    }
    _name = Zemeckis.areNamesEnabled() ? Objects.requireNonNull( name ) : null;
    _task = Objects.requireNonNull( task );
    _cancelAction = cancelAction;
  }

  @Nullable
  Runnable getTask()
  {
    return _task;
  }

  @Nullable
  Cancelable getCancelAction()
  {
    return _cancelAction;
  }

  void execute()
  {
    if ( null != _task )
    {
      _task.run();
      _task = null;
      _cancelAction = null;
    }
  }

  @Override
  public void cancel()
  {
    if ( null != _cancelAction )
    {
      _cancelAction.cancel();
      _cancelAction = null;
    }
    _task = null;
  }

  @Override
  public String toString()
  {
    return Zemeckis.areNamesEnabled() ? _name : super.toString();
  }
}
