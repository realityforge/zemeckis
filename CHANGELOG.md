# Change Log

### [v0.12](https://github.com/realityforge/zemeckis/tree/v0.12) (2021-07-27) · [Full Changelog](https://github.com/spritz/spritz/compare/v0.11...v0.12)

Changes in this release:

* Update the `org.realityforge.gir` artifact to version `0.12`.
* Upgrade the `org.realityforge.akasha` artifacts to version `0.15`.

### [v0.11](https://github.com/realityforge/zemeckis/tree/v0.11) (2021-04-22) · [Full Changelog](https://github.com/spritz/spritz/compare/v0.10...v0.11)

Changes in this release:

* Upgrade the `org.realityforge.akasha` artifact to version `0.10`.
* Upgrade the `org.realityforge.braincheck` artifact to version `1.31.0`.

### [v0.10](https://github.com/realityforge/zemeckis/tree/v0.10) (2021-04-10) · [Full Changelog](https://github.com/spritz/spritz/compare/v0.09...v0.10)

Changes in this release:

* Upgrade the `org.realityforge.braincheck` artifact to version `1.30.0`.
* Upgrade the `org.realityforge.akasha` artifact to version `0.06`.

### [v0.09](https://github.com/realityforge/zemeckis/tree/v0.09) (2021-03-23) · [Full Changelog](https://github.com/realityforge/zemeckis/compare/v0.08...v0.09)

Changes in this release:

* Upgrade the `org.realityforge.grim` artifacts to version `0.05`.
* Upgrade the `au.com.stocksoftware.idea.codestyle` artifact to version `1.17`.
* Move from Elemental2 to Akasha as the mechanism for interacting with the browser.

### [v0.08](https://github.com/realityforge/zemeckis/tree/v0.08) (2021-01-11) · [Full Changelog](https://github.com/realityforge/zemeckis/compare/v0.07...v0.08)

Changes in this release:

* Correct the name of the module used in the `pulse_task` example.
* Add a compile time setting `zemeckis.log_worker_interactions` that will control whether debug logging is emitted to the console when interacting with the Worker for scheduling periodic and delayed tasks.
* Avoid accidental coercion in Worker code by replacing constructs like `&& m.data.id` with `&& m.data.id !== undefined`. Previously requests to schedule tasks with periods, delays and ids of `0` could result in the task being ignored.

### [v0.07](https://github.com/realityforge/zemeckis/tree/v0.07) (2021-01-11) · [Full Changelog](https://github.com/realityforge/zemeckis/compare/v0.06...v0.07)

Changes in this release:

* Add simple the demonstrates the interaction between periodic and delayed tasks.
* If workers are used to schedule delayed and periodic tasks then name the worker `ZemeckisTimer` to make identifying the purpose of the worker clear.
* Inline some let statements that seemed to cause problems for some browsers.
* Ensure the record of the delayed timer is removed from the Worker cache when the delayed timer is fired. This eliminated a memory leak.
* Use the correct syntax when deleting properties from objects as the previous syntax is not guaranteed to work without additional libraries.
* Fix a bug when requesting a delayed timer that resulted in the Worker creating a periodic timer and delivering the `tick` message multiple times but the main thread ignoring subsequent ticks. The application behaviour was correct but the code created significant amount of unnecessary load when many delayed tasks were created.
* Improve the Worker logic so that malformed messages sent to the Worker will be ignored rather than potentially creating a cascading failure.

### [v0.06](https://github.com/realityforge/zemeckis/tree/v0.06) (2021-01-08) · [Full Changelog](https://github.com/realityforge/zemeckis/compare/v0.05...v0.06)

The release includes 2 non breaking API changes.

Changes in this release:

