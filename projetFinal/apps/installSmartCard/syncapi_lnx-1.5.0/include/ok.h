/**
****************************************************************************** 
* @file
* @brief OMNIKEY common include file
*
****************************************************************************** 
* $HeadURL$
*
* @date $LastChangedDate: 2007-06-13 14:16:44 +0200 (Wed, 13 Jun 2007) $
* @version $Revision: 161 $ ($LastChangedRevision: 161 $)
* @author $Author: mh $
* 
* Copyright © 2000 - 2007 OMNIKEY
******************************************************************************/
#ifndef _ok_h_
#define _ok_h_

/**
 * @mainpage 
 *
 * @section overview Overview on OK.H Common Include File 
 *
 * This file is intended to be used by all Omnikey written code. It's main
 * purpose is to provide a common coding style and help to keep the code
 * operating system independent.
 *
 * It contains type definitions for all common data types and pointers to
 * them. Furthermore commonly used macros and company-wide error codes are
 * defined here.
 * 
 * There is another include file called OKOS.H that contains some pragmas
 * that disable certain unimportant warnings for the Microsoft Compiler. This is
 * necessary to compile WINDOWS.H under 32-bit at warning level 4. The same
 * pragmas occur in OK.H for the case where OKOS.H is not present. They are
 * properly guarded to be activated only when MSC is in use.
 * In any C-Code the include file order should be that way:
 * 
 * \li Include 'okos.h' if necessary (e.g. an <tt>#include windows.h</tt> 
 * statement will follow). 
 * \li Include the standard compiler files like 'windows.h' and 'stdio.h'.
 * \li Include 'ok.h' to get all the Omnikey definitions and types.
 * \li Include the Omnikey module include files like 'mem.h' or 'errl.h' which
 * rely upon the presence of 'ok.h'.
 *
 * @section more  Further reading
 *
 * - @subpage packaging 
 * - @subpage errorcodes
 */

/**
 * @page packaging OK Structure Packing Conventions 
 *
 * Currently the following packing is used by the compiler settings in our
 * environment: 16-Bit PC - 2, 32-Bit Windows - 8, OS/2 - 4.
 * Every external include file that contains structures must include
 * 'okpon.h' at the beginning and 'okpoff.h' at the end. These include files
 * set the structure packing to the above values in dependence of the target
 * operating system and compiler (#pragma statement syntax depends on the
 * compiler used) overriding the compiler flags. To set a specific other
 * packing (e.g. 1) for a certain structure inside an include file use:
 * \code
 * #include okpon1.h 
 * ... define the structure that shall get packing 1 here ...
 * #include okpon.h 
 * \endcode
 * There are include files named 'OKPONx.H', where 'x' may be 1, 2, 4 or 8.
 * \attention Do NOT write #pragma statements directly into your code for reasons
 * described above!
 */

/****************************************************************************/
/* Operating system defined ?                                               */
/****************************************************************************/
#if   defined S_BOOT

#elif defined S_WNT

#elif defined S_SOLARIS

#else // NON windows
#include <config.h>
#include <pcscdefines.h>
#endif

#if !defined S_LINUX && !defined S_WNT && !defined S_SOLARIS && !defined S_MACOSX && !defined S_BOOT
   #error "No target operating system defined (e.g. S_WNT), see OK.H"
#endif

/****************************************************************************/
/* Set processor type if it's not defined                                   */
/****************************************************************************/
#if defined S_BOOT
   #if !defined P_86 && !defined P_286 && !defined P_386 && !defined P_PENTIUM
      #define P_86
   #endif
#elif defined S_WNT
   #if !defined P_386 && !defined P_PENTIUM && !defined P_ALPHA
      #define P_386
   #endif
#elif defined S_LINUX
   #if !defined P_386 && !defined P_PENTIUM
      #define P_386
   #endif
#elif defined S_SOLARIS
   #ifndef P_SPARC
      #define P_SPARC
   #endif
