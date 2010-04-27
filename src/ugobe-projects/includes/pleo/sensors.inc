/* $Id$ */

/**
 * @file
 * Sensor definitons
 *
 * Pleo sensor IDs and related constants for Sensor System
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */
 

/**
 * @enum sensor_name
 * @brief this enumeration defines
 */
enum sensor_name 
{
    SENSOR_NONE          =    -1, /** name used to specify 'no sensor' */
    SENSOR_MIN           =     0, /** number of the lowest defined sensor (useful for loops) */
    SENSOR_BATTERY       =     2, /** Battery level (0-100) */
    SENSOR_IR            =     3, /** IR data received */
    SENSOR_IR_ACTIVITY   =     4, /** IR has received a code of. DISABLED */
    SENSOR_TOUCH_FIRST   =     6, /** Alias for the first touch sensor */ 
    SENSOR_HEAD          =     6, /** Head touch sensor 0=not touched 1=touched */
    SENSOR_CHIN          =     7, /** Chin touch sensor 0=not touched 1=touched */
    SENSOR_BACK          =     8, /** Back touch sensor 0=not touched 1=touched */
    SENSOR_LEFT_LEG      =     9, /** Left Leg touch sensor 0=not touched 1=touched */
    SENSOR_RIGHT_LEG     =    10, /** Right Leg touch sensor 0=not touched 1=touched */
    SENSOR_LEFT_ARM      =    11, /** Left Arm touch sensor 0=not touched 1=touched */
    SENSOR_RIGHT_ARM     =    12, /** Right Arm touch sensor 0=not touched 1=touched */
    SENSOR_ARSE          =    13, /** Arse touch sensor 0=not touched 1=touched */
    SENSOR_TAIL          =    13, /** Alias for arse touch sensor 0=not touched 1=touched */
    SENSOR_TOUCH_LAST    =    13, /** Alias for the last touch sensor  */
  
    SENSOR_FOOT_FIRST    =    14, /** Alias for the first foot switch */
    SENSOR_FRONT_LEFT    =    14, /** front left foot switch */
    SENSOR_FRONT_RIGHT   =    15, /** front right foot switch */
    SENSOR_BACK_LEFT     =    16, /** back left foot switch */
    SENSOR_BACK_RIGHT    =    17, /** back right foot switch */
    SENSOR_FOOT_LAST     =    17, /** Alias for the last foot switch */

    SENSOR_CARD_DETECT   =    18, /** SD Card is present */
    SENSOR_WRITE_PROTECT =    19, /** SD Cards write protect tab is set (software only)*/
    SENSOR_LEFT_LOUD     =    20, /** Absolute loudness of left microphone*/
    SENSOR_LIGHT         =    21, /** Absolute light light (0-100)*/
    SENSOR_RIGHT_LOUD    =    22, /** Absolute loudness of right microphone*/
    SENSOR_OBJECT        =    23, /** Object in front of Pleo (reflected IR)*/
    SENSOR_MOUTH         =    24, /** Something blocking mouth IR*/
    SENSOR_SOUND_DIR     =    26, /** Sound direction */
    SENSOR_LIGHT_CHANGE  =    27, /** Light level change, lighten or darken*/
    SENSOR_SOUND_LOUD    =    28, /** Absolute loudness of ambient sound (0-100)*/
    SENSOR_TILT          =    29, /** Current orientation (per tilt_name enum)*/
    SENSOR_TERMINAL      =    30, /** line of text from terminal/serial*/
    SENSOR_POWER_DETECT  =    31, /** Is the charger plugged in?*/
    SENSOR_USB_DETECT    =    32, /** Is the USB cable onnected*/
    SENSOR_WAKEUP        =    33, /** Wakeup / Mom button*/
    SENSOR_BATTERY_TEMP  =    34, /** Battery at critical temp?*/
    SENSOR_CHARGER_STATE =    35, /** charger state */
    SENSOR_SHAKE         =    36, /** Shake sensor activated */
    SENSOR_SOUND_LOUD_CHANGE =37, /** 1 = went above trig, 0 is went below aux_trig*/
    SENSOR_BEACON        =    38, /** value = ID of other Pleo*/
    SENSOR_BATTERY_CURRENT =  39, /** electrical current draw from battery*/
    SENSOR_PACKET        =    40, /** virtual sensor to get the packet data from the NXP processor*/
    SENSOR_MAX,
  
