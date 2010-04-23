/* $Id$ */

/**
 * @file
 * Property system interface
 *
 * The Property system is the high level blackboard, used
 * to pass information between scripts and the native code.
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */

#if defined _property_included
  #endinput
#endif

#define _property_included

#if TARGET == TARGET_PLEO
	/* for the property_name type */
	#include "pleo/properties.inc"
#endif

enum prop_value {
    prop_error = 0xFFFFFFFF,
};
    

/**
 * @brief read the value of a property
 * @param property to read
 * @return value of the property. prop_error if not read successfully
 */
native property_get({property_name,user_property_name}: property);

stock get({property_name,user_property_name}: property)
    { return property_get(property); }


/**
 * @brief write the value of a property
 * @param property to read
 * @param value to write
 * @return old? value of the property. prop_error if not set
 */
native property_set({property_name,user_property_name}: property, value);

stock set({property_name,user_property_name}: property, value)
    { return property_set(property, value); }


/**
 * @brief change this property into a leaky integrator and set the leak amount
 * @param property to set
 * @param leak delta to apply
 * @param rate at which to leak the value, in ms
 * @param maximum value this property can leak to
 * @param minimum value this property can leak to
 * @return none
 */
native property_set_leak({property_name,user_property_name}: property, delta, interval, max, min);


/**
 * @brief enable or disable all leakage
 * @param flag enable = true / disable = false
 * @return none
 */
native property_leak_enable(bool: enable_leaking);


/**
 * @brief load properties from a file. this should merge properties
 * already defined with those defined in the proeprties file.
 * @param filename to load from
 * @return bool whether the load was successfull
 */
native bool: property_load(const file_name[]);


/**
 * @brief save properties to a file
 * @param filename to save to
 * @return bool whether the save was successfull
 */
native bool: property_save(const file_name[]);
 

/**
 * @brief get the name of a property as a string
 * @param property ID to look up
 * @param buffer to store the property name. Note it may not
 * include a 'property_' prepended to the name.
 * @param packed whether to store the result as a packed string
 * @param length of the name buffer passed in, in cells
 * @return bool whether the name was retrieved successfully.
 * @todo implement native side
 */
//native property_get_name(property_name: property, name[], bool: packed = true, len = sizeof name);


/**
 * @brief add reporting of property changes to this property
 *        the below declared function on_property is called 
 *        when a qualifying change is detected
 * @param property: the property to report changes on
 * @param min_change: the minimum amount of change to report. 0 for no report on change
 * @param trigger: a threshold to report crossings of 
 */
native bool: property_add_report({property_name,user_property_name}: property, min_change, trigger);


/**
 * @brief turn property reporting on / off
 * @param enable: true for on, false for off
 */
native property_enable_reporting(bool: enable);


/**
 * @brief prototype for an on_property function that script can
 * implement to be notified of property changes right away. Currently,
 * this function must be in the sensors script.
 * @param time at which the property changed
 * @param property name that has changed. this can also be a user_property
 * @param the new value of the property
 */
forward public on_property(time, property_name: property, value);
