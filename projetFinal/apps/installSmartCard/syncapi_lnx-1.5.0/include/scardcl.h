/*****************************************************************************
@doc            INT EXT
******************************************************************************
* $ProjectName:  $
* $ProjectRevision:  $
*-----------------------------------------------------------------------------
* $Source: z:/pr/cmsync/sw/scardcl/rcs/scardcl.h $
* $Revision: 74 $
*-----------------------------------------------------------------------------
* $Author: mh $
*-----------------------------------------------------------------------------
* History: see EOF
*----------------------------------------------------------------------------
* Copyright (c) 2000 - 2006 OMNIKEY
*****************************************************************************/

#ifndef _INC_SCARD_CL
   #define _INC_SCARD_CL

#include <ok.h>
   
#ifdef __cplusplus
extern "C" {
#endif

OKERR ENTRY SCardCLICCTransmit(IN SCARDHANDLE		ulHandleCard,
								  IN PUCHAR     		pucSendData,				
                         		  IN ULONG       		ulSendDataBufLen,
                        		  IN OUT PUCHAR			pucReceivedData,
								  IN OUT PULONG			pulReceivedDataBufLen);


   #ifdef __cplusplus
}
   #endif









#endif  /* _INC_SCARD_CL */


/*****************************************************************************
* History:
* $Log: scardcl.h $
* Revision 1.10  2006/02/16 12:20:13  TBruendl
* No comment given
*
* Revision 1.9  2005/09/07 11:22:48  TBruendl
* No comment given
*
* Revision 1.5  2004/12/23 07:46:31  TBruendl
* No comment given
*
* Revision 1.3  2004/07/09 07:45:21  TBruendl
* No comment given
*
* Revision 1.1  2004/02/26 09:47:44  TBruendl
* No comment given
*
******************************************************************************/