#elif defined S_MACOSX
  #ifndef P_POWERPC
     #define P_POWERPC
  #endif
#else
   #error "No target processor defined (e.g. P_PENTIUM), see OK.H"
#endif

/****************************************************************************/
/* Processor defined ?                                                      */
/****************************************************************************/
#if   defined P_86
#elif defined P_286
#elif defined P_386
#elif defined P_PENTIUM
#elif defined P_SPARC
#elif defined P_ALPHA
#elif defined P_POWERPC
// #elif defined P_IA64
// #elif defined P_AMD64
#else
   #error "No target operating system defined (e.g. S_WNT), see OK.H"
#endif

/****************************************************************************/
/* Integer Byte Ordering                                                    */
/****************************************************************************/
#if defined P_86 || defined P_286 || defined P_386 || defined P_PENTIUM ||\
   defined P_IA64 || defined P_AMD64
   #ifndef LITTLE_ENDIAN
      #define LITTLE_ENDIAN
   #endif
#elif defined P_SPARC
   #ifndef BIG_ENDIAN
      #define BIG_ENDIAN
   #endif
#endif

/****************************************************************************/
/* Set less supported windows version                                       */
/****************************************************************************/
#ifndef WINVER
   #define WINVER  0x0400
#endif

/****************************************************************************/
/* Disabling warnings allows usage of WIN32 <windows.h> at warning level 4  */
/****************************************************************************/
#if defined _MSC_VER
   #if defined S_WNT
      #pragma warning (disable: 4001) /**< nonstd. extension 'single line comm. */
      #pragma warning (disable: 4057) /**< 'uns. char *' differs in indirection */
      #pragma warning (disable: 4115) /**< named type definition in parentheses */
      #pragma warning (disable: 4200) /**< non.ex. zero-sized array in str/union */
      #pragma warning (disable: 4201) /**< non.ex. nameless struct/union */
      #pragma warning (default: 4209) /**< non.ex. benign typedef redefinition */
      #pragma warning (disable: 4214) /**< non.ex. bit field types other th. int */
      #pragma warning (disable: 4514) /**< unreferenced inline function removed */
   #endif
#endif /* _MSC_VER */

#include <okpon.h>

/****************************************************************************/
/* Calling conventions                                                      */
/****************************************************************************/
#if defined S_BOOT
   #define FAR     _far
   #define NEAR    _near
#elif defined S_LINUX || defined S_SOLARIS || defined S_MACOSX
   #define FAR
   #define NEAR
#endif

#if defined S_WNT
   #define ENTRY   _stdcall
   #define CENTRY  _cdecl
#else
   #define ENTRY
   #define CENTRY
#endif

/****************************************************************************/
/*   64 bit data types                                                      */
/****************************************************************************/
#if defined P_IA64 || defined P_AMD64
  #if defined(S_WNT)
  #elif defined(S_LINUX)
  #endif
#else
  #if defined(S_WNT)
    typedef __int64  LONG64;
    typedef unsigned __int64  ULONG64;
    typedef LONG64 *PLONG64;
    typedef ULONG64 FAR *PULONG64;
  #elif defined(S_LINUX)
    typedef long long LONG64;
    typedef unsigned long long ULONG64;
  #elif defined(S_MACOSX)
    typedef long long LONG64;
    typedef unsigned long long ULONG64;
  #else
    typedef double LONG64;
    typedef double ULONG64;
    typedef unsigned short BOOL;
  #endif
#endif

/****************************************************************************/
/*  File system/path                                                        */
/****************************************************************************/
#if defined S_LINUX || defined S_SOLARIS || defined S_MACOSX
  #define IN
  #define OUT
