/* $Id$ */

/**
 * @file
 * Camera interface
 *
 * Interface to the Camera system of this life form.
 * 
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */

#if defined _camera_included
  #endinput
#endif

#define _camera_included

// Must be the same in the Pleo firmware, and so ought
// to be in a shared include file.
#define FOV_H_DEG 50
#define FOV_V_DEG 40


/**
 * @brief enable or disable all camera functionality
 * @param whether to enable (true) or disable (false)
 * @return none
 */
native void: camera_enable(bool: enable);

/**
 * @brief enable camera (color) tracking
 * @param whether to enable (true) or disable (false)
 * @return none
 */
native void: camera_track_enable(bool: enable);

/**
 * @brief sets an entry in a colormap manually
 *
 * @param entry 0-7, the entry to set
 * @param  rhi high for the red   channel
 * @param  rlo low  for the red   channel 
 * @param  ghi high for the green channel
 * @param  glo low  for the green channel 
 * @param  bhi high for the blue  channel 
 * @param  blo low  for the blue  channel
 * @return void
 */
native void: camera_set_colormap(entry, rhi, rlo, ghi, glo, bhi, blo);

/**
 * @brief sets the camera to a preset colormap
 * @param name of preset colormap to use. see pleo/properties.inc
 * @return void
 */
native void: camera_set_color_preset(color_map_preset: preset);

/**
 * @brief requests camera average for a given window
 * @param x1 window upper left x 
 * @param y1 window upper left y
 * @param x2 window lower right x
 * @param y2 window lower right y
 * @return void
 */ 
native void: camera_request_average(x1, y1, x2, y2);

/** 
 * @brief receives camera average for a given window
 *        needs to be called within a second after a request
 *        to succeed. will also fail while the camera has not 
 *        yet responded to the request
 * @param &y_b receiving Y or G average
 * @param &u_g receiving U or B average
 * @param &v_r receiving V or R average
 * @return success 
 */ 
native bool: camera_get_average(&y_g, &u_b, &v_r);


