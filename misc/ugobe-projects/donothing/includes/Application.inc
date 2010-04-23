/* $Id$ */

/**
 * @file
 * Application System interface
 *
 * Primitives provided by the high-level to control applications. Applications
 * are a combination of a sensor script, main script and a set of aux. or behavior
 * scripts. Note this does not normally include any script executing in the User VM.
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */

#if defined _application_included
  #endinput
#endif

#define _application_included


/**
 * @brief load an application
 * @param path to where the application is located
 * @return bool indicates whether the application has started loading
 */
native bool: app_load(const path[]);
    

/**
 * @brief unload an application
 * @param path to where the application is located. this must match where the application
 *    was loaded from, or can be an empty string to unload the current application no
 *    matter where it was loaded from.
 * @param power_down whether to also power down the lifeform when the app unload is complete
 * @param for how long should we stay powered down. will power up after X ms. default to 0, which is never
 * @return bool indicates whether the application has started unloading
 */
native bool: app_unload(const path[], bool: power_down = false, power_down_time_ms = 0);


/**
 * @brief ask if there is already an application loaded
 * @return bool represents whether an application is loaded or not
 */
native bool: app_is_loaded();


enum app_config {
    app_wait_for_init = 1,
    app_auto_load = 2,
    app_run_init = 3,
};

/**
 * @brief get a configuration paramter for the application module
 * @param type of data type or property to change
 * @return value of this property
 */
native app_get_config(app_config: type);


/**
 * @brief set a configuration paramter for the application module
 * @param type of data type or property to change
 * @return old value of this property
 */
native app_set_config(app_config: type, value);
