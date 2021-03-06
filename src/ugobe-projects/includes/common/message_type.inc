  enum message_type
  {
    MSG_NONE        = 0,
    MSG_FATAL_ERROR = 1 << 0,
    MSG_ERROR       = 1 << 1,
    MSG_WARNING     = 1 << 2,
    MSG_INFO        = 1 << 3,
    MSG_DEBUG       = 1 << 4,
    MSG_MONITOR     = 1 << 5,
    MSG_MONINIT     = 1 << 6,
    MSG_MONUPDATE   = 1 << 7,
    MSG_LOWLEVEL    = 1 << 8,
    MSG_HIGHLEVEL   = 1 << 9,
    MSG_VM          = 1 << 10,
    MSG_SENSOR      = 1 << 11,
    MSG_JOINT       = 1 << 12,
    MSG_MOTION      = 1 << 13,
    MSG_SOUND       = 1 << 14,
    MSG_TIMING      = 1 << 15,
    MSG_IO          = 1 << 16,
    MSG_ANIM        = 1 << 17,
    MSG_DRIVE       = 1 << 18,
    MSG_EMOTION     = 1 << 19,
    MSG_SCRIPT      = 1 << 20,
    MSG_CAM         = 1 << 21,
    MSG_ATTENTION   = 1 << 22,
    MSG_PROPERTY    = 1 << 23,
    MSG_SEQ         = 1 << 24,
    MSG_NXP	        = 1 << 25,
    MSG_STATS       = 1 << 26,
    MSG_RESOURCE    = 1 << 26,
    MSG_PLOG        = 1 << 27,
    MSG_APPLICATION = 1 << 28,
    MSG_MANAGER     = 1 << 29,
    MSG_ALL         = ~0
  };

