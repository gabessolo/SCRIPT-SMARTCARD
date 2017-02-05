/**
****************************************************************************** 
* @file 
* @brief Set OS specific pragmas.
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

#ifndef _okos_h_
#define _okos_h_

#if defined _MSC_VER
   #if defined S_WNT
      #pragma warning (disable: 4001) /*nonstd. extension 'single line comm. */
      #pragma warning (disable: 4057) /*'uns. char *' differs in indirection */
      #pragma warning (disable: 4103) /* allows #pragma for structure alignment */
      #pragma warning (disable: 4115) /*named type definition in parentheses */
      #pragma warning (disable: 4200) /*non.ex. zero-sized array in str/union*/
      #pragma warning (disable: 4201) /*non.ex. nameless struct/union        */
      #pragma warning (disable: 4209) /*non.ex. benign typedef redefinition  */
      #pragma warning (disable: 4214) /*non.ex. bit field types other th. int*/
      #pragma warning (disable: 4514) /*unreferenced inline function removed */
   #endif
#endif /* _MSC_VER */

#endif /* _okos_h_ */

