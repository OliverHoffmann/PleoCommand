/* $Id$ */

/**
 * @file
 * Motion interface
 *
 * Primitives provided by the motion interface to play motion files
 * or predefined motions.
 *
 * Motions are the animation data used to animate Pleo at run-time.
 * The animations are exported from 3d Max as CSV files and converted
 * to UMF(Ugobe Motion Files) files via build scripts.
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */

#if defined _motion_included
    #endinput
#endif

#define _motion_included


enum Motion {
	// special value to pass to motion_is_playing to see if any motions are active
	Motion_Any = 0xAA,
	// return value from play representing error
    Motion_Invalid = 0xFF,
};


/**
 * @brief play a motion by ID
 * @param name of a predefined motion, usually in the generated motions.inc
 * @return handle to be used in the other motion functions. Motion_Invalid on error.
 */
native Motion: motion_play(motion_name: name);


/** 
 *  @brief play a motion by name
 *  @param name a file containing motion data. will first look for a <name>.umf. if
 * not found, will look inside any active URF file.
 *  @return handle to be used in the other motion functions, Motion_Invalid on error
 */
native Motion: motion_play_file(const file_name[]);


/**
 *  @brief pause or resume a motion
 *  @param motion handle of the motion to pause/resume, or a motion_name
 *  @param pause boolean representing whether to pause or resume
 *  @return whether the motion was successfully paused or resumed
 */
native bool: motion_pause({Motion,motion_name}: motion, bool: pause /*, bool: smooth = false*/);


/**
 *  @brief stop a motion
 *  @param motion handle returned from motion_play function, or a motion_name. special
 *       value of 0 means to top all motions.
 *  @return true on success, false on failure: already stopped, or invalid motion value passed
 */
native bool: motion_stop({Motion,motion_name}: motion);


/**
 *  @brief test if a motion is currently playing
 *  @param motion name to be tested, or the Motion: tag returned from the motion play functions
 *  @return True on playing, false on any other state
 */
native bool: motion_is_playing({Motion,motion_name}: motion);


/**
 *  @brief set the speed at which to play motions
 *  @param percent of normal playback speed
 *  @return speed it actually set it to.
 */
native motion_set_playback_speed(percent_of_normal);


/**
 *  @brief get the speed at which to play motions
 *  @return speed
 */
native motion_get_playback_speed();
