/*****************************************************************************\
* Copyright (c) 1998 IBM Corporation
* file res_mgr.h  , PCSC Migration Interface
* Definitions for Subset of Resource Manager defined in PCSC specification
\*****************************************************************************/
#ifndef _RES_MGR_H_
#define _RES_MGR_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "types.h"

typedef   void**	 LPSCARDCONTEXT;
typedef   void*		 SCARDCONTEXT;
typedef   void**	 LPSCARDHANDLE;
typedef   void*		 SCARDHANDLE;
typedef   unsigned char* STR;

#define SCARD_S_SUCCESS			 ((DWORD)0x00000000L)

#define SCARD_E_INVALID_PARAMETER	 ((DWORD)0x80100004L)
#define SCARD_E_INSUFFICIENT_BUFFER      ((DWORD)0x80100008L)
#define SCARD_E_UNKNOWN_READER		 ((DWORD)0x80100009L)
#define SCARD_E_NOT_READY		 ((DWORD)0x80100010L)
#define SCARD_E_INVALID_VALUE		 ((DWORD)0x80100011L)
#define SCARD_E_NO_SERVICE		 ((DWORD)0x8010001DL)
#define SCARD_E_SHARING_VIOLATION	 ((DWORD)0x8010000BL)
#define SCARD_E_UNSUPPORTED_REQUEST      ((DWORD)0xA0100001L)

#define SCARD_W_UNRESPONSIVE_CARD	 ((DWORD)0x80100066L)
#define SCARD_W_UNPOWERED_CARD		 ((DWORD)0x80100067L)
#define SCARD_W_REMOVED_CARD		 ((DWORD)0x80100069L)

#ifndef _WIN32			       /* windows errors		      */
#define ERROR_INVALID_HANDLE		 ((DWORD)0x00000006L)
#define ERROR_NOT_SUPPORTED		 ((DWORD)0x00000032L)
#endif

/* Access Mode Flags */
#define SCARD_SHARE_EXCLUSIVE		 1
#define SCARD_SHARE_DIRECT		 3

/* Protocol Identifier Bits */
#define SCARD_PROTOCOL_UNDEFINED	 0x00000000
#define SCARD_PROTOCOL_T1		 0x00000002

/* Card Disposition    */
#define SCARD_UNPOWER_CARD		 2
#define SCARD_EJECT_CARD		 3

/* Card Reader State   */
#define SCARD_STATE_UNAWARE		 0x00000000
#define SCARD_STATE_CHANGED		 0x00000002
#define SCARD_STATE_UNAVAILABLE		 0x00000008
#define SCARD_STATE_EMPTY		 0x00000010
#define SCARD_STATE_PRESENT		 0x00000020

#define SCARD_ABSENT			 1
#define SCARD_PRESENT			 2
#define SCARD_POWERED			 4

/* Context Scope       */
#define SCARD_SCOPE_USER		 0

/* Attributes */
#define SCARD_ATTR_VALUE(Class, Tag) ((((ULONG)(Class)) << 16) | ((ULONG)(Tag)))

#define SCARD_CLASS_VENDOR_INFO     1   /* Vendor information definitions */
#define SCARD_CLASS_ICC_STATE       9   /* ICC State specific definitions */

#define SCARD_ATTR_VENDOR_NAME SCARD_ATTR_VALUE(SCARD_CLASS_VENDOR_INFO, 0x0100)
#define SCARD_ATTR_VENDOR_IFD_TYPE SCARD_ATTR_VALUE(SCARD_CLASS_VENDOR_INFO, 0x0101)
#define SCARD_ATTR_VENDOR_IFD_VERSION SCARD_ATTR_VALUE(SCARD_CLASS_VENDOR_INFO, 0x0102)
#define SCARD_ATTR_ATR_STRING SCARD_ATTR_VALUE(SCARD_CLASS_ICC_STATE, 0x0303)
#define SCARD_ATTR_ICC_PRESENCE SCARD_ATTR_VALUE(SCARD_CLASS_ICC_STATE, 0x0300)


/******************************************************************************/

typedef struct {
	 LPCSTR   szReader;
	 void* pvUserData;
	 DWORD dwCurrentState;
	 DWORD dwEventState;
	 DWORD dwRes1;		       /* field for RFU			      */
	 BYTE  bRes2[36];	       /* field for RFU			      */
      } SCARD_READERSTATE,* LPSCARD_READERSTATE;

typedef struct {
	 DWORD dwProtocol;
	 DWORD cbPciLength;
      } SCARD_IO_REQUEST, *LPSCARD_IO_REQUEST, *const LPCSCARD_IO_REQUEST;

#ifdef _WIN32
  #ifndef WINSCARDDATA
  #define WINSCARDDATA __declspec(dllimport)
  #endif
  WINSCARDDATA extern SCARD_IO_REQUEST
     g_rgSCardT1Pci;
  #define SCARD_PCI_T1 (&g_rgSCardT1Pci)
#else
  #define SCARD_PCI_T1 NULL
#endif

/******************************************************************************/
/*  Resource Manager Functions Prototypes				      */
/******************************************************************************/
#ifndef EXPENTRY
  #ifdef __OS2__
    #define EXPENTRY  _System
  #elif _WIN32
    #define EXPENTRY   __stdcall
  #elif _WINDOWS
    #define EXPENTRY  _far _loadds _pascal
  #else
    #define EXPENTRY  far _loadds
  #endif
#endif

LONG EXPENTRY SCardEstablishContext(DWORD dwScope, LPVOID pvReserved1,
				    LPVOID pvReserved, LPSCARDCONTEXT phContext);

LONG EXPENTRY SCardReleaseContext(SCARDCONTEXT hContext);

LONG EXPENTRY SCardConnect(SCARDCONTEXT hContext, LPCTSTR szReader,
			   DWORD dwShareMode, DWORD dwPreferredProtocol,
			   LPSCARDHANDLE phCard, LPDWORD pdwActiveProtocol);

LONG EXPENTRY SCardReconnect(SCARDHANDLE hCard, DWORD dwShareMode,
			     DWORD dwPreferredProtocols,DWORD dwInitialization,
			     LPDWORD pdwActiveProtocol);

LONG EXPENTRY SCardDisconnect(SCARDHANDLE hCard, DWORD dwDisposition);

LONG EXPENTRY SCardGetStatusChange(SCARDCONTEXT hContext, DWORD dwTimeout,
				   LPSCARD_READERSTATE rgReaderStates,
				   DWORD cReaders);

LONG EXPENTRY SCardGetAttrib(SCARDHANDLE CardHandle, DWORD Tag,
			     LPBYTE ValueBuffer, LPDWORD Length);

LONG EXPENTRY SCardControl(SCARDHANDLE hCard, DWORD dwControlCode,
			   LPCVOID lpInBuffer, DWORD nInBufferSize,
			   LPVOID lpOutBuffer, DWORD nOutBufferSize,
			   LPDWORD lpBytesReturned);

LONG EXPENTRY SCardListReaders(SCARDCONTEXT hContext, LPCTSTR mszGroups,
			       LPTSTR mszReaders, LPDWORD pcchReaders);

LONG EXPENTRY SCardTransmit(SCARDHANDLE hCard, LPCSCARD_IO_REQUEST pioSendPci,
		      LPCBYTE pbSebdBuffer, DWORD cbSendLength,
		      LPSCARD_IO_REQUEST pioRecvPci, LPBYTE pbRecvBuffer,
		      LPDWORD pcbRecvLength);


#ifdef __cplusplus
}

#endif

#endif
