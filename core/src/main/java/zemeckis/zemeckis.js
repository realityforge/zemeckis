/**
 * This file provides the @defines for zemeckis configuration options.
 * See ZemeckisConfig.java for details.
 */
goog.module('zemeckis');
goog.module.declareLegacyNamespace();

const {addSystemPropertyFromGoogDefine} = goog.require('jre');

/** @define {string} */
const environment = goog.define('zemeckis.environment', 'production');
addSystemPropertyFromGoogDefine('zemeckis.environment', environment);

/** @define {string} */
const enableNames = goog.define('zemeckis.enable_names', 'false');
addSystemPropertyFromGoogDefine('zemeckis.enable_names', enableNames);

/** @define {string} */
const purgeTasksWhenRunawayDetected =
    goog.define('zemeckis.purge_tasks_when_runaway_detected', 'true');
addSystemPropertyFromGoogDefine(
    'zemeckis.purge_tasks_when_runaway_detected', purgeTasksWhenRunawayDetected);

/** @define {string} */
const enableUncaughtErrorHandlers =
    goog.define('zemeckis.enable_uncaught_error_handlers', 'false');
addSystemPropertyFromGoogDefine(
    'zemeckis.enable_uncaught_error_handlers', enableUncaughtErrorHandlers);

/** @define {string} */
const useMessageChannelToScheduleTasks =
    goog.define('zemeckis.use_message_channel_to_schedule_tasks', 'true');
addSystemPropertyFromGoogDefine(
    'zemeckis.use_message_channel_to_schedule_tasks',
    useMessageChannelToScheduleTasks);

/** @define {string} */
const useWorkerToScheduleDelayedTasks =
    goog.define('zemeckis.use_worker_to_schedule_delayed_tasks', 'true');
addSystemPropertyFromGoogDefine(
    'zemeckis.use_worker_to_schedule_delayed_tasks',
    useWorkerToScheduleDelayedTasks);

/** @define {string} */
const logWorkerInteractions =
    goog.define('zemeckis.log_worker_interactions', 'false');
addSystemPropertyFromGoogDefine(
    'zemeckis.log_worker_interactions', logWorkerInteractions);

/** @define {string} */
const logger = goog.define('zemeckis.logger', 'none');
addSystemPropertyFromGoogDefine('zemeckis.logger', logger);

exports = {
  enable_names: enableNames,
  enable_uncaught_error_handlers: enableUncaughtErrorHandlers,
  environment,
  log_worker_interactions: logWorkerInteractions,
  logger,
  purge_tasks_when_runaway_detected: purgeTasksWhenRunawayDetected,
  use_message_channel_to_schedule_tasks: useMessageChannelToScheduleTasks,
  use_worker_to_schedule_delayed_tasks: useWorkerToScheduleDelayedTasks,
};
