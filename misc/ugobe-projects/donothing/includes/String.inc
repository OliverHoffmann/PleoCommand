/* $Id$ */

/**
 * @file
 * String interface
 *
 * String module. Most prototypes taken from the amxString module
 * that is documented in the Pawn documentation.
 *
 * Copyright (c) 2007,2008 Ugobe, Inc.
 */
 
#if defined _string_included
  #endinput
#endif

#define _string_included


/**
 * @brief return the string length 
 * @param string to get the length of. Should work on both packed and unpacked strings
 * @return length in characters of the input string
 */
native string_length(const string[]);


/**
 * @brief determine whether the passed string is packed or not.
 * @param string string to check. note that it needs to have some content, or it cannot be determined
 *    whether the string is packed or not.
 * @return bool is the string backed. true is packed. false if unpacked or unknown
 */
stock bool: string_is_packed(const string[])
    { return !(0 <= string[0] <= ucharmax); }


/**
 * @brief compare two strings for equivalence
 * @param string one to compare
 * @param string two to compare
 * @param bool which determines whether case will be considered. default is false
 * @return 0 if the strings are equivalent
 */
native string_compare(const string1[], const string2[], bool: ignorecase=false, length=cellmax);


/**
 * @brief append one string to the end of another
 * @param destination string
 * @param source string
 * @return void
 */
native string_concat(dest[], const source[], length = sizeof dest);
    

/**
 * @brief copy one string to another
 * @param destination string
 * @param source string
 * @return void
 */
native string_copy(dest[], const source[], length = sizeof dest);


/**
 * @brief formatted print to a string buffer
 * @param dest of characters to print to
 * @param size available space in dest buffer
 * @param pack whether the dest string should be packed or not
 * @param format string. see printf
 * @return none
 */
native string_printf(dest[], size=sizeof dest, bool: pack=false, const format[], ...);


/**
 * @brief break up a string into parts
 * @param source string
 * @param tokens to use as delimiters
 * @param ... strings to hold the result. need as many as tokens there are
 * @return void
 */
native string_tokenize(const string[], const delimiters[], ...);


/**
 * @brief convert an integer to a string
 * @param number the value to convert to a string
 * @param base the number base to use for conversion, 8, 10 or 16
 * @param buffer destination string buffer to put the result
 * @param size the amount of space to use to put the value
 * @return number of characters filled in the buffer
 * @todo make this into a proper string_printf() function
 */
native itoa(number, base, buffer[], size = sizeof buffer);


/**
 * @brief convert a string to an integer
 * @param buffer the buffer that contains a number to convert
 * @return number the resultant value. 0 on error
 */
native atoi(const buffer[]);
