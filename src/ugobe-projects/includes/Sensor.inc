/* $Id$ */

/**
 * @file
 * Sensors interface
 *
 * Interface to the Sensor system of this life form.
 * 
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */

#if defined _sensor_included
  #endinput
#endif

#define _sensor_included


// get the proper sensor definitions
#if TARGET == TARGET_PLEO
    #include "pleo/sensors.inc"
#endif


/** @brief read a sensor
 *
 *  Reads the status of the given sensor. Some sensors
 *  return additional data that can be read using other
 *  primitives. The value returned is the "massaged" value;
 *  that is, it has been averaged, debounced, etc.
 *  @param name sensor to read
 *  @return sensor value of the sensor
 */
native sensor_read(sensor_name: sensor);

stock sensor_get_value(sensor_name: sensor)
    { return sensor_read(sensor); }
    

/** @brief read a sensor
 *
 *  Reads the raw value of the given sensor. That is, the
 *  last value for this sensor gathered directly from the 
 *  low-level driver.
 *  @param name sensor to read
 *  @return sensor raw value of the sensor
 */
native sensor_read_raw(sensor_name: sensor);


/** @brief write a sensor value.
 *
 *  @param name sensor to write
 *  @param value value to set for the sensor
 *  @return none
 *  @note this function should rarely, if ever, be called.
 */
native sensor_write(sensor_name: sensor, value);

    
/** @brief query a sensor for its triggered state
 *
 *  This function reads the current triggered condition of
 *  the given sensor. If the sensor is triggered, it is sending
 *  events/messages to the high-level script through the on_sensor
 *  call.
 *  @param name sensor to read
 *  @return sensor boolean representing whether this sensor is triggered
 */
native bool: sensor_is_triggered(sensor_name: sensor);

    
/** @brief reset a sensors trigger value
 *
 *  This function will reset - or untrigger - the given
 *  sensor.
 *  @param name sensor to reset
 *  @return none
 */
native sensor_reset(sensor_name: sensor);

    
/** @brief read a sensors data
 *
 *  Some sensors - like the IR and Camera - can return more than
 *  one cell of data. This function allows the script to read the
 *  data for this sensor type. 
 *  @param name sensor to read
 *  @param data the cell array used to store the returned data
 *  @return number of cells worth of data returned
 *  @todo add a packed parameter
 */
native sensor_read_data(sensor_name: sensor, data[], length = sizeof data);


/** @brief read a sensors data size
 *
 *  Some sensors - like the IR and Camera - can return more than
 *  one cell of data. This function allows the script to read the
 *  amount of data available for this sensor type. 
 *  @param name sensor to read
 *  @param data the cell array used to store the returned data
 *  @return number of cells worth of data returned
 */
native sensor_data_size(sensor_name: sensor);

    
/** @brief write array data to a sensor
 *
 *  This function allows the script to send an array of cell data to
 *  a 'sensor' or device. Currently, this only works for SENSOR_IR. The
 *  data is treated as a string. That is, it will be checked to see
 *  if it is packed or not and do the 'right' thing.
 *  @param name sensor to write to
 *  @param data data to send to the sensor/device
 *  @return number of bytes successfully written to the sensor/device
 */
native sensor_write_data(sensor_name: sensor, const data[], length = sizeof data);


/** @brief convert a sensor ID to s sensor name/string
 *
 *  Return the string representation of a sensor
 *  @param sensor ID
 *  @param buffer to return the sensor name
 *  @return number of characters put in name buffer
 *  @todo add a packed parameter
 */
native sensor_get_name(sensor_name: sensor, name[], length = sizeof name);


/** @brief get a working parameter for a sensor
 *
 *  Get a configuration parameter for a sensor
 *  @param sensor ID
 *  @param what configuration to query. see <device>/sensors.inc
 *  @return current value of this configuration parameter
 */
native sensor_get_config(sensor_name: sensor, sensor_config: type);


/** @brief set a working parameter for a sensor
 *
 *  Set a configuration paramter for a sensor
 *  @param sensor ID
 *  @param what configuration to change. see <device>/sensors.inc
 *  @param what value to set the sensor parameter to
 *  @return none
 */
native sensor_set_config(sensor_name: sensor, sensor_config: type, value);


/** @brief enable a sensor
 *
 *  This will enable the given sensor. This will turn the sensor
 *  on and allow it to trigger the high-level.
 *  @param sensor name of sensor to enable
 *  @return none
 */
stock sensor_enable(sensor_name: sensor)
    { sensor_set_config(sensor, sensor_config_enabled, true); }


/** @brief disable a sensor
 *
 *  This will disable a sensor, preventing the on_sensor
 *  function from being called for this sensor. It may also
 *  have the side effect of reducing load on the processor.
 *  @param sensor name of sensor to disable
 *  @return none
 */
stock sensor_disable(sensor_name: sensor)
    { sensor_set_config(sensor, sensor_config_enabled, false); }


/** @brief set a sensors trigger level
 *
 *  this function writes the value of the given sensor. This
 *  currently only useful for the SENSOR_TIMER to define the
 *  duration of the timer. Settings values for other sensors may
 *  have undesired effects, like triggering.
 *
 *  @param name sensor to write
 *  @param value trigger value of the sensor
 *  @return none
 */
stock sensor_set_trigger_level(sensor_name: sensor, value)
    { sensor_set_config(sensor, sensor_config_trigger, value); }

#pragma deprecated sensor_set_level

stock sensor_set_level(sensor_name: sensor, value)
    { sensor_set_config(sensor, sensor_config_trigger, value); }

/** @brief get the time between the last two triggers
 *  @param sensor ID
 *  @return the time between the last two triggers
 */
stock sensor_time_since_last(sensor_name: sensor)
    { return sensor_get_config(sensor, sensor_config_trigger_last); }


/** @brief get the last sensor trigger time
 *  @param sensor ID
 *  @return the time this sensor last triggered 
 */
stock sensor_get_trigger_time(sensor_name: sensor)
    { return sensor_get_config(sensor, sensor_config_trigger_time); }


/** @brief get the number of times this sensor has triggered 
 *  @param sensor ID
 *  @return trigger count
 */
stock sensor_get_trigger_count(sensor_name: sensor)
    { return sensor_get_config(sensor, sensor_config_trigger_count); }


/** @brief allow a sensor to be reported to script. 
 *         in contrast to enabling/disabling a sensor
 *         this function blocks the sensor from script
 *         while still allowing the rest of the firmware
 *         to use it. (to calculate other sensors).
 *  @param the sensor ID 
 *  @param call_script: true to let the sensor reach script, false to block it
 */
native sensor_call_script(sensor_name: sensor, bool: call_script);


/** @fn public bool: on_sensor(time, sensor_name:sensor, value)
 *
 * This is a function that sensor scripts can define to be notified of
 * sensor trigger events. When a sensor trigger occurs, this function is
 * invoked.
 *
 * @param time - milliseconds lifeform has been running
 * @param sensor - ID of sensor that triggered
 * @param value - sensor value 
 *
 * @return a bool specifying whether the sensor should be reset.
 *  true means reset the trigger.
 *  false means do not reset the trigger.
 */
forward public on_sensor(time, sensor_name:sensor, value);


/*
 * Timer-specific function
 */

/**
 * @brief sets the timer interval
 * @param the interval to set in milliseconds, use 0 to disable
 * @return previous interval
 */
stock set_timer_interval(interval)
    { sensor_set_trigger_level(SENSOR_TIMER, interval); }
 