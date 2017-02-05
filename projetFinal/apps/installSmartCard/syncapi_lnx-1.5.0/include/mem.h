#ifndef _INC_MEM

#define _INC_MEM



#include <okpon.h>



/****************************************************************************/

/* Funktions-Prototypen                                                     */

/****************************************************************************/



#if defined OKDEBUG && ! defined MEM_TRACE_NOT_USED

   /* for trace of mem functions; now default in debug versions;

      to enable it, set environment variable USCMEMLOG and let it point

      to the logfile (full path!)

   */

   #define MEM_TRACE

#endif



#ifdef __cplusplus

   extern "C" {

#endif



GPVOID ENTRY MemMove(GPVOID pDestination, UINT uiDestLen, GPCONSTVOID pSource, UINT uiNumber);



#if defined(_WIN32_WCE)

  #define MemSet(mem,len,bByte,uiNumber) memset(mem,(int)bByte,uiNumber)	

  #define MemCmp(dst,src,srclen) memcmp(dst,src, srclen)

  #define MemCpy(dst,dstlen,src,srclen) memcpy(dst,src, (((long)srclen)<=((long)dstlen)) ? srclen : dstlen)

  #define MemRealloc(b,s)  realloc(b,s)

  #define MemAlloc(s)      malloc(s)

  #define MemFree(p)       free(p)

#else

  #if defined MEM_TRACE

    #define MemRealloc(b,s)  MemTRealloc(b,s, __FILE__, __LINE__)

    #define MemAlloc(s)      MemTAlloc(s,__FILE__, __LINE__)

    #define MemFree(p)       do { \

                                 MemTFree(p, __FILE__, __LINE__); \

                                 p = NULL; \

                                } while (p)

  #else

    GPVOID ENTRY MemRealloc(GPVOID pBuffer, UINT uiSize);

    GPVOID ENTRY MemAlloc(UINT uiSize);

    VOID   ENTRY MemFree(GPVOID pPointer);

  #endif /* MEM_TRACE */



  GPVOID ENTRY MemSet (GPVOID pArray, UINT uiArrLen, BYTE bByte,UINT uiNumber);

  INT ENTRY MemCmp (GPCONSTVOID pArray1, GPCONSTVOID pArray2, UINT uiNumber);

  GPVOID ENTRY MemCpy (GPVOID pDestination, UINT uiDestLen, GPCONSTVOID pSource, UINT uiNumber);

#endif /* _WIN32_WCE) */



/* Memory - related Funktionen, die NICHT Systemfunktionen emulieren ! */

/* Man beachte auch den MEM-praefix */

/*

VOID   ENTRY MEMReleaseName(POK_NAME psToBeReleased);

VOID   ENTRY MEMReleaseItem(POK_ITEM psToBeReleased);

BOOL   ENTRY MEMbIsLegalName(POK_NAME psName);

*/

VOID   ENTRY MEM_UlongToMSBFirst (GPBYTE pbBuffer, ULONG ulValue);

ULONG  ENTRY MEM_MSBFirstToUlong (GPBYTE pbBuffer);

VOID   ENTRY MEM_UshortToMSBFirst (GPBYTE pbBuffer, USHORT usValue);

USHORT ENTRY MEM_MSBFirstToUshort (GPBYTE pbBuffer);



ULONG  ENTRY MEM_ntohl(ULONG ulValue);

ULONG  ENTRY MEM_htonl(ULONG ulValue);



OKERR ENTRY MEM_BinToHexString (GPBYTE pbData, UINT uiDataSize,

                                 GPCHAR pszHexString, UINT uiStringSize);

OKERR ENTRY MEM_HexStringToBin (GPCHAR pszHexString,

                                 GPBYTE pbData, GPUINT puiDataSize);



#if defined S_BOOT

UINT MEMGetFreeBytes(VOID);

VOID MEMGetLastEntry(LPUINT puiLastEntry,

                     LPUINT puiMaxEntries);

#endif



#ifdef __cplusplus

   }

#endif



#include <okpoff.h>



#endif /* _INC_MEM */

