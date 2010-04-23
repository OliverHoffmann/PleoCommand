/* $Id$ */

/**
 * @file
 * Joint/Motor System interface
 *
 * Functions for low-level control of the joints or motors
 * of this life form. Note that these Joint IDs vary from one
 * lifeform to another, yet these functions should still apply.
 *
 * Look in the life form specific includes for those functions
 * which are unique to that device.
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */
 
#if defined _joint_included
  #endinput
#endif

#define _joint_included


// Pleo joint angles:
//  zero is neutral
//  neutral is vertical for arms and legs
//  neutral is horizontal for tail and neck and torso
//  positive forward and up and right
//  negative back and down and left

// move joint names to separate file so it can be used directly from C
#if TARGET == TARGET_PLEO
	#include "pleo/joints.inc"
#endif

/**
 * @enum angle_type
 * enumeration to tell the native code what units are being
 * passed in for joint control
 */
enum angle_type {
    angle_degrees,  /** 'true' angle in degrees */
    angle_vr,       /** underlying VR values */
    angle_mu        /** motor units */
};

/**
 * @brief move a given joint to a specific position
 * @param joint ID. see joints.inc
 * @param angle to which you want the joint to move
 * @param speed the speed at which to move, in angle_type per second
 * @param type of unit to use: degrees or VR
 * @return no return value
 */
native joint_move_to(joint_name: joint, angle, speed = 128, angle_type: type = angle_degrees);

/**
 * @brief query a given joint for its current position
 * @param joint ID. see joints.inc
 * @param type of value to return: degrees or VR
 * @return position in angle
 */
native joint_get_position(joint_name: joint, angle_type: type = angle_degrees);


enum joint_attribute {
    ja_none,        // N/A
    ja_status,      // RO
    ja_position,    // RO
    ja_pwm,         // RO
    ja_load,        // RO
    ja_speed,       // RO
    ja_time,        // RO
    ja_setpoint,    // RO
    ja_deadband,    // RW
    ja_threshold,   // RW
};

/**
 * @brief query a joint for some current status parameter
 * @param joint ID. see joints.inc
 * @param type of information to return (see joint_attribute enum)
 * @return value of the 
 */
native joint_get_attribute(joint_name: joint, joint_attribute: type);

/**
 * @brief set an operating property for a joint
 * @param joint ID. see joints.inc
 * @param type of information to set (see joint_attribute enum)
 * @param new value of attribute you are setting.
 * @return the previous value of this attribute
 */
native joint_set_attribute(joint_name: joint, joint_attribute: type, value);

/**
 * @brief query a given joint for its current status
 * @param joint ID. see joints.inc
 * @return status of this joint
 */
stock joint_status: joint_get_status(joint_name: joint)
    { return joint_get_attribute(joint, ja_status); }

/**
 * @brief query a given joint for its minimum angle value
 * @param joint ID. see joints.inc
 * @return minimum position in angle
 */
native joint_get_min(joint_name: name, angle_type: type);

/**
 * @brief set a joints minimum value
 * @param joint ID. see joints.inc
 * @param angle new angle to set for the minimum
 * @param type of unit to use to set the minimum: degrees or VR
 * @return minimum position in angle
 * @todo can this be replaced with joint_calivrate_reset?
 */
native joint_set_min(joint_name: joint, angle, angle_type: type);

/**
 * @brief query a given joint for its maximum angle value
 * @param joint ID. see joints.inc
 * @return maximum position in angle
 */
native joint_get_max(joint_name: joint, angle_type: type);

/**
 * @brief set a joints maximum value
 * @param joint ID. see joints.inc
 * @param angle new angle to set for the maximum
 * @param type of unit to use to set the maximum: degrees or VR
 * @return minimum position in angle
 * @todo can this be replaced with joint_calivrate_reset?
 */
native joint_set_max(joint_name: joint, angle, angle_type: type);

/**
 * @brief query a given joint for its neutral angle value
 * @param joint ID. see joints.inc
 * @return neutral position in angle (should always be 0)
 */
native joint_get_neutral(joint_name: joint, angle_type: type);

/**
 * @brief set a joints neutral value
 * @param joint ID. see joints.inc
 * @param angle new angle to set for the minimum
 * @param type of unit to use to set the minimum: degrees or VR
 * @return minimum position in angle
 * @todo can this be replaced with joint_calivrate_reset?
 */
native joint_set_neutral(joint_name: joint, angle, angle_type: type);

/**
 * @brief set the position offset for this joint
 * @param joint ID. see joints.inc
 * @param angle amount to offset this joint from neutral, in angles
 * @return  none
 */
native void: joint_set_offset(joint_name: name, angle);

/**
 * @brief get the position offset for this joint
 * @param joint ID. see joints.inc
 * @return angle amount this joint is offset from neutral, in angles
 */
native joint_get_offset(joint_name: name);

/**
 * @brief define who controls the given joint
 * @param joint ID. see joints.inc
 * @param who defines who controls this joint. 0 means that the motion or command will control
 *   the joint. 1 means the script or API level will control the joint (locking out any motions
 *   or commands)
 * @return  none
 */
native void: joint_control(joint_name: name, who);

/**
 * @brief get the joint string name from the firmware, given a joint ID
 * @param joint ID. see joints.inc
 * @param buffer to fill in the joint name
 * @param size of buffer provided
 * @return  none
 * @todo add a packed parameter
 */
native joint_get_name(joint_name: joint, name[], length = sizeof name);


/**
 * @brief call the native code calibration routine for a joint
 * @param joint name. see joints.inc
 * @param flag to indicate min/max/neutral or max/min/neutral calibration order
 * @param PWM value to use when driving towards min VR (valid values are 1 to 33; 33 is default for most joints)
 * @param PWM value to use when driving towards max VR (ditto)
 * @param flag to indicate whether user can adjust neutral using neck/tail, then hit wakeup; false means auto -- no wakeup needed
 * @return  whether the calibration completed successfully
 */
native bool: joint_calibrate(joint_name: joint, bool:reverse_order = false, pwm_min_vr = 0, pwm_max_vr = 0, bool:user_adjust = true);

/**
 * @brief reset all limits for this joint to their default
 * @param joint name. see joints.inc
 * @return none
 */
native joint_calibrate_reset(joint_name: joint);

/**
 * @brief change the allowed range of motion for a joint.
 * @param joint name. see joints.inc
 * @param minimum angle in degrees
 * @param neutral angle in degrees
 * @param maximum angle in degrees
 * @return  whether the change was accepted 
 */
native bool: joint_set_angle_limits(joint_name: joint, min_angle, neutral, max_angle);


/**
 * @brief whether to turn on logic that detects joint stall conditions
 * @param enable true if stall detection should be enabled, false to disable
 * @return none
 */
native joint_set_stall_detect_enable(bool: enable);

/**
 * @brief get current setting for stall detection code
 * @return bool true if stall detection is enabled, false if disabled
 */
native bool: joint_get_stall_detect_enable();

/**
 * @brief find out whether or not a joint is moving
 * @param joint name. see joints.inc
 * @return true if the joint is moving, false if it is not
 */
native bool: joint_is_moving(joint_name: joint);
