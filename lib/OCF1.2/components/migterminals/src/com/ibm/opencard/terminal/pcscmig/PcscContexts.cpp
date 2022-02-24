/*
 * Copyright © 1997 - 1999 IBM Corporation.
 * 
 * Redistribution and use in source (source code) and binary (object code)
 * forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 1. Redistributed source code must retain the above copyright notice, this
 * list of conditions and the disclaimer below.
 * 2. Redistributed object code must reproduce the above copyright notice,
 * this list of conditions and the disclaimer below in the documentation
 * and/or other materials provided with the distribution.
 * 3. The name of IBM may not be used to endorse or promote products derived
 * from this software or in any other form without specific prior written
 * permission from IBM.
 * 4. Redistribution of any modified code must be labeled "Code derived from
 * the original OpenCard Framework".
 * 
 * THIS SOFTWARE IS PROVIDED BY IBM "AS IS" FREE OF CHARGE. IBM SHALL NOT BE
 * LIABLE FOR INFRINGEMENTS OF THIRD PARTIES RIGHTS BASED ON THIS SOFTWARE.  ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IBM DOES NOT WARRANT THAT THE FUNCTIONS CONTAINED IN THIS
 * SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR THAT THE OPERATION OF IT WILL
 * BE UNINTERRUPTED OR ERROR-FREE.  IN NO EVENT, UNLESS REQUIRED BY APPLICABLE
 * LAW, SHALL IBM BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  ALSO, IBM IS UNDER NO OBLIGATION
 * TO MAINTAIN, CORRECT, UPDATE, CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS
 * SOFTWARE.
 */

/*
 * Author:  Stephan Breideneich (sbreiden@de.ibm.com)
 * Version: $Id: PcscContexts.cpp,v 1.1 1998/04/09 18:28:50 breid Exp $
 */

#include <stdio.h>
#include "PcscContexts.h"

/* this array holds the established context for the cleanup */
long establishedContexts[MAX_CONTEXTS];

/*
 * initContextTable
 *
 * clears the internal table
 */
void initContextTable() {
  /* initial cleanup of the establishedContexts array */
  int i;
  for (i=0; i<MAX_CONTEXTS; i++)
    establishedContexts[i]=0;
}

/*
 * isContextAvailable
 *
 * checks if the given context is available in establishedContexts array
 *
 * return  < 0 - context not in use
 * return >= 0 - context in use. returncode gives the position within the array establishedContexts
 */
int isContextAvailable(long context) {
  for (int i=0 ; i < MAX_CONTEXTS ; i++)
    if (establishedContexts[i] == context)
      return i; // context found

  return -1;    // context not found
}					       /* end of isContextAvailable*/

/*
 * addContext
 *
 * adds a context to the internal table establishedContext
 *
 * return   -1 - failed
 * return >= 0 - position of the context in the table
 */
int addContext(long context) {
  int freePos;

  // where is a free element in the establishedContexts array?
  for (freePos=0 ;freePos < MAX_CONTEXTS; freePos++ )
    if (establishedContexts[freePos] == 0) {
      establishedContexts[freePos] = context;
      return freePos;
    }

  // addContext failed
  return -1;
}						       /* end of addContext*/


/*
 * removeContext
 *
 * removes the given context from the internal table establishedContext
 *
 * return   -1 - given context not found
 * return  = 0 - ok
 */
int removeContext(long context) {
  for (int freePos; freePos < MAX_CONTEXTS; freePos++)
    if (establishedContexts[freePos] == context) {
      establishedContexts[freePos] = 0;
      return 0;
    }

  // given context not found
  return -1;
}						    /* end of removeContext*/



/*
 * removeAllContexts
 *
 * removes all registered contexts
 */
void removeAllContexts() {
    for (int i; i < MAX_CONTEXTS; i++) {
	if (establishedContexts[i] != 0)
	    removeContext(establishedContexts[i]);
    }

    return;
}

// $Log: PcscContexts.cpp,v $
// Revision 1.1  1998/04/09 18:28:50  breid
// initial version
//
