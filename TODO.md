## TODO

This document is essentially a list of shorthand notes describing work yet to be completed.
Unfortunately it is not complete enough for other people to pick work off the list and
complete as there is too much un-said.

### Pre-Beta-Release

* Add a `task(...)` method that schedules to the current VPU if any else invokes `becomeMacroTask`.

* Add mechanism by which a `RepeatingTask` is supported. The `RepeatingTask` is added to either the
  `OnIdle` or `MacroTask` VPUs. If the `RepeatingTask` exceeds the `TIME_SLICE` (aka 14ms for `MacroTask`
  VPUs or deadline for `OnIdle` VPU) then the tasks are scheduled onto next scheduling. Replicant should
  be updated to take advantage of this. The JVM variant will just repeatedly invoke the task until done.
  The entry point to this may be an `incrementalTask(...)` to match what was previous in GWT.

* Support scheduling of tasks with delay and repeating tasks inside a WebWorker where the WebWorker
  is responsible for scheduling the tasks. This produces a more stable scheduling at the expense of
  slightly more complexity. Both sides would need to keep a record of which timers are active and when
  a timer is canceled in the main app it should be cancelled in the WebWorker. When the main app is
  unloaded, it is also responsible for unloading all registered timers or shutting down the WebWorker.
  See the [article](https://medium.com/teads-engineering/the-most-accurate-way-to-schedule-a-function-in-a-web-browser-eadcd164da12)
  for measurements carried out by another party. This feature should be controlled by a compile time
  flag which can fallback to local scheduling.

### Scheduler

* https://github.com/spanicker/main-thread-scheduling

* https://github.com/facebook/react/blob/master/packages/scheduler/src/Scheduler.js

* Enhance scheduler so that it can schedule based on user priority/deadline as well as delay and/or period.
  These priorities indicate when the task will be executed. By default the task may be scheduled via as an
  idle task but as it approaches the deadline then it is moved to macroTask. The priorities out of react's
  scheduler and the corresponding timeout in ms are as follows

      // Times out immediately
      var IMMEDIATE_PRIORITY_TIMEOUT = -1;
      // Eventually times out
      var USER_BLOCKING_PRIORITY = 250;
      var NORMAL_PRIORITY_TIMEOUT = 5000;
      var LOW_PRIORITY_TIMEOUT = 10000;
      // Never times out
      var IDLE_PRIORITY = maxSigned31BitInt;

* Should add a queuing method in VPU that schedules according to one of these above priorities.

* Once the scheduler is in play it is likely we will want to implement code using `idle-until-urgent` strategy.
  Useful to delay some of the expensive setup for off screen stuff.
  - https://philipwalton.com/articles/idle-until-urgent/
  - https://github.com/GoogleChromeLabs/idlize/blob/master/IdleQueue.mjs
  - https://github.com/GoogleChromeLabs/idlize/blob/master/IdleValue.mjs
