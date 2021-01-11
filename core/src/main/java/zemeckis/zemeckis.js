/**
 * This file provides the @defines for zemeckis configuration options.
 * See ZemeckisConfig.java for details.
 */
goog.provide('zemeckis');

/** @define {string} */
zemeckis.environment = goog.define('zemeckis.environment', 'production');

/** @define {string} */
zemeckis.enable_names = goog.define('zemeckis.enable_names', 'false');

/** @define {string} */
zemeckis.purge_tasks_when_runaway_detected = goog.define('zemeckis.purge_tasks_when_runaway_detected', 'true');

/** @define {string} */
zemeckis.enable_uncaught_error_handlers = goog.define('zemeckis.enable_uncaught_error_handlers', 'false');

/** @define {string} */
zemeckis.use_message_channel_to_schedule_tasks = goog.define('zemeckis.use_message_channel_to_schedule_tasks', 'true');

/** @define {string} */
zemeckis.use_worker_to_schedule_delayed_tasks = goog.define('zemeckis.use_worker_to_schedule_delayed_tasks', 'true');

/** @define {string} */
zemeckis.log_worker_interactions = goog.define('zemeckis.log_worker_interactions', 'false');

/** @define {string} */
zemeckis.logger = goog.define('zemeckis.logger', 'none');
