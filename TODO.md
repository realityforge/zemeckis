## TODO

This document is essentially a list of shorthand notes describing work yet to be completed.
Unfortunately it is not complete enough for other people to pick work off the list and
complete as there is too much un-said.

### Pre-Beta-Release

* Replace the term `macroTask` in API with `task` and maybe rather than scheduling on a subsequent macro task
  it just schedules to the current task?

* Determine the feasibility of having the macro/micro/animationFrame/afterFrame/onIdle methods returning a
  `Cancelable` that will allow the cancelling of a scheduled task. This probably means changing the task queue
  to contain a wrapper object ala `Task`. This would also allow us to name tasks...

* Add grim annotations.

### Scheduler

* Scheduling using `task(...)` should use the priority and/or VPU of the creating task.

* Support scheduling of tasks with delay and repeating tasks inside a WebWorker where the WebWorker
  is responsible for scheduling the tasks. This produces a more stable scheduling at the expense of
  slightly more complexity. Both sides would need to keep a record of which timers are active and when
  a timer is canceled in the main app it should be cancelled in the WebWorker. When the main app is
  unloaded, it is also responsible for unloading all registered timers or shutting down the WebWorker.
  See the [article](https://medium.com/teads-engineering/the-most-accurate-way-to-schedule-a-function-in-a-web-browser-eadcd164da12)
  for measurements carried out by another party. This feature should be controlled by a compile time
  flag which can fallback to local scheduling.

* Support scheduling tasks with delay = 0 by using `MessageChannel.send` as it has less jitter. This
  feature should be controlled by a compile time flag which can fallback to local scheduling.

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
