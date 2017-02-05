/*****************************************************************************

@doc            INT EXT

******************************************************************************

* $ProjectName:  $

* $ProjectRevision:  $

*-----------------------------------------------------------------------------

* $Source$

* $Revision: 27 $

*-----------------------------------------------------------------------------

* $Author: jp $

*-----------------------------------------------------------------------------

* History: see EOF

*-----------------------------------------------------------------------------

* Copyright (c) 2000 OMNIKEY AG

******************************************************************************/



#ifndef _INC_SCARD3W

   #define _INC_SCARD3W

      

   #include <ok.h>

   #include <winscard.h>



/*****************************************************************************/

/** CONSTANTS                                                                */

/*****************************************************************************/





   #define SCARD3W_MAX_DATA_LEN     1024

   #define SCARD3W_PIN_LEN          2





/*****************************************************************************/

/** FUNCTION PROTOTYPES                                                      */

/*****************************************************************************/



   #ifdef __cplusplus

extern "C" {

   #endif



   OKERR ENTRY SCard3WBPReadData         (IN  SCARDHANDLE ulHandleSmartCard,

                                           IN  ULONG       ulBytesToRead,

                                           OUT LPBYTE      pbReadBuffer,

                                           IN  ULONG       ulAddress);



   OKERR ENTRY SCard3WBPVerifyProtectBit (IN  SCARDHANDLE ulHandleSmartCard,

                                           IN  ULONG       ulAddress,

                                           OUT LPBOOL      pfProtected);



   OKERR ENTRY SCard3WBPVerifyProtectBitEx(

                IN  SCARDHANDLE ulHandleSmartCard,

                IN  ULONG       ulBytesToRead,

                OUT LPBYTE      pbReadBuffer,

                IN  ULONG       ulAddress);



   OKERR ENTRY SCard3WBPWriteData        (IN  SCARDHANDLE ulHandleSmartCard,

                                           IN  ULONG       ulDataLen,

                                           IN  LPBYTE      pbData,

                                           IN  ULONG       ulAddress,

                                           IN  BOOL        fProtected);



   OKERR ENTRY SCard3WBPCompareAndProtect(IN  SCARDHANDLE ulHandleSmartCard,

                                           IN  BYTE        bData,

                                           IN  ULONG       ulAddress);



   OKERR ENTRY SCard3WBPPresentPIN       (IN  SCARDHANDLE ulHandleSmartCard,

                                           IN  ULONG       ulPINLen,

                                           IN  LPBYTE      pbPIN);



   OKERR ENTRY SCard3WBPChangePIN        (IN  SCARDHANDLE ulHandleSmartCard,

                                           IN  ULONG       ulOldPINLen,

                                           IN  LPBYTE      pbOldPIN,

                                           IN  ULONG       ulNewPINLen,

                                           IN  LPBYTE      pbNewPIN);



   OKERR ENTRY SCard3WBPIsPinPresented   (IN  SCARDHANDLE ulHandleSmartCard,

                                           OUT LPBOOL      pfPinPresented);



   #ifdef __cplusplus

}

   #endif



#endif /* _INC_SCARD3W */

