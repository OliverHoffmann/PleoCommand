/* $Id$ */

/**
 * @file
 * Animation System interface
 *
 * Primitives provided by the animation system to execute command files. Command
 * files are lists of motions files tagged with properties to allow the firmware
 * to choose the most applicable motion at any one time.
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */

#if defined _animation_included
  #endinput
#endif

#define _animation_included


/**
 * @brief execute a command. this version will stop any existing command
 * @param name a command to execute
 * @param number of loops to exececute this command. 0 means no loops, play once
 * @return opaque handle representing the executing command. 0 on error
 */
native Command: command_exec(command_name: command, loops=0);

stock Command: command_play(command_name: command, loops=0)
    { return command_exec(command, loops); }
    

/**
 * @brief query whether a given command is active/playing
 * @param handle returned from other command functions
 * @return success or failure code
 */
native bool: command_is_playing({Command,command_name}: command);


/**
 * @brief add a new command to play. returns a handle for the future remove
 * @param name of command to play
 * @return opaque handle representing the executing command. zero on error
 */
native Command: command_add(command_name: command, loops=0);


/**
 * @brief remove a command currently playing
 * @param handle returned from other command functions
 * @return success or failure code
 */
native bool: command_remove({Command,command_name}: command);
