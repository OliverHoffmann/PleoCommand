/* $Id$ */

/**
 * @file
 * Property definitons
 *
 * Pleo property IDs and other constants
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */


/**
 * @enum property_name
 * @brief this enumeration defines the system properties that both script and LifeOS know about
 */
enum property_name 
{
    property_none                =     0, /** name used to specify 'no property' */
    property_min                 =     1, /** the number of the lowest defined property (useful for loops) */
    property_arousal             =     1, /** not used by LifeOS */
    property_valence             =     2, /** not used by LifeOS */
    property_stance              =     3, /** not used by LifeOS */
    property_age                 =     4, /** not used by LifeOS */
    property_damage              =     5, /** not used by LifeOS */
    property_energy              =     6, /** not used by LifeOS */
    property_health              =     7, /** not used by LifeOS */
    property_mood                =     8, /** not used by LifeOS */
    property_command             =     9, /** currently active command */
    property_command_status      =    10, /** current command status (per command_status_enum) */
    property_layer               =    11, /** layer of the current animation */
    property_probability         =    12, /** not used by LifeOS */
    property_motion              =    13, /** currently active motion */
    property_command_pending     =    14, /** currently pending command */
    property_behavior_status     =    15, /** current behavior vm status (per command_status_enum) */
    property_script_status       =    15, /** alias for   property_behavior_status */
    property_fatigue             =    16, /** not used by LifeOS */
    property_direction           =    17, /** not used by LifeOS */
    property_pose                =    18, /** not used by LifeOS */
    property_sequence            =    19, /** not used by LifeOS */
    property_platform            =    20, /** not used by LifeOS */
    property_behavior            =    21, /** not used by LifeOS */
    property_script              =    21, /** not used by LifeOS */
    property_speed               =    22, /** not used by LifeOS */
    property_neutral             =    23, /** not used by LifeOS */
    property_pickedup            =    24, /** not used by LifeOS */
    property_stand               =    25, /** not used by LifeOS */
    property_liedown             =    26, /** not used by LifeOS */
    property_trick_step          =    27, /** not used by LifeOS */
    property_trick_id            =    28, /** not used by LifeOS */
    property_sound               =    29, /** currently active sound */
    property_drive               =    30, /** currently active drive */
    property_motion_time         =    31, /** time left in this motion (in ms) */
    property_attn_track_weight   =    32, /** minimum size of anobject to be tracked */
    property_attn_hold_flags     =    33, /** bitmask representing which touch sensors are held */
    property_cam_img_progress    =    34, /** current status of the camera system */
    property_attn_track_mindist  =    35, /** minimum distance an object must move before a tracking event is generated */
    property_attn_track_move     =    36, /** allow LifeOS to move the neck for tracking (0 or 1) */ 
    property_attn_p2p_timeout    =    37, /** time after which pleo considers a p2p conversation abandoned */
    property_max,                         /** one higher than the last system property (useful for loops) */
    property_limit               = 65535  /** force this enum to be a 16 bit value */
};


/**
 * @enum color_map_preset
 * @brief this enumeration defines the different color maps that can be selected 
 * for object tracking using the camera
 */
enum color_map_preset 
{
    color_map_none   = 0, /** placeholder for no colormap specified */
    color_map_red    = 1, /** colormap for red objects              */
    color_map_green  = 2, /** colormap for green objects            */
    color_map_blue   = 3, /** colormap for blue  objects            */
    color_map_white  = 4, /** colormap for white objects            */
    color_map_yellow = 5, /** colormap for yellow objects           */
    color_map_auto   = 6, /** generate a colormap based on the current histogram   */
    color_map_custom = 7, /** generate a colormap based on the current RGB average */
    color_map_max         /** colormap for green objects            */  
};


/**
 * @enum compare_name
 * @brief this enumeration defines operators for comparisons. used in wait object
 */
enum compare_name 
{
    compare_none,           /** no comparison set */
    compare_equal,          /** attribute and value must compare exactly */
    compare_not_equal,      /** attribute and value must be different */
    compare_less_than,      /** attribute is less than value */
    compare_greater_than    /** attribute is greater than value */
};

/**
 * @enum config_name
 * @brief this enumeration defines different Pleo configuration attributes
 */
enum config_name 
{
    config_access = -1,
    config_none,
    config_serial_is_usb,   /** return 1 if serial communication is happening over USB. 0 otherwise */
    config_monitor_echo,    /** Turn on or off monitor echo feedback */
    config_log_color,       /** Turn on or off ASCI color coding */
    config_df_free,         /** how much free space on DF */
};

/**
 * @enum version_name
 * @brief this enumeration defines the different sources of version
 * information available. Used for the version_get call.
 */
enum version_name 
{
    version_none,
    version_serial,           /** Serial number of this Pleo */
    version_arm0,             /** Version of firmware in the body */
    version_arm1,             /** Version of firmware in the head */
    version_motor0,           /** Version of firmware running on motor controller 0 */
    version_motor1,           /** Version of firmware running on motor controller 1 */
    version_motor2,           /** Version of firmware running on motor controller 2 */
    version_motor3,           /** Version of firmware running on motor controller 3 */
    version_hardware,         /** Unique Hardware ID of this Pleo */
    version_board_id,         /** PCB revision ID of this Pleo */
    version_app_df,           /** Version of application stored in DataFlash, if any */
    version_app,              /** Version of currently executing application */
    version_boot,             /** Version of boot loader */
};
