## TODO

This document is essentially a list of shorthand notes describing work yet to be completed.
Unfortunately it is not complete enough for other people to pick work off the list and
complete as there is too much un-said.

### High Priority Tasks

* Experiment with using MessageChannel.send for MacroTask and compare jitter/delay between the two

* Use a WebWorker to schedule repeating tasks

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

* Some tasks should be scheduled to current VPU and/or with similar deadlines/priorities as creating task.
