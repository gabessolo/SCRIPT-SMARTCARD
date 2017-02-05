/*****************************************************************
/
/ File   :   pcscdefines.h
/ Author :   David Corcoran <corcoran@linuxnet.com>
/ Date   :   June 15, 2000
/ Purpose:   This provides PC/SC shared defines.
/            See http://www.linuxnet.com for more information.
/ License:   See file LICENSE
/
******************************************************************/

#ifndef _pcscdefines_h_
#define _pcscdefines_h_

#include <wintypes.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __wintypes_h__
    typedef unsigned char BYTE;
    typedef unsigned char UCHAR;
    typedef unsigned char *PUCHAR;
    typedef char *PCHAR;
    typedef unsigned short *PUSHORT;
    typedef unsigned short USHORT;
    typedef unsigned long ULONG;
    typedef unsigned long *PULONG;
    typedef const void *LPCVOID;
    typedef unsigned long DWORD;
    typedef unsigned long *PDWORD;
    typedef unsigned short WORD;
    typedef long LONG;
    typedef long RESPONSECODE;
    typedef const char *LPCTSTR;
    typedef const BYTE *LPCBYTE;
    typedef BYTE *LPBYTE;
    typedef DWORD *LPDWORD;
    typedef char *LPTSTR;
#endif

#if defined S_MACOSX
    typedef const char *LPCTSTR;
#endif

    typedef char *PCHAR;
    typedef unsigned int DWORD32;
    typedef char CHAR;
    typedef void VOID;
    typedef short SHORT;
    typedef unsigned int UINT;
    typedef unsigned int *PUINT;
    typedef int INT;
    typedef int *PINT;

#ifndef TRUE
#define TRUE 0x01
#endif

#ifndef FALSE
#define FALSE 0x00
#endif

    typedef enum {
	STATUS_SUCCESS = 0xFA,
	STATUS_UNSUCCESSFUL = 0xFB,
	STATUS_COMM_ERROR = 0xFC,
	STATUS_DEVICE_PROTOCOL_ERROR = 0xFD,
    STATUS_MORE_PROCESSING_REQUIRED = 0xFE
    } status_t;

#define MAX_RESPONSE_SIZE  264
#define MAX_ATR_SIZE       33
#define PCSCLITE_MAX_CHANNELS  16

#ifdef __cplusplus
}
#endif
#endif				/* _pcscdefines_h_ */
