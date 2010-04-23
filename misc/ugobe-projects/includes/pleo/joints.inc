/*
 * Joint names for Pleo
 */
enum joint_name {
  JOINT_NONE            =    -1,
  JOINT_MIN             =     0,
  JOINT_RIGHT_SHOULDER  =     0,
  JOINT_RIGHT_ELBOW     =     1,
  JOINT_LEFT_SHOULDER   =     2,
  JOINT_LEFT_ELBOW      =     3,
  JOINT_LEFT_HIP        =     4,
  JOINT_LEFT_KNEE       =     5,
  JOINT_RIGHT_HIP       =     6,
  JOINT_RIGHT_KNEE      =     7,
  JOINT_TORSO           =     8,
  JOINT_TAIL_HORIZONTAL =     9,
  JOINT_TAIL_VERTICAL   =    10,
  JOINT_NECK_HORIZONTAL =    11,
  JOINT_NECK_VERTICAL   =    12,
  JOINT_HEAD            =    13,
  JOINT_MAX_PHYSICAL    =    13,
  JOINT_TRANSITION      =    14,
  JOINT_SOUND           =    15,
  JOINT_MAX
};

/*
 * Joint status values for all joints for Pleo
 */
enum joint_status {
  JOINT_STATUS_NONE 	= -1,
  JOINT_STATUS_MIN 	= 0,
  JOINT_STATUS_IDLE    	= 0,
  JOINT_STATUS_RUNNING 	= 1,
  JOINT_STATUS_STOPPED 	= 2,
  JOINT_STATUS_CANNOT_START = 3,
  JOINT_STATUS_STALLED 	= 4,
  JOINT_STATUS_NOTREACH	= 5,
  JOINT_STATUS_SLOW = 5,
  JOINT_STATUS_COMMERR = 6,
  JOINT_STATUS_CHERR = 7,
  JOINT_STATUS_NOTREADY = 8,
  JOINT_STATUS_DISPLACED = 9,
  JOINT_STATUS_MAX
};