* Change the default mechanism for scheduling macro tasks to use `MessageChannel.send` as it is less likely to
  be throttled by the browser when in a background thread when compared to `setTimeout(...)` style scheduling
  and it seems to have less jitter. See the explainer [article](https://www.tenforums.com/tutorials/80233-enable-disable-google-chrome-background-tab-throttling-windows.html) for some details about
  how chrome throttles some timers. The toolkit can use revert to using `setTimeout(...)` by specifying the
  compile time setting `zemeckis.use_message_channel_to_schedule_tasks` to `false`.
* Improve the clairty of the `Zemeckis` javadocs.
* Change the default mechanism for scheduling delayed and periodic tasks so that the tasks are scheduled
  within a `WebWorker` and the web worker sends a message to the main thread to to trigger the task. This
  results in a more stable scheduling at the expense of additional code complexity and code size. This also
  eliminates throttling that occurs when the browser tab is in the background. See the [article](https://medium.com/teads-engineering/the-most-accurate-way-to-schedule-a-function-in-a-web-browser-eadcd164da12)
  for measurements carried out by another party that validates this approach. The toolkit can use revert
  to using `setTimeout(...)` and `setInterval(...)` by specifying the compile time setting
  `zemeckis.use_worker_to_schedule_delayed_tasks` to `false`.

### [v0.05](https://github.com/realityforge/zemeckis/tree/v0.05) (2021-01-07) · [Full Changelog](https://github.com/realityforge/zemeckis/compare/v0.04...v0.05)

Changes in this release:

* Decouple `ZemeckisConfig` from GWT libraries to make it compatible with J2CL.

### [v0.04](https://github.com/realityforge/zemeckis/tree/v0.04) (2021-01-06) · [Full Changelog](https://github.com/realityforge/zemeckis/compare/v0.03...v0.04)

The release includes 22 non breaking API changes, 1 potentially breaking API change and 2 breaking API changes.

Changes in this release:

* Move all methods from the `Schedule` class to the `Zemeckis` class.
* Rework the `Zemeckis.macroTask(...)`, `Zemeckis.microTask(...)`, `Zemeckis.animationFrame(...)`, `Zemeckis.afterFrame(...)` and `Zemeckis.onIdle(...)` methods so that they return a `Cancelable` that can be used to abort the scheduled task before it executes.
* Make it possible to specify a human-readable name for a task when scheduling a task. Names are only supported if the `Zemeckis.areNamesEnabled()` returns true which is controlled by the compile time parameter `zemeckis.enable_names`.
* Add `grim` annotations to make it easy to assert which parts of code is expected to be optimized away in different builds.

### [v0.03](https://github.com/realityforge/zemeckis/tree/v0.03) (2021-01-05) · [Full Changelog](https://github.com/realityforge/zemeckis/compare/v0.02...v0.03)

The release includes 2 non breaking API changes and 3 breaking API changes.

Changes in this release:

* Fix the invariant message generated by `Schedule.periodicTask(...)` to indicate that `0` is not a valid period.
* Rename the method `Scheduler.schedule(...)` to `Scheduler.delayedTask(...)`.
* Rename the method `Scheduler.scheduleAtFixedRate(...)` to `Scheduler.periodicTask(...)`.
* Rework the `Scheduler.delayedTask(...)` and `Scheduler.periodicTask(...)` methods to call `becomeMacroTask(...)` to wrap supplied task. This will result in tasks scheduled with a delay and tasks scheduled via `Scheduler.macroTask(...)` behaving the same way.
* Remove `Schedule.becomeMacroTask(...)` from the public API as should not be needed now that `delayedTask` and `periodicTask` methods invoke this method internally.

### [v0.02](https://github.com/realityforge/zemeckis/tree/v0.02) (2020-12-31) · [Full Changelog](https://github.com/realityforge/zemeckis/compare/v0.01...v0.02)

Changes in this release:

* Fix bug that resulted in micro tasks not being scheduled.
* Fix bug that resulted in afterFrame tasks not being scheduled.

### [v0.01](https://github.com/realityforge/zemeckis/tree/v0.01) (2020-12-31) · [Full Changelog](https://github.com/realityforge/zemeckis/compare/aabdb6891ff2bc9f21417aab5e9ab7492173a361...v0.01)

 🎉 Initial release 🎉
