#ifndef _INC_PCSCSUB

   #define _INC_PCSCSUB

      

   #include <ok.h>

   #include <winscard.h>

#if defined S_MACOSX

#include <pcsclite.h>

#endif



#include "cmdebug.h"



   #ifdef __cplusplus

extern "C" {

   #endif



  #if defined S_LINUX || defined S_MACOSX



  #if defined S_LINUX

    #define PCSC_MAX_BUFFER_SIZE MAX_BUFFER_SIZE

  #else

	  #define CMD_HEADER 1 // 0xFF 0xFF

	  #define CMD_OFFSET (CMD_HEADER + 4) // 4 Bytes controlcode

    #define PCSC_MAX_BUFFER_SIZE (MAX_BUFFER_SIZE - CMD_OFFSET)

  #endif

  	

  long SCardControlOK(long hCard,

					  unsigned long dwControlCode,

		              const unsigned char *pbSendBuffer,

		              unsigned long cbSendLength,

		              unsigned char *pbRecvBuffer,

					  unsigned long cbRecvLength,

					  unsigned long *pcbBytesReturned);



   #endif // defined S_LINUX || defined S_MACOSX

   

   #ifdef __cplusplus

}

   #endif



#endif /* _INC_PCSCSUB */

