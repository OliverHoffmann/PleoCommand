/**
 * @file
 * Limit(s) file
 *
 * Some firmware limits that scripts ought to follow, specific to
 * Pleo. See the Pawn Language Guide for details.
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */


/*
 * These are the limits for the DATA section + STACK size
 * use these with the amxram pragma. For example:
 *    #pragma amxram SENSORS_RAM_MAX
 */
 
// Header + Data + Stack for Sensors VM
#define SENSORS_RAM_MAX 3*1024

// Header + Data + Stack for Main VM
#define MAIN_RAM_MAX 4*1024

// Header + Data + Stack for Behavior/Aux VM
#if PLEO_API == PLEO_1_1
    #define AUX_RAM_MAX 5*1024/2
#elseif PLEO_API == PLEO_1_0
    #define AUX_RAM_MAX 2*1024
#endif

// Header + Data + Stack for User/Init VM
#define USER_RAM_MAX 2*1024



/* These are the limits for the entire script, if being loaded entirely in memory.
 * Not meaningful if we are streaming code.
 * Use these with the amxlimit pragma. For example:
 *   #pragma amxlimit SENSORS_MAX
 */
 
#define SENSORS_MAX SENSORS_RAM_MAX
#define MAIN_MAX MAIN_RAM_MAX
#define AUX_MAX AUX_RAM_MAX
#define USER_MAX USER_RAM_MAX

