/* $Id$ */

/**
 * @file
 * Attention System interface
 *
 * Interface to the Attention System in Pleo.
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */
 
#if defined _attention_included
  #endinput
#endif

#define _attention_included


/**
 * @brief sets the parameters for object and edge detection
   @param  object_nv_lo_limit:  Minimum Neck Vertical angle for Object detection to occur, default = 0
   @param  edge_nv_hi_limit:    Maximum Neck Vertical angle for Edge detection to occur, default = -40 degrees 
   @param  object_ir_hi_limit:  high limit for object ir reflection. if it goes over  this, object goes on, default 40
   @param  object_ir_lo_limit:  low  limit for object ir reflection. if it goes under this, object goes off, default 20
   @param  edge_ir_hi_limit:    high limit for edge ir reflection.   if it goes over  this, edge   goes off, default 40
   @param  edge_ir_lo_limit:    low  limit for edge ir reflection.   if it goes under this, edge   goes on, default 20
   @param  object_linger:       minimum time(ms) neck v needs to be above object_nv_low_limit for object to trigger default 500ms
   @param  edge_linger:         minimum time(ms) neck v needs to be below edge_nv_hi_limit for object to trigger default 500ms
 * @return void
 */
native void: set_object_edge_parms(
                           object_nv_lo_limit,
                           edge_nv_hi_limit,
                           object_ir_lo_limit,
                           object_ir_hi_limit,
                           edge_ir_lo_limit,
                           edge_ir_hi_limit,
                           object_linger,
                           edge_linger);


/**
 * @brief reset the message slot from one sender
 * @param sender id or negative for all
 * @return success
 */
native bool: p2p_reset(from);

/**
 * @brief stops the sending of pleo2pleo messages
 * @return success
 */
native bool: p2p_stop_msg();

/**
 * @brief sets the pleo2pleo message
 * @param recipient: who should get this
 * @param data1: data byte 1
 * @param data2: data byte 2
 * @param broadcast: is this a broadcasted message (if yes recipient is ignored)
 * @return success
 */
native bool: p2p_set_msg(recipient, data1, data2, broadcast);

/**
 * @brief gets the pleo2pleo message for a specific sender
 * @param from: whose message do you want?
 * @param data1: received data byte 1
 * @param data2: received data byte 2
 * @param broadcast: was this a broadcasted message ?
 * @param repeat_count: how manieth copy of this message did we get?
 * @param repeat_count: how many messages did this sender send us in total during this encounter
 * @return success
 */
native bool: p2p_get_msg(from, &data1, &data2, &broadcast, &repeat_count, &total_count);

/**
 * @brief gets the pleo2pleo id for this pleo
 * @return the pleo2pleo id
 */
native p2p_get_id();