#endif
#if defined S_LINUX || defined S_SOLARIS || defined S_MACOSX
   #define OK_SEP_DRIVE      '/' /*cnst Drive separator as character, S_UNIX*/
   #define OK_SEP_PATH       '/' /*cnst Directory separator as character,
                                          S_UNIX*/
   #define OK_SEP_PATH_FIRST '/' /*cnst First directory separator as
                                          character, S_UNIX*/
   #define OK_SEP_PATH_LAST  '/' /*cnst Separator behind last directory as
                                          character, S_UNIX*/
   #define OK_SEP_EXT        '.' /*cnst Filename extension character as
                                          character, S_UNIX*/
   #define OK_SEP_DRIVE_STR  "/" /*cnst Drive separator as string, S_UNIX*/
   #define OK_SEP_PATH_STR   "/" /*cnst Path separator as string, S_UNIX*/
   #define OK_SEP_EXT_STR    "." /*cnst Filename extension character as
                                          string, S_UNIX <nl>*/
#else
   #define OK_SEP_DRIVE      ':'  /*@cnst Drive separator as character,
   default*/
   #define OK_SEP_PATH       '\\' /*@cnst Directory separator as
   character, default*/
   #define OK_SEP_PATH_FIRST '\\' /*@cnst First directory separator as
   character, default*/
   #define OK_SEP_PATH_LAST  '\\' /*@cnst Separator behind last directory
   as character, default*/
   #define OK_SEP_EXT        '.'  /*@cnst Filename extension character as
   character, default*/
   #define OK_SEP_DRIVE_STR  ":"  /*@cnst Drive separator as string,
   default*/
   #define OK_SEP_PATH_STR   "\\" /*@cnst Path separator as string,
   default*/
   #define OK_SEP_EXT_STR    "."  /*@cnst Filename extension character as
   string, default*/
#endif

/****************************************************************************/
/* Macros                                                                   */
/****************************************************************************/
#define SStoDS(x)    x
#define HIGH(v) ((BYTE) ((v) >> 8))
#define LOW(v) ((BYTE) (v))
#define HIGHW(v) ((WORD) ((v) >> 16))
#define LOWW(v) ((WORD) (v))
#define SWAPW(v) (WORD) ((LOW(v)<<8)|HIGH(v))
#ifdef S_SBFW
  #define PCSWAPW(v) v
  #define FWSWAPW(v) (WORD) ((LOW(v)<<8)|HIGH(v))
#else
  #define PCSWAPW(v) (WORD) ((LOW(v)<<8)|HIGH(v))
  #define FWSWAPW(v) v
#endif
/* @macro SWAPL(V) | Unconditional byte swapping of DWORD value.*/
#define SWAPL(v) (LONG) ((((LONG)(SWAPW(LOWW(v))))<<16)|SWAPW(HIGHW(v)))
#define SWAPUL(v) (ULONG) ((((ULONG)(SWAPW(LOWW(v))))<<16)|SWAPW(HIGHW(v)))
#ifdef S_SBFW
   /* @macro PCSWAPL(V) | Byte swapping of DWORD value, PC code only. */
  #define PCSWAPL(v) v
   /* @macro FWSWAPL(V) | Byte swapping of WORD value, firmware code only. */
  #define FWSWAPL(v) (LONG) ((((LONG)(SWAPW(LOWW(v))))<<16)|SWAPW(HIGHW(v)))
#else
  #define PCSWAPL(v) (LONG) ((((LONG)(SWAPW(LOWW(v))))<<16)|SWAPW(HIGHW(v)))
  #define FWSWAPL(v) v
#endif
/* @macro MINVAL(x, y) | Minimum of two integers. */
#define MINVAL(x, y) (((x) < (y)) ? (x) : (y))
/* @macro LO_NIB(c) | Get low nibble of BYTE value. */
#define LO_NIB(c) ( 0x0F & (c) )
/* @macro HI_NIB(c) | Get high nibble of BYTE value. */
#define HI_NIB(c) ( 0x0F & ((c) >> 4) )

/**************************************************************************/
/*  General error codes                                                   */
/**************************************************************************/

