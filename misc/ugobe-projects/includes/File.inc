/* $Id$ */

/**
 * @file
 * File System interface
 *
 * Defines functions to read and write files on SD Card and
 * DataFlash.
 *
 * Path names are of the form "<drive>:/<dir>/<file>.<ext>", where
 * drive is currently 'a' (SD Card) or 'b' (Dataflash). Note the use
 * of the '/' as the directory separator.
 *
 * Note: though currently not implemented, our intention it to have
 * some sort f access control from script. That is, some functions -
 * file_delete, file_copy, etc. - may require elevated permissions
 * to work. This will be similar in function to the 'access' monitor
 * command.
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */
 
#if defined _file_included
  #endinput
#endif

#define _file_included


/**
 * @enum filemode
 * enumeration to determine how to open a file
 */
enum filemode {
    io_read = 0,            /** file must exist */
    io_write = 1,           /** creates a new file for read/write */
//    io_readwrite = 2,       /** opens an existing file, or creates a new file */
    io_append = 3,          /** appends to file. file must exist */
};

/**
 * @enum seek_offset
 *  wrt what point do we seek for the file_seek call
 * NOTE: only seek_set is currently supported
 */
enum seek_offset {
    seek_set,               /** see form beginning of file */
    seek_cur,               /** seek from current file position */
    seek_end                /** seek from end of file. offset value is absolute distance */
};

/** 
 * @enum device
 * The devicesource is where files are stored. Can be on SD
 * Card or DataFlash
 */
enum device {
    device_sd,          /** SD Card device, also known as 'a:' */
    device_df,          /** Internal DataFlash device, also known as 'b' */
    };
    
/**
 * @brief change the currently active device or 'drive'
 * @param device the device to change to. one of the device enums 
 * @return true on successful device change, false on failure
 */
native bool: device_change(device: dst);


/**
 * @brief open a file
 * @param file full path name of a file to open
 * @return handle of open file. 0 on failure
 * @todo ability to specify text or binary mode
 */
native File: file_open(const file_name[], filemode: mode = io_read);

/**
 * @brief read a line from an open file. needs Unix or Mac termination, NOT Windows/DOS
 * @param file handle returned from the file_open
 * @param line buffer to store line info. must be one greater than maximum line length to adjust for terminator
 * @param packed whether the native code should pack the returned string
 * @param size of the buffer, in cells! no need to pass in, but if so, ensure it is in cell units, not characters or bytes
 * @return number of characters written to the buffer, not counting null terminator
 */
native file_gets(File: file, line[], bool: packed = false, size = sizeof line);

/**
 * @brief write a line to an open file
 * @param file handle returned from the file_open
 * @param line buffer to write
 * @return number of characters written
 */
native file_puts(File: file, const line[]);

/**
 * @brief read binary data from an open file
 * @param file handle returned from the file_open
 * @param line buffer to use
 * @param packed whether the native code should pack the returned data
 * note we do note use the default size = sizeof line construct, since that passes the
 * number of cells, not bytes or characters
 * @param size of the buffer being passed in
 * @return number of characters read
 */
native file_read(File: file, line[], bool: packed = true, size /*= sizeof line*/);

/**
 * @todo NOT COMPLETELY IMPLEMENTED
 * @brief read binary data from an open file
 * @param file handle returned from the file_open
 * @param line buffer to use
 * @param size of the buffer being passed, in characters
 * note we do note use the default size = sizeof line construct, since that passes the
 * number of cells, not bytes or characters
 * @return number of characters read
 */
native file_write(File: file, const line[], size /*= sizeof line*/);

/**
 * @todo NOT COMPLETELY IMPLEMENTED
 * @brief write a formatted line to an open file
 * @param file handle returned from the file_open
 * @param format string
 * @return number of characters written
 */
native file_printf(File: file, const string[], ...);

/**
 * @brief seek the read/write pointer to a given position within a file
 * @param file handle returned from the file_open
 * @param line buffer to write
 * @param size of the buffer being passed ing
 * @return new position of seek pointer within file
 */
native file_seek(File: file, offset, seek_offset: origin = seek_set);

/**
 * @brief close a file
 * @param file handle returned by the file_open call
 * @param flush whether to flush the file before closing.
 * @return none
 */
native file_close(File: file, bool: flush = true);

/**
 * @brief flush any outstanding writes to the file
 * @param file handle returned by the file_open call
 * @return none
 */
native file_flush(File: file);

/**
 * @brief get the size of a file
 * @param File handle of an open file
 * @return size of file, in bytes
 */
native file_get_size(File: file);


/**
 * @brief start a folder/directory traversal
 * @param pattern of the files to find
 * @param buffer to store the result in, if successfull. should be at least 36 characters
 * @param packed whether to pack the result in the name buffer.
 * @param len length of the name buffer passed in, in cells
 * @return boolean whether the call succedded
 */
native Find: find_first(const pattern[], name[], bool: packed = true, len = sizeof name);

/**
 * @brief continue a folder/directory traversal
 * @param buffer to store the result in, if successfull
 * @param packed whether to pack the result in the name buffer.
 * @param len length of the name buffer passed in, in cells
 * @return boolean whether the call succeeded. false on end of folder list
 */
native bool: find_next(Find: handle, name[], bool: packed = true, len = sizeof name);


/**
 * @brief check to see if a file exists
 * @param file full path name to search for. can include a drive (a: or b:). if not
 *    included, then uses the current directory.
 * @return boolean whether the file exists or not
 */
native bool: file_exists(const file_name[]);

/**
 * @brief copy a file from one location to another 
 * @param source path name
 * @param destination path name
 * @return bool true on successful copy, false otherwise
 */
native bool: file_copy(const src[], const dst[]);

/**
 * @brief delete a file
 * @param full path name of a file
 * @return bool successful delete or not.
 * @note this function requires write permission to the dataflash
 */
native bool: file_delete(const file_name[]);