    //derived sensors, here to share type sensor_t
    SENSOR_DERIVED_MIN         = 100, /** start of derived sensors */
    SENSOR_EDGE_IN_FRONT       = 101, /** edge ahead detected 1=edge found, 0=no edge */
    SENSOR_EDGE_ON_LEFT        = 102, /** edge left detected 1=edge found, 0=no edge */
    SENSOR_EDGE_ON_RIGHT       = 103, /** edge right detected 1=edge found, 0=no edge */
    SENSOR_OBJECT_IN_FRONT     = 104, /** object ahead detected 1=object found, 0=no object */
    SENSOR_OBJECT_ON_LEFT      = 105, /** object left detected 1=object found, 0=no object */
    SENSOR_OBJECT_ON_RIGHT     = 106, /** object right detected 1=object found, 0=no object */
    SENSOR_TOUCH_TAP           = 107, /** touch sensor was touched and released. value=sensor_id */ 
    SENSOR_TOUCH_HOLD          = 108, /** touch sensor is being held. value=sensor_id */
    SENSOR_TOUCH_RELEASE       = 109, /** touch sensor is no longer held. value=sensor_id */
    SENSOR_TOUCH_PETTED        = 110, /** pleo is being petted (value per enum petted_name) */
    SENSOR_ABUSE               = 111, /** pleo is being abused 1=abuse started. 0=abuse stopped */
    SENSOR_PICKED_UP           = 112, /** pleo is being picked up. 1=picked up, 0=put down */
    SENSOR_TRACKABLE_OBJECT    = 113, /** an object matching the current colormap is found. direction 1-9 like a keypad, 0 when object is gone */
    SENSOR_FACE_DETECTION      = 114, /** not in use */
    SENSOR_ROCKING             = 115, /** not in use */
    SENSOR_LIGHT_BRIGHT_CHANGE = 117, /** not in use */
    SENSOR_ANTICIPATED_CUE     = 118, /** not in use */
    SENSOR_EDGE                = 119, /** bundle sensor for enabling/disabling all edge related sensors */ 
    SENSOR_TOUCH_TAP_HOLD      = 120, /** bundle sensor for enabling/disabling all touch related sensors */
    SENSOR_SEQUENCE            = 121, /** not in use */
    SENSOR_JOINT_STUCK         = 122, /** not in use */
    SENSOR_JOINT_UNSTUCK       = 123, /** not in use */
    SENSOR_TIMER               = 124, /** Virtual sensor used for timing. value=interval */
    SENSOR_MSG_RECEIVED        = 125, /** a new pleo is received */ 
    SENSOR_MSG_GONE            = 126, /** a pleo that was in contact has left or stopped communicating */
    SENSOR_MILESTONE           = 127, /** not in use */
    SENSOR_PLOG                = 128, /** virtual sensor to enable or disable the plog */

    SENSOR_DERIVED_MAX                /** last derived sensor plus one (useful for loops */
};

/**
 * @enum derived_sensor_priority
 * @brief the minimum priority a sensor helper needs to have to get a turn
 * if we get really busy, the minimal_priority should rise.
 * if a helper picks up something cool, its priority can rise  
 */
enum derived_sensor_priority 
{
    D_SENSOR_PRIORITY_NONE    =  0,
    D_SENSOR_PRIORITY_MIN     =  1,
    D_SENSOR_PRIORITY_DEFAULT =  5,
    D_SENSOR_PRIORITY_MAX     = 10  
};

/**
 * @enum tilt_name
 * @brief defines the different orientations pleo can detect
 */
enum tilt_name
{
    TILT_NONE       =     0,
    TILT_MIN        =     1,
    TILT_ON_FEET    =     1,
    TILT_LEFT_SIDE  =     2,
    TILT_RIGHT_SIDE =     3,
    TILT_ON_NOSE    =     4,
    TILT_ON_TAIL    =     5,
    TILT_ON_BACK    =     6,
    TILT_MAX
};

/**
 * @enum petted_name
 * @brief defines the different ways in which pleo can be petted
 */
enum petted_name  
{
    PETTED_NONE            =  0,
    PETTED_MIN             =  1,
    PETTED_STOPPED         =  1,
    PETTED_BACKARSE        =  2,
    PETTED_ARSEBACK        =  3,
    PETTED_HEADBACK        =  4,
    PETTED_BACKHEAD        =  5,
    PETTED_HEADBACKARSE    =  6,
    PETTED_ARSEBACKHEAD    =  7,
    PETTED_MAX
};

/**
 * @enum packet_type
 * @brief defines the different types of packets for communication with the head processor
 */
enum packet_type 
{
    PACKET_NONE    = 0,
    PACKET_AUDIO   = 1 <<  0, // 'a'
    PACKET_CAMERA  = 1 <<  2, // 'c'
    PACKET_ECHO    = 1 <<  4, // 'e'
    PACKET_IR      = 1 <<  8, // 'i'
    PACKET_LOG     = 1 << 11, // 'l'
    PACKET_MOUTH   = 1 << 12, // 'm'
    PACKET_STAT    = 1 << 15, // 'p'
    PACKET_SRAM    = 1 << 18, // 's'
    PACKET_TOUCH   = 1 << 19, // 't'
    PACKET_VERSION = 1 << 21, // 'v'
    PACKET_ERROR   = 1 << 23, // 'x'
    PACKET_EXT     = 1 << 25, // 'z'
    PACKET_ALL     = 0x7FFFFFFF
} ;

/**
 * @enum sensor_config
 * @brief this enumeration defines configuration attributes
 */
enum sensor_config 
{
    sensor_config_none,
    
    // all sensors
    sensor_config_value,            /** sensor value. RO. Not Implemented */
    sensor_config_raw_value,        /** sensor raw value. RO. Not Implemented */
    sensor_config_enabled,          /** sensor is enabled? RW */
    sensor_config_trigger,          /** sensor trigger level/value. RW */
    sensor_config_aux_trigger,      /** sensor aux trigger level/value. RW */
    sensor_config_trigger_last,     /** time since last sensor trigger. RO */
    sensor_config_trigger_time,     /** time of last sensor trigger. RO */
    sensor_config_trigger_count,    /** count of sensor triggers, in this play session. RO */
    
    // sensor beacon
    sensor_config_beacon_id,        /** the beacon ID used for Pleo 2 Pleo comm. RW. Not Implemented */
    
    // sensor timer
    sensor_config_timer_interval,   /** the duration of each timer event. RW */
    
    // sensor packet
    sensor_config_packet_filter,
};
