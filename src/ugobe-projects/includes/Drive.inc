/* $Id$ */

/**
 * @file
 * Drive System interface.
 *
 * interface to the Drive/Behavior System of Pleo
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */
 
#if defined _drive_included
  #endinput
#endif

#define _drive_included

/**
 * @brief add a drive to the system
 * @param name for the drive. Used to call evaluator, letters only.
 * @param tvl trigger-value-low: the low trigger
 * @param tvh trigger-value-high: the high trigger
 * @param interval at which the evaluator is called (ms). -1 for never
 * @return 0
 */
native drive_add(const drive_name[], tvl, tvh, interval);

/**
 * @brief set the trigger values of a drive
 * @param tvl trigger-value-low: the low trigger
 * @param tvh trigger-value-high: the high trigger
 */
native set_trigger_values(const drive_name[], tvl, tvh);

#pragma deprecated set_trigger_values

// name that matches the style used everywhere else
stock drive_set_trigger_values(const drive_name[], tvl, tvh)
    { set_trigger_values(drive_name[], tvl, tvh); }

/**
 * @brief set the value of a behavior manually
 * @param the name of the drive to set
 * @param the value to set this drive to 
 */
native drive_set_value(const drive_name[], value);


/**
 * @brief add a behavior to a drive
 * @param id of the associated behavior script
 * @param name of the drive to add this behavior to
 * @param lead time not used
 * @param up regulation [not used, should be 0]
 * @param maintain regulation [not used, should be 0]
 * @param down regulation [not used, should be 0]
 * @param interval at which the evaluator is called (ms). -1 for never
 * @return 0
 */
native behavior_add(behavior_id, const drive_name[], lead_time, upreg, samereg, downreg, interval);

/**
 * @brief set the value of a behavior manually
 * @param the name of the drive for this behavior
 * @param the id of the behavior 
 * @param the value to set this behavior to 
 */
native behavior_set_value(const drive_name[], behavior_id, value);
