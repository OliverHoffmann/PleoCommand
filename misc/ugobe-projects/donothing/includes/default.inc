/* $Id$ */

/**
 * @file
 * default.inc
 *
 * This file hides the default.inc file that is included with the Pawn
 * distribution. The version that comes with the Pawn distribution includes some
 * modules only appropriate to run on a desktop.
 *
 */
 

#define TARGET_PLEO 100
#define TARGET_NEXT 200

#if !defined TARGET
    #define TARGET TARGET_PLEO
#endif

// what I would like is a macro here so that we do not
// a lot of #ifs in the main .inc files.
//#define TARGET_INCLUDE(%1) #include <TARGET"/"%1>


#define PLEO_1_0 100
#define PLEO_1_1 110

#if !defined PLEO_API
    #define PLEO_API PLEO_1_1
#endif

