/*****************************************************************************\
* Copyright (c) 1998 IBM Corporation
* file types.h
\*****************************************************************************/

#ifndef RES_TYPES_INCLUDED
#define RES_TYPES_INCLUDED

#ifdef __cplusplus
extern "C" {
#endif

#ifndef EXP_VAWIN
   #if defined(_WIN32)&&(defined(__IBMC__)||defined(__IBMCPP))
       #define EXP_VAWIN   _Export
   #else
       #define EXP_VAWIN
   #endif
#endif



#ifdef _WIN32
  #include <windows.h>

  #ifndef _NEWLPC_DEFINED_
  #define _NEWLPC_DEFINED_

    typedef const BYTE * LPCBYTE;
    typedef const VOID * LPCVOID;
  #endif

  typedef HINSTANCE    DLLHANDLE;
#endif


#ifdef _DOS
 #include <stdio.h>

 typedef const BYTE *  LPCBYTE;
 typedef const VOID *  LPCVOID;
 typedef  void far*    LPVOID;
 typedef  DWORD far*   LPDWORD;
 typedef  BYTE far*    LPBYTE;
 typedef  char far*    LPCTSTR;
 typedef  void far*    LPCVOID;
 typedef  BYTE far*    LPTSTR;

 #define  DLLHANDLE       BYTE /* placeholder for DLL handles  not used in DOS*/
#endif

#ifdef __OS2__
 #define INCL_DOSMODULEMGR
 #include <os2.h>
 #include <stdio.h>
 #include <stdlib.h>

 #define OS_SCARD_PCI_T1  NULL

 typedef  HMODULE         DLLHANDLE;

 #ifndef S_OS2_32
 typedef  unsigned long   DWORD;
 typedef  void *          LPVOID;
 typedef  DWORD*          LPDWORD;
 typedef  BYTE *          LPBYTE;
 #endif

 typedef  const char *    LPCBYTE;
 typedef  char *          LPCTSTR;
 typedef  const char *    LPCSTR;
 typedef  void *          LPCVOID;
 typedef  BYTE *          LPTSTR;
#endif

#ifdef _WINDOWS
 #include <windows.h>
 typedef  unsigned long   DWORD;
 typedef  unsigned short  WORD;
 typedef  unsigned char   BYTE;
 typedef  HINSTANCE       DLLHANDLE;
 typedef  char far*       LPCTSTR;
 typedef  void far*       LPCVOID;
 typedef  BYTE far*       LPTSTR;
#endif

#ifdef __cplusplus
}

#endif
#endif

