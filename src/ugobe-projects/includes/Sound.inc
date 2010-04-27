/**
 * @file
 * Sound interface
 *
 * Primitives provided by the sound subsystem to play sound files
 * by name or generated ID.
 *
 * Sounds are currently normally played during motion playback, using 
 * sound triggers embedded in the motions files. During the build process,
 * WAVe files are converted to Ugobe Sound File (USF) format.
 * Currently, they must be 11k, 8-bit mono.
 *
 * Note that when a sound ID or name is passed to the native code, the file
 * will be looked for in the current drive (see chdrive)
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */
 
#if defined _sound_included
  #endinput
#endif

#define _sound_included


enum Sound {
    Sound_Any = 0,
    Sound_Invalid = 0
};

/**
 * @brief play a sound
 * @param name the sound ID generated at build time
 * @return Sound: handle on success. Sound_Invalid if failed
 */
native Sound: sound_play(sound_name: sound, bool: interrupt = false);


/**
 * @brief play a sound given a file name
 * @param file name the sound file
 * @return Sound: handle on success. Sound_Invalid if failed
 */
native Sound: sound_play_file(const string[]);


/**
 * @brief stop the currently playing sound
 * @param name the sound ID generated at build time
 * @return success true for successful load and play, false otherwise
 */
native bool: sound_stop({Sound,sound_name}: sound = Sound_Any);


/**
 * @brief query whether a sound is playing
 * @param name the sound ID generated at build time
 * @return success true indicates a sound playing. false indicates no sound is playing
 */
native bool: sound_is_playing({Sound,sound_name}: sound = Sound_Any);


/**
 * @brief tell the sound system which sound lookup table to use
 * @param name the command id to use
 * @return none
 */
native sound_command(command_name: name, bool: interrupt = false);


/**
 * @brief set the global sound volume
 * @param new volume percentage adjustment. 10-200 range, 100 being normal.
 * @return none
 */
native sound_get_volume();


/**
 * @brief set the global sound volume
 * @param new volume value, in percent of nominal. 10-200 range
 * @return none
 */
native sound_set_volume(volume);


/**
 * @brief set the global sound speed, or pitch
 * @param new speed adjustment, in percent. 25-200 range, 100 being normal.
 * @return none
 */
native sound_get_speed();


/**
 * @brief set the global sound volume
 * @param new volume value. 0-100 range
 * @return none
 */
native sound_set_speed(speed);
