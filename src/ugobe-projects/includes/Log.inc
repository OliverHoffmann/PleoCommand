/* $Id$ */

/**
 * @file
 * Log System interface
 *
 * Defines functions for logging output
 *
 * Copyright (c) 2007,2008 Ugobe, Inc. 
 */
 
#if defined _log_included
  #endinput
#endif

#define _log_included

#include "common/message_type.inc"

/**
 * @brief get a character from the serial or USB port
 * @return next character available
 */
native getchar();

/**
 * @brief send a character to the serial or USB port
 * @param character to send
 * @return none
 */
native void: putchar(c);

/**
 * @brief test whether there is data from the serial port ready
 * @return whether there is a character ready
 */
native bool: haschar();


/**
 * @brief print a string to the serial or USB port
 * @param array of characters to send
 * @return none
 */
native void: print(const string[]);

/**
 * @brief print a formatted string to the serial or USB port
 * @param format string, with embedded %? place holders
 * @param parameters to send, which match the format string
 * @return none
 * @note we currently support format specifiers include: %c, %s,
 * %o, %i, %d, %u, %x. sub-specifiers include 0-9, l, L. See the
 * Standard C printf reference for more information.
 */
native void: printf(const string[], ...);


/**
 * @brief print a message tagged with a type
 * @param type ORed message type values
 * @param string format string of following variable
 * @return none
 */
native void: Message(message_type: type, const string[], ...);

/**
 * @brief print a Debug message to the log
 * @param string format string of following variable
 * @return none
 */
native void: Debug(const string[], ...);

/**
 * @brief print an Info(rmational) message to the log
 * @param string format string of following variable
 * @return none
 */
native void: Info(const string[], ...);

/**
 * @brief print a Warning message to the log
 * @param string format string of following variable
 * @return none
 */
native void: Warning(const string[], ...);

/**
 * @brief print an Error message to the log
 * @param string format string of following variable
 * @return none
 */
native void: Error(const string[], ...);

/**
 * @brief enable log output based on message types
 * @param which message types to enable
 * @return none
 */
native void: log_enable(message_type: types);

/**
 * @brief disable log output based on message types
 * @param which message types to enable
 * @return none
 */
native void: log_disable(message_type: types);

/**
 * @brief write log messages to a named file. note this is in
 *  addition to being written to the active serial port.
 * @param file name to log to.
 * @return boolean whether the log file was successfully opened
 * @note this will write to the active device. if that device is the
 * data flash, this script must have write permissions there. if the 
 * active device is the SD Card, and there is no SD Card
 */
native bool: log_set_file(const file_name[]);

/**
 * @brief flush the current log file, if any.
 * @return none
 */
native void: log_flush();