/**
 * @page errorcodes General error codes
 *
 * These are considered as well known errors throughout Omnikey code.
 * Module specific errors are defined in the module include file above
 * OKERR_STANDARD_LIMIT.
 *
 * Some of these errors are there for compatibility reasons only. So there
 * are similar error codes for the same situation (e.g. out of memory). 
 * Programmers should try to use the error codes defined in OK.H as often as possible
 * instead of inventing module specific new error codes.
 */
#define OKERR_STANDARD_LIMIT     0x82000000L  /**< Module specific error codes may be defined above this limit. */

typedef LONG  OKERR;             /**< standard OK Errorcode type */

/**************************************************************************/
/* Success codes                                                          */
/**************************************************************************/
#ifndef NO_ERROR
   #define NO_ERROR                0       /**< No error */
#endif
#define OKERR_OK                   0       /**< No error */
#define OKERR_SUCCESS              0       /**< No error */

/**************************************************************************/
/*  Parameter errors  0x81000000 -                                        */
/**************************************************************************/
#define OKERR_PARM1       0x81000000L         /**< Error in parameter 1 */
#define OKERR_PARM2       0x81000001L         /**< Error in parameter 2 */
#define OKERR_PARM3       0x81000002L         /**< Error in parameter 3 */
#define OKERR_PARM4       0x81000003L         /**< Error in parameter 4 */
#define OKERR_PARM5       0x81000004L         /**< Error in parameter 5 */
#define OKERR_PARM6       0x81000005L         /**< Error in parameter 6 */
#define OKERR_PARM7       0x81000006L         /**< Error in parameter 7 */
#define OKERR_PARM8       0x81000007L         /**< Error in parameter 8 */
#define OKERR_PARM9       0x81000008L         /**< Error in parameter 9 */
#define OKERR_PARM10      0x81000009L         /**< Error in parameter 10 */
#define OKERR_PARM11      0x8100000AL         /**< Error in parameter 11 */
#define OKERR_PARM12      0x8100000BL         /**< Error in parameter 12 */
#define OKERR_PARM13      0x8100000CL         /**< Error in parameter 13 */
#define OKERR_PARM14      0x8100000DL         /**< Error in parameter 14 */
#define OKERR_PARM15      0x8100000EL         /**< Error in parameter 15 */
#define OKERR_PARM16      0x8100000FL         /**< Error in parameter 16 */
#define OKERR_PARM17      0x81000010L         /**< Error in parameter 17 */
#define OKERR_PARM18      0x81000011L         /**< Error in parameter 18 */
#define OKERR_PARM19      0x81000012L         /**< Error in parameter 19 */

/**************************************************************************/
/*  PW        errors  0x81100000 -                                        */
/**************************************************************************/
#define OKERR_INSUFFICIENT_PRIV    0x81100000L  /**< You currently do not have the rights to execute the requested action. Usually a password has to be presented in advance. */
#define OKERR_PW_WRONG             0x81100001L  /**< The presented password is wrong */
#define OKERR_PW_LOCKED            0x81100002L  /**< The password has been presented several times wrong and is therefore locked. Usually use some administrator tool to unblock it. */
#define OKERR_PW_TOO_SHORT         0x81100003L  /**< The lenght of the password was too short.*/
#define OKERR_PW_TOO_LONG          0x81100004L  /**< The length of the password was too long.*/
#define OKERR_PW_NOT_LOCKED	   0x81100005L  /**< The password is not locked */

/**************************************************************************/
/*  ITEM      errors  0x81200000 -                                        */
/**************************************************************************/
#define OKERR_ITEM_NOT_FOUND       0x81200000L  /**< An item (e.g. a key of a specific name) could not be found */
#define OKERR_ITEMS_LEFT           0x81200001L  /**< There are still items left, therefore e.g. the directory / structure etc. can't be deleted. */
#define OKERR_INVALID_CFG_FILE     0x81200002L  /**< Invalid configuration file */
#define OKERR_SECTION_NOT_FOUND    0x81200003L  /**< Section not found */
#define OKERR_ENTRY_NOT_FOUND      0x81200004L  /**< Entry not found */
#define OKERR_NO_MORE_SECTIONS     0x81200005L  /**< No more sections */
#define OKERR_ITEM_ALREADY_EXISTS  0x81200006L  /**< The specified item alread exists. */
#define OKERR_ITEM_EXPIRED         0x81200007L  /**< Some item (e.g. a certificate) has expired. */

