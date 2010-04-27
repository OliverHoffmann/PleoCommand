/* $Id$ */

/**
 * @file
 * Utility interface
 *
 * Misc. support functions
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */
 
#if defined _util_included
  #endinput
#endif

#define _util_included

// These are here, since some existing scripts only include Util,
// whereas they shold include these directly if they need them.
#include <String.inc>
#include <Time.inc>

#if TARGET == TARGET_PLEO
	/* For the version_name and config_name enumerations */
	#include "pleo/properties.inc"
#endif


/**
 * @brief return a random number
 * @param range of the random number to generate
 * @return random number
 */
native rand(x);

// a synonym for rand(), in case people have read the
// Pawn Programmers Guide, where it refers to some functions
// as recomended core functions
//#define random rand
stock random(x)
	{ return rand(x); }
	
/**
 * @brief seed the random number generator
 * @param seed value. typically use time() return as input
 * @return none
 */
native seed(x);

/**
 * @brief return the absolute number
 * @param value
 * @return absolute value of input
 */
native abs(i);

    
/**
 * @brief return the version of the firmware we are running on
 * @param buffer to store the version as a 'string' for use in other contexts. packed
 * @param size of buffer passed to the function
 * @return value representing product, version, etc.
 */
native version_get(version_name: type, buffer[], bool: packed = true, size = sizeof buffer);

/**
 * @brief set the version explicitly. curently only for serial number
 * @param which version string should be set. note that not all version strings are settable
 * @param buffer containing the new version string
 * @return value representing product, version, etc.
 */
native version_set(version_name: type, const buffer[]);


/**
 * @brief return information about the configuration of this Pleo
 * @param what type of information to return. see config_type
 * @return the value of the configuration paramter
 */
native config_get(config_name: config);

/**
 * @brief return set configuration parameters for this Pleo
 * @param what type of information to set. see config_type
 * @param value the new value for the configuration
 * @return none
 */
native config_set(config_name: config, value);


/**
 * @brief execute a monitor command from script
 * @param string containing full monitor command. Do not use a '\n' to
 * terminate the strng, or you will be sending two commands.
 * @return none
 */
native monitor_exec(const command[]);

