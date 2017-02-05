/**
****************************************************************************** 
* @file 
* @brief Pragma on. 
*
* Set alignment according to platform. Also see @see okpoff.h.
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

#if defined _MSC_VER
   #if !(defined S_BOOT)
      #pragma warning (disable: 4103) /* allows #pragma for structure alignment */
   #endif

   #if defined S_BOOT
      #pragma pack(2)
   #elif defined S_WNT
      #pragma pack(8)
   #endif
#endif /* _MSC_VER */