/**************************************************************************/
/*  General   errors  0x81300000 -                                        */
/**************************************************************************/
#define OKERR_UNEXPECTED_RET_VALUE 0x81300000L  /**< Unexpected return value */
#define OKERR_COMMUNICATE          0x81300001L  /**< General communication error */
#define OKERR_NOT_ENOUGH_MEMORY    0x81300002L  /**< Not enough memory */
#define OKERR_BUFFER_OVERFLOW      0x81300003L  /**< Buffer overflow */
#define OKERR_TIMEOUT              0x81300004L  /**< A timeout has occurred */
#define OKERR_NOT_SUPPORTED        0x81300005L  /**< The requested functionality is not supported at this time / under this OS / in this situation etc. */
#define OKERR_ILLEGAL_ARGUMENT     0x81300006L  /**< Illegal argument*/
#define OKERR_READ_FIO             0x81300007L  /**< File IO read error */
#define OKERR_WRITE_FIO            0x81300008L  /**< File IO write error */
#define OKERR_INVALID_HANDLE       0x81300009L  /**< Invalid handle*/
#define OKERR_GENERAL_FAILURE      0x8130000AL  /**< General failure. Use this error code in cases where no other errors match and it is not worth to define a new error code.*/
#define OKERR_FILE_NOT_FOUND       0x8130000BL  /**< File not found */
#define OKERR_OPEN_FILE            0x8130000CL  /**< File opening failed */
#define OKERR_SEM_USED             0x8130000DL  /**< The semaphore is currently use by an other process */

/*****************************************************************************/
/* Definitions for Compatibility to old OK code                              */
/*****************************************************************************/
typedef const CHAR  CONSTCHAR;    /**< character type */
typedef const CHAR FAR *PCONSTCHAR;

#if defined S_WNT
   typedef CONSTCHAR FAR *GPCONSTCHAR;
   typedef const VOID FAR *GPCONSTVOID;
   typedef VOID FAR *GPVOID;
   typedef CHAR FAR *GPCHAR;
   typedef WCHAR FAR *GPWSZ;
   typedef CHAR FAR *GPSZ;
   typedef WCHAR FAR *GPWCHAR;
   typedef UCHAR FAR *GPUCHAR;
   typedef BYTE FAR *GPBYTE;
   typedef CHAR FAR *GPSTR;
   typedef INT  FAR *GPINT;
   typedef SHORT FAR *GPSHORT;
   typedef USHORT FAR *GPUSHORT;
//#ifndef NT_50
   /* already defined in Win2000 */
   typedef UINT FAR *GPUINT;
//#endif
   typedef WORD FAR *GPWORD;
   typedef LONG FAR *GPLONG;
   typedef ULONG FAR *GPULONG;
   typedef LONG64 *GPLONG64;
   typedef ULONG64 *GPULONG64;
   typedef DWORD FAR *GPDWORD;
   typedef BOOL FAR *GPBOOL;
#elif defined S_LINUX || defined S_SOLARIS || defined S_MACOSX
   typedef CONSTCHAR FAR *GPCONSTCHAR;
   typedef const VOID FAR *GPCONSTVOID;
   typedef VOID FAR *GPVOID;
   typedef CHAR FAR *GPCHAR;
   typedef BYTE FAR *GPBYTE;
   typedef CHAR FAR *GPSTR;
   typedef INT  FAR *GPINT;
   typedef SHORT FAR *GPSHORT;
   typedef USHORT FAR *GPUSHORT;   
   typedef UINT FAR *GPUINT;
   typedef BOOL *LPBOOL;
#endif

   typedef CHAR FAR *LPSZ;

#include <okpoff.h>

#endif /* _ok_h_ */

