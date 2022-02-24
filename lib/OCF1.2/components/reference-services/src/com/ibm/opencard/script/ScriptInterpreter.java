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

package com.ibm.opencard.script;

import java.util.Vector;
import java.util.Stack;

import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.service.CardServiceOperationFailedException;
import opencard.core.service.CardServiceImplementationException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.util.HexString;
import opencard.core.util.Tracer;

import opencard.opt.service.CardServiceResourceNotFoundException;
import opencard.opt.util.TLV;
import opencard.opt.util.Tag;

import com.ibm.opencard.handler.Request;
import com.ibm.opencard.handler.Handler;
import com.ibm.opencard.handler.HandlerChain;
import com.ibm.opencard.buffer.TLVBuffer;
import com.ibm.opencard.buffer.DataBuffer;
import com.ibm.opencard.dictionary.Dictionary;
import com.ibm.opencard.util.SCTTag;


/**
 * An interpreter for IBM's SmartCard Toolkit scripts.
 *
 * @author Thomas Schaeck (schaeck@de.ibm.com)
 * @author Roland Weber  (rolweber@de.ibm.com)
 * @version  $Id: ScriptInterpreter.java,v 1.4 1998/09/10 07:56:03 cvsusers Exp $
 */
public class ScriptInterpreter
{
  /** tracer for debugging output */
  private Tracer tracer = new Tracer(ScriptInterpreter.class);

  /** request handler chain */
  private HandlerChain handlerChain = null;

  /** card channel owner for direct communication with the smartcard */
  private APDUSender apduSender = null;

  /** the id of the current procedure */
  private TLV currentProcedureID = null;

  /** the id of the current request */
  private TLV currentRequestID = null;

  /** the type of the current request */
  private Request request = null;

  /** procedure call stack */
  private Stack callStack = null;

  /**
   * Instantiates a script interpreter.
   *
   * @param handlerchain   a collection of handlers for various requests
   */
  public ScriptInterpreter(HandlerChain handlerchain, APDUSender apdusender)
  {
    handlerChain = handlerchain;
    apduSender   = apdusender;
    callStack    = new Stack();
    request      = new Request(Request.UNDEFINED, //@@@ oversized buffers ?
                               new DataBuffer(new byte[1000], 1000),
                               new TLVBuffer(new byte[1000], 1000));
  }

  /**
   * Set the dictionary to be used.
   * @param dictionary the dictionary to be used
   */
  public void setDictionary(Dictionary dictionary)
  {
    /*@@@ setDictionary has to be implemented
    AgentHandler agentHandler =
      (AgentHandler) handlerChain.get("AgentHandler");
    if (agentHandler != null) {
      agentHandler.setDictionary(dictionary);
    }
    */
  }


  /*@@@ providing credentials to handlers has to be implemented
  ****************************************************************************
  * Set the KeyBag for the AgentHandler.
  * @param keyBag the KeyBag to be used by AgentHandlers.
  ****************************************************************************/
  /*
  public void setKeyBag(KeyBag keyBag)
  {
    AgentHandler agentHandler = (AgentHandler) handlerChain.get("AgentHandler");
    if (agentHandler != null) {
      agentHandler.setKeyBag(keyBag);
    }
  }
  */

  /*
  ****************************************************************************
  * Set the KeyStore for the ScriptExtension.
  * @param keyStore the KeyStore to be used for SIGNATURE requests,
  *                 ENCIPHER requets, ...
  ****************************************************************************/
  /*
  public void setKeyStore(KeyStore keyStore)
       throws CardServiceResourceNotFoundException
  {
    // The CryptoHandler class is missing in the exportable version of the
    // OCF, so we use the reflection API instead of directly using the class.
    try {
      Class cryptoHandlerClass = Class.forName("com.ibm.opencard.handler.CryptoHandler");
      Class[] parameterTypes   = { opencard.opt.iso.fs.KeyStore.class };
      Object[] parameters      = new Object[] { keyStore };
      Method setKeyStore       = cryptoHandlerClass.getMethod("setKeyStore", parameterTypes);
      setKeyStore.invoke(handlerChain.get("CryptoHandler"), parameters);
    } catch (Exception e) {
      tracer.error("setKeyStore", e);
      throw new CardServiceResourceNotFoundException(e.toString());
    }
  }
  */


  /**
   * Executes a given order within the given script, using optional dictionary
   * and optional blackboard.
   * @param order      the name of the script procedure to be executed
   * @param script     the script containing the procedure that shall be
   *                   executed
   * @param dictionary the dictionary to be used (optional)
   * @param blackboard the blackbaord to be used (optional)
   */
  //@@@ document exceptions
  public void executeOrder(String       order,
                           Script       script,
                           Dictionary   dictionary,
                           TLVBuffer    blackboard)
       throws CardServiceException, CardTerminalException
  {
    TLV         scriptTLV   = null;
    int[]       index       = {0};

    setDictionary(dictionary);
    scriptTLV = script.buffer().findTLV(SCTTag.SCRIPT, index);
    tracer.info("executeOrder", order);

    execProcedure(findProcedure(scriptTLV, order), scriptTLV, blackboard);
  }


  /**
   * Executes a script procedure.
   *
   * @param procedureTLV the procedure to be executed
   * @param scriptTLV    the script which can be used for procedure calls from
   *                     within the script to be executed
   * @param blackboard   the blackboard containing data referenced from
   *                     BUFFER_PARTs.
   */
  //@@@ document exceptions
  private void execProcedure(TLV        procedureTLV,
                             TLV        scriptTLV,
                             TLVBuffer  blackboard)
       throws CardServiceException, CardTerminalException
  {
    TLV    anyTLV       = null;
    TLV    argumentTLV  = null;
    TLV    parameterTLV = null;
    TLV    resultTLV    = null;
    byte[] argument     = null;
    byte[] parameter    = null;

    // Find the next request TLV
    while ((anyTLV = procedureTLV.findTag(null, anyTLV)) != null)
      {
        // get the requests argument, parameter and result section
        if ((argumentTLV = anyTLV.findTag(SCTTag.ARGUMENT, null)) != null)
          argument  = evaluate(argumentTLV, blackboard);
        if ((parameterTLV = anyTLV.findTag(SCTTag.PARAMETER, null)) != null)
          parameter = evaluate(parameterTLV, blackboard);
        resultTLV = anyTLV.findTag(SCTTag.RESULT, null);

        TLV id = anyTLV.findTag(SCTTag.ID, null);
        if (id != null)
          currentRequestID = id;
        // handle the request
        switch (anyTLV.tag().code())
          {
          case Code.ID:
            currentProcedureID = anyTLV;   // Used for error message creation
            callStack.push(anyTLV);
            break;

          case Code.AGENT_REQUEST:
            handle0815Request(Request.AGENT,
                              argument, parameter,
                              resultTLV, blackboard);
            /*handleAgentRequest(argument, parameter,
                               resultTLV, blackboard);*/
            break;

          case Code.AGENCY_REQUEST:
            handleUnsupportedRequest("AGENCY_REQUEST");
            /*handleAgencyRequest(argument, parameter,
                                resultTLV, blackboard);*/
            break;

          case Code.AUTHENTICATION_REQUEST:
            handle0815Request(Request.AUTHENTICATION,
                              argument, parameter,
                              resultTLV, blackboard);
            /*handleAuthenticationRequest(argument, parameter,
                                        resultTLV, blackboard);*/
            break;

          case Code.BUFFER_REQUEST:
            handleBufferRequest(argument, parameter,
                                resultTLV, blackboard);
            break;

          case Code.CARD_REQUEST:
            handleCardRequest(argument, parameter,
                              resultTLV, blackboard);
            break;

          case Code.CHAINING_REQUEST:
            handle0815Request(Request.CHAINING,
                              argument, parameter,
                              resultTLV, blackboard);
            /*handleChainingRequest(argument, parameter,
                                  resultTLV, blackboard);*/
            break;

          case Code.ENCIPHER_REQUEST:
            handleEncipherRequest(argument, parameter,
                                  resultTLV, blackboard);
            break;

          case Code.DECIPHER_REQUEST:
            handle0815Request(Request.DECIPHER,
                              argument, parameter,
                              resultTLV, blackboard);
            /*handleDecipherRequest(argument, parameter,
                                  resultTLV, blackboard);*/
            break;

          case Code.EXPORT_REQUEST:
            handle0815Request(Request.EXPORT,
                              argument, parameter,
                              resultTLV, blackboard);
            /*handleExportRequest(argument, parameter,
                                resultTLV, blackboard);*/
            break;

          case Code.IDENTIFICATION_REQUEST:
            handle0815Request(Request.IDENTIFICATION,
                              argument, parameter,
                              resultTLV, blackboard);
            /*handleIdentificationRequest(argument, parameter,
                                        resultTLV, blackboard);*/
            break;

          case Code.IMPORT_REQUEST:
            handleUnsupportedRequest("IMPORT_REQUEST");
            /*handleImportRequest(argument, parameter,
                                resultTLV, blackboard);*/
            break;

          case Code.FUNCTION_REQUEST:
            handleUnsupportedRequest("FUNCTION_REQUEST");
            /*handleFunctionRequest(argument, parameter,
                                  resultTLV, blackboard);*/
            break;

          case Code.SIGNATURE_REQUEST:
            handleSignatureRequest(argument, parameter,
                                   resultTLV, blackboard);
            break;

          case Code.VALIDATION_REQUEST:
            handle0815Request(Request.VALIDATION,
                              argument, parameter,
                              resultTLV, blackboard);
            /*handleValidationRequest(argument, parameter,
                                    resultTLV, blackboard);*/
            break;

          case Code.PROCEDURE_CALL:
            execProcedure(findProcedure(scriptTLV,
                                        new String(anyTLV.valueAsByteArray())),
                          scriptTLV, blackboard);
            break;

          default:
            handleUnsupportedRequest("unknown request");
            break;
          }

      } // while
    //@@@ push in case ID above. What about currentProcedureID ?
    callStack.pop();
  }


  // Very ugly, but this saves us instantiations and garbage collection
  private DataBuffer evaluationDataBuffer
      = new DataBuffer(new byte[1000], 1000);

  /**
   * Evaluates a TLV. The BUFFER_PARTs, CONSTANT_PARTs, BUFFER_SIZEs and
   * PLACEHOLDER_PARTs are substituted by the associated data and the
   * resulting TLV structure is converted to a byte array.
   * @param tlv         the argument TLV
   * @param blackboard  the blackboard containing data referenced from
   *                    BUFFER_PARTs.
   * @return            binary representation of the evaluated TLV
   */
  private byte[] evaluate(TLV tlv, TLVBuffer blackboard)
       throws CardServiceException
  {
    TLV anyTLV = null;
    evaluationDataBuffer.clear();

    if (tlv.tag().isConstructed() == true) {
      // The tag is constructed, concatenate the evaluation results
      // of all contained tags.
      boolean[] constructed = { true };
      while ((anyTLV = tlv.findTag(null, anyTLV)) != null) {
        evaluationDataBuffer.append(evaluateTLV(anyTLV, blackboard, constructed));
      }
      return evaluationDataBuffer.data();
    }
    else {
      // The tag is primitive, simply return it's contents.
      return tlv.valueAsByteArray();
    }
  }


  /**
   * Evaluates a TLV. The BUFFER_PARTs, CONSTANT_PARTs, BUFFER_SIZEs and
   * PLACEHOLDER_PARTs are substituted by the associated data and the
   * resulting TLV structure is converted to a byte array.
   * @param tlv         the argument TLV
   * @param blackboard  the blackboard containing data referenced from
   *                    BUFFER_PARTs.
   * @param constructed boolean array, length 1. The only element indicates
   *                    wether the parent TLV of anyTLV is primitive or
   *                    constructed. This element may be modified by
   *                    this method, to indicate wether the parent TLV still
   *                    can be interpreted as constructed after
   *                    parts have been replaced.
   * @return            binary representation of the evaluated TLV
   */
  private byte[] evaluateTLV(TLV anyTLV,
                             TLVBuffer blackboard,
                             boolean[] constructed)
       throws CardServiceException
  {
    byte[] id   = null;
    byte[] data = null;

    switch (anyTLV.tag().code()) {
    case Code.CONSTANT_PART:
      // Replace a constant part by it's contents
      constructed[0]= false;
      return anyTLV.valueAsByteArray();
    case Code.BUFFER_PART:
      // Allowed syntax: BUFFER_PART(ID(string) [OFFSET(int)] [SIZE(int)]) or BUFFER_PART(string)
      // Replace a buffer part by the data referenced by it's identifier
      TLV bpTLV = null;
      constructed[0]= false;

      if (anyTLV.tag().isConstructed() == false) {
        // Simplified notation: BUFFER_PART(string)
        if ((id = anyTLV.valueAsByteArray()) == null)
          throw new CardServiceInvalidParameterException(msg("Invalid ID entry in given script"));
        if ((data = blackboard.getData(id)) == null) {
          throw new CardServiceInvalidParameterException(msg("ID("+HexString.hexify(id)+new String(id) + ") not found in given blackboard"));
        }
        return data;
      } else {
        // explicit notation: BUFFER_PART(ID(string) [OFFSET(int)] [SIZE(int)])
        if ((bpTLV = anyTLV.findTag(SCTTag.ID, null)) == null)
          throw new CardServiceInvalidParameterException(msg("Missing ID in given script"));
        if ((id = bpTLV.valueAsByteArray()) == null)
          throw new CardServiceInvalidParameterException(msg("Invalid ID entry in given script"));
        if ((data = blackboard.getData(id)) == null)
          throw new CardServiceInvalidParameterException(msg("ID("+HexString.hexify(id)+") not found in given blackboard"));

        bpTLV = anyTLV.findTag(SCTTag.OFFSET, null);
        int offset = (bpTLV != null) ? bpTLV.valueAsNumber() : 0;
        bpTLV = anyTLV.findTag(SCTTag.SIZE, null);
        int size   = (bpTLV != null) ? bpTLV.valueAsNumber() : (data.length - offset);
        byte[] result = new byte[size];
        System.arraycopy(data, offset, result, 0, size);
        return result;
      }
    case Code.BUFFER_SIZE:
      // Replace buffer size by the size of the data referenced by it's identifier
      constructed[0]= false;

      if ((id = anyTLV.findTag(SCTTag.ID, null).valueAsByteArray()) == null)
        throw new CardServiceInvalidParameterException(msg("Missing ID in BUFFER_SIZE tag in given script"));
      if ((data = blackboard.getData(id)) == null)
        throw new CardServiceInvalidParameterException(msg("ID("+HexString.hexify(id)+") not found in blackboard"));
      return TLV.lengthToBinary(data.length);
    default:
      // If anyTLV is constructed, evaluate it's contents, otherwise convert it to bytes
      if (anyTLV.tag().isConstructed()) {
        TLV innerTLV = null;
        DataBuffer db = new DataBuffer(new byte[0], 0);
        boolean isConstructed[] = { true };

        while ((innerTLV = anyTLV.findTag(null, innerTLV)) != null)
          db.append(evaluateTLV(innerTLV, blackboard, isConstructed));

        // Here, the tag may change from constructed to primitive
        Tag newTag = new Tag(anyTLV.tag());
        newTag.setConstructed(false);
        TLV resultTLV = new TLV(newTag, db.data());
        resultTLV.tag().setConstructed(isConstructed[0]);
        return resultTLV.toBinary();
      }
      else
        return anyTLV.toBinary();
    }
  } // evaluateTLV


  /**************************************************************************
  * Execute the result section of the request.
  * BUFFER_ENTRY, BUFFER_DELETE, STATUSWORDS and MATCH tags are processed,
  * other tags are ignored.
  * @param resultTLV  the result TLV
  * @param argument   the argument data which are the result of a request
  * @param blackboard the blackboard containing data referenced from
  *                   BUFFER_PARTs.
  **************************************************************************/
  private void executeResultSection(TLV       resultTLV,
                                    byte[]    argument,
                                    TLVBuffer blackboard)
       throws CardServiceException
  {
    TLV anyTLV = null;

    if (resultTLV != null) {
      while ((anyTLV = resultTLV.findTag(null, anyTLV)) != null) {
        switch (anyTLV.tag().code()) {
        case Code.BUFFER_ENTRY:
          // Create a new entry in the blackboard
          blackboard.addDataEntry(anyTLV.valueAsByteArray(), argument);
          break;
        case Code.BUFFER_DELETE:
          // Remove an existing entry from the blackboard
          blackboard.removeEntry(anyTLV.valueAsByteArray());
          break;
        case Code.STATUSWORDS:
          // Check if the status words (last two bytes of argument data)
          // contain given data
          byte[] statusWords         = new byte[2];
          byte[] expectedStatusWords = null;

          System.arraycopy(argument, argument.length-2, statusWords, 0, 2);
          expectedStatusWords = (anyTLV.tag().isConstructed()==true) ?
            evaluate(anyTLV,blackboard):anyTLV.valueAsByteArray();

          if (! arraysEqual(statusWords, expectedStatusWords))
            throw new CardServiceOperationFailedException
              (msg("StatusWords " + HexString.hexify(statusWords) +
                   "don't match expected status words " +
                   HexString.hexify(expectedStatusWords)));
          break;
        case Code.MATCH:
          // Check if the argument data (or a part of it) matches given data
          byte[] argumentPart = null;
          byte[] pattern      = null;
          TLV bpTLV = null;

          if (anyTLV.tag().isConstructed()== true) {
            pattern = evaluate(anyTLV.findTag(SCTTag.PATTERN, null),
                               blackboard);
            bpTLV = anyTLV.findTag(SCTTag.OFFSET, null);
            int offset = (bpTLV != null) ? bpTLV.valueAsNumber() : 0;
            bpTLV = anyTLV.findTag(SCTTag.SIZE, null);
            int size = (bpTLV != null) ?
              bpTLV.valueAsNumber() : (pattern.length - offset);
            argumentPart = new byte[size];

            System.arraycopy(argument, offset, argumentPart, 0, size);
            break;
          }
          else {
            pattern = anyTLV.valueAsByteArray();
            argumentPart = argument;
          }

          if (! arraysEqual(pattern, argumentPart))
            throw new CardServiceOperationFailedException
              (msg("Argument" + HexString.hexify(argumentPart) +
                   "doesn't match given data " +
                   HexString.hexify(pattern)));
          break;
        }
      }
    }
  }

  /**
   * Handles a standard request.
   * The handling for most types of requests is identical. Some preprocessing
   * has to be done, the request is passed to the handler chain, and the result
   * is evaluated. This is exactly what this method does.
   *
   * @param type        the kind of request, as define by the constants in
   *                    class <tt>Request</tt>
   * @param argument    the evaluated argument of the request
   * @param parameter   the evaluated parameter of the request
   * @param result      the expected result of the request
   * @param blackboard  the data area of this execution environment
   *
   * @see com.ibm.opencard.handler.Request
   */
  private void handle0815Request(int    type,
                                 byte[] argument,
                                 byte[] parameter,
                                 TLV    result,
                                 TLVBuffer blackboard)
       throws CardServiceOperationFailedException, CardServiceException
  {
    request.setID(type);
    request.argument().set(argument);   // check for null ?
    request.parameter().set(parameter);

    boolean success = false;
    try {
      success = handlerChain.handle(request);
    } catch(CardServiceException csx) {
      tracer.error("handle0815Request", csx);
      throw new CardServiceOperationFailedException(msg(csx.toString()));
    } catch(CardTerminalException ctx) {
      tracer.error("handle0815Request", ctx);
      throw new CardServiceOperationFailedException(msg(ctx.toString()));
    }

    if (success)
      executeResultSection(result, request.argument().data(), blackboard);
    else
      throw new CardServiceOperationFailedException
        (msg("request not handled by chain"));

  } // handle0815Request


  /**
   * Handles an unsupported request.
   * Actually, an unsupported request cannot be handled. Instead, an
   * exception is thrown that indicates that the request is not supported.
   * The type of the request is passed as a string argument, which will
   * be included in the detail message of the exception.
   *
   */
  private void handleUnsupportedRequest(String type)
       throws CardServiceInabilityException
  {
    throw new CardServiceInabilityException
      (msg("request " + type + " not supported"));
  }


  /*
  **************************************************************************
  * Handle an AGENT request.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  /*
  private void handleAgentRequest(byte[] argument, byte[] parameter, TLV result, TLVBuffer blackboard)
       throws CardServiceException, CardTerminalException
  {
    boolean success = false;

    request.setID(Request.AGENT);
    if (argument != null)
      request.argument().set(argument);
    request.parameter().set(parameter);

    try {
      success = handlerChain.handle(request);
    } catch (Exception e) {
      tracer.error("handleAgentRequest", e);
      throw new CardServiceException(msg("request handling failed"));
    }
    if (success) {
      executeResultSection(result, request.argument().data(), blackboard);
    } else {
      throw new CardServiceException(msg("request handling failed"));
    }
  }
  */

  /*
  **************************************************************************
  * Handle an AGENCY request.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  /*
  private void handleAgencyRequest(byte[] argument, byte[] parameter,
                                  TLV result, TLVBuffer blackboard)
  throws CardServiceException, CardTerminalException
  {
    request.setID(Request.AGENCY);
    request.argument().set(argument);
    request.parameter().set(parameter);

    throw new CardServiceInabilityException(msg("AGENCY_REQUEST not supported"));
  }
  */

  /*
  **************************************************************************
  * Handle an AUTHENTICATION request.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  /*
  private void handleAuthenticationRequest(byte[] argument, byte[] parameter,
                                           TLV result, TLVBuffer blackboard)
  throws CardServiceException
  {
    TLV bufferEntryTLV = null;

    // argument = agent.doPadding(argument);
    request.setID(Request.AUTHENTICATION);
    request.argument().set(argument);
    request.parameter().set(parameter);

    try {
      handlerChain.handle(request);
    } catch (Exception e) {
      tracer.error("handleAuthenticationRequest", e);
      throw new CardServiceException(msg("request handling failed"));
    }
    executeResultSection(result, request.argument().data(), blackboard);
  }
  */

  /**************************************************************************
  * Handle a BUFFER request.
  * Buffer requests are a special case since they do not invoke the handler
  * chain.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  private void handleBufferRequest(byte[] argument, byte[] parameter,
                                   TLV result, TLVBuffer blackboard)
  throws CardServiceException, CardTerminalException
  {
    request.setID(Request.BUFFER);
    request.argument().set(argument);
    request.parameter().set(parameter);

    executeResultSection(result, argument, blackboard);
  }

  /**************************************************************************
  * Handle a SIGNATURE request.
  * Signature requests are special since some padding is done on the argument.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  private void handleSignatureRequest(byte[] argument, byte[] parameter,
                                      TLV result, TLVBuffer blackboard)
       throws CardServiceException
  {
    TLV bufferEntryTLV = null;

    /**/
    // replace this by argument = agent.doPadding(argument);
    int overallLength = argument.length;
    int remainder = argument.length % 8;
    byte[] filler = { (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                      (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    if (remainder>0)
      overallLength += 8-remainder;
    byte[] paddedArgument = new byte[overallLength];
    System.arraycopy(argument, 0, paddedArgument, 0, argument.length);
    System.arraycopy(filler,   0, paddedArgument, argument.length,
                     overallLength-argument.length);
    /**/

    request.setID(Request.SIGNATURE);
    request.argument().set(paddedArgument);
    request.parameter().set(parameter);

    try {
      handlerChain.handle(request);
    } catch (Exception e) {
      tracer.error("handleSignatureRequest", e);
      throw new CardServiceException(msg("request handling failed"));
    }
    executeResultSection(result, request.argument().data(), blackboard);
  }

  /*
  **************************************************************************
  * Handle a VALIDATION request.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  /*
  private void handleValidationRequest(byte[] argument, byte[] parameter,
                                       TLV result, TLVBuffer blackboard)
       throws CardServiceException
  {
    TLV bufferEntryTLV = null;

    request.setID(Request.VALIDATION);
    request.argument().set(argument);
    request.parameter().set(parameter);

    try {
      handlerChain.handle(request);
    } catch (Exception e) {
      tracer.error("handleValidationRequest", e);
      throw new CardServiceException(msg("request handling failed"));
    }
    executeResultSection(result, request.argument().data(), blackboard);
  }
  */

  /**************************************************************************
  * Handle a CARD request.
  * Card requests are special, since they are handled directly, not via
  * the handler chain.
  *
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  private void handleCardRequest(byte[] argument, byte[] parameter,
                                 TLV result, TLVBuffer blackboard)
    throws CardServiceException, CardTerminalException
  {
    request.setID(Request.CARD);
    request.argument().set(argument);

    TLV statusWordsTLV = null;
    TLV bufferEntryTLV = null;

    byte[] body = null;
    CommandAPDU capdu = new CommandAPDU(argument, argument.length);
    ResponseAPDU rapdu = apduSender.sendCommand(capdu);

    executeResultSection(result, rapdu.getBytes(), blackboard);
  }

  /**************************************************************************
  * Handle a CHAINING request.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  private void handleChainingRequest(byte[] argument, byte[] parameter,
                                     TLV result, TLVBuffer blackboard)
    throws CardServiceException, CardTerminalException
  {
    TLV bufferEntryTLV = null;

    byte[] arg = new byte[8];

    request.setID(Request.CHAINING);
    request.argument().set(argument);
    request.parameter().set(parameter);

    try {
      handlerChain.handle(request);
    } catch (Exception e) {
      tracer.error("handleChainingRequest", e);
      throw new CardServiceException(msg("request handling failed"));
    }
    executeResultSection(result, request.argument().data(), blackboard);
  }

  /**************************************************************************
  * Handle an ENCIPHER request.
  * Encipher requests are special since some padding is done here.
  *
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  private void handleEncipherRequest(byte[] argument, byte[] parameter,
                                     TLV result, TLVBuffer blackboard)
    throws CardServiceException, CardTerminalException
  {
    TLV bufferEntryTLV = null;

    /**/
    int overallLength = argument.length;
    int remainder = argument.length % 8;
    byte[] filler = { (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                      (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    if (remainder>0)
      overallLength += 8-remainder;
    byte[] paddedArgument = new byte[overallLength];
    System.arraycopy(argument, 0, paddedArgument, 0, argument.length);
    System.arraycopy(filler,   0, paddedArgument, argument.length,
                     overallLength-argument.length);
    /**/
    // argument = agent.doPadding(argument);
    request.setID(Request.ENCIPHER);
    request.argument().set(argument);
    request.parameter().set(parameter);

    try {
      handlerChain.handle(request);
    } catch (Exception e) {
      tracer.error("handleEncipherRequest", e);
      throw new CardServiceException(msg("request handling failed"));
    }
    executeResultSection(result, request.argument().data(), blackboard);
  }

  /*
  **************************************************************************
  * Handle an DECIPHER request.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  /*
  private void handleDecipherRequest(byte[] argument, byte[] parameter,
                                     TLV result, TLVBuffer blackboard)
  throws CardServiceException, CardTerminalException
  {
    TLV bufferEntryTLV = null;

    request.setID(Request.DECIPHER);
    request.argument().set(argument);
    request.parameter().set(parameter);

    try {
      handlerChain.handle(request);
    } catch (Exception e) {
      tracer.error("handleDecipherRequest", e);
      throw new CardServiceException(msg("request handling failed"));
    }
    executeResultSection(result, request.argument().data(), blackboard);
  }
  */

  /*
  **************************************************************************
  * Handle an EXPORT request.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  /*
  private void handleExportRequest(byte[] argument, byte[] parameter,
                                   TLV result, TLVBuffer blackboard)
  throws CardServiceException, CardTerminalException
  {
    TLV bufferEntryTLV = null;

    request.setID(Request.EXPORT);
    request.argument().set(argument);
    request.parameter().set(parameter);

    try {
      handlerChain.handle(request);
    } catch (Exception e) {
      throw new CardServiceException(msg("request handling failed"));
    }
    executeResultSection(result, request.argument().data(), blackboard);
  }
  */

  /**************************************************************************
  * Handle an IMPORT request.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  private void handleImportRequest(byte[] argument, byte[] parameter,
                                   TLV result, TLVBuffer blackboard)
  throws CardServiceException, CardTerminalException
  {
    request.setID(Request.IMPORT);
    request.argument().set(argument);
    request.parameter().set(parameter);

    throw new CardServiceInabilityException(msg("IMPORT_REQUEST not supported"));
  }

  /*
  **************************************************************************
  * Handle an IDENTIFICATION request.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  /*
  private void handleIdentificationRequest(byte[] argument, byte[] parameter,
                                           TLV result, TLVBuffer blackboard)
  throws CardServiceException, CardTerminalException
  {
    TLV bufferEntryTLV = null;

    byte[] arg = new byte[8];

    request.setID(Request.IDENTIFICATION);
    request.argument().set(arg);
    request.parameter().set(parameter);

    try {
      handlerChain.handle(request);
    } catch (Exception e) {
      tracer.error("handleIdentificationRequest", e);
      throw new CardServiceException(msg("request handling failed"));
    }
    executeResultSection(result, request.argument().data(), blackboard);
  }
  */

  /*
  **************************************************************************
  * Handle a FUNCTION request.
  * @param argument   evaluated argument section of request
  * @param parameter  evaluated parameter section of request
  * @param result     result of request
  * @param blackboard blackboard to be used
  **************************************************************************/
  /*
  private void handleFunctionRequest(byte[] argument, byte[] parameter,
                                     TLV result, TLVBuffer blackboard)
  throws CardServiceException, CardTerminalException
  {
    request.setID(Request.FUNCTION);
    request.argument().set(argument);
    request.parameter().set(parameter);

    throw new CardServiceInabilityException(msg("FUNCTION_REQUEST not supported"));
  }
  */

  /**************************************************************************
  * Find the procedure with the given name withing the given TLV object.
  * @param scriptTLV the TLV containing the procedure to be searched for.
  * @param order     the ID (name) of the procedure to be serached for.
  * @exception       opencard.core.service.CardServiceResourceNotFoundException
  *                  thrown when no procedure with the given ID can be found
  *                  in the script
  **************************************************************************/
  private TLV findProcedure(TLV scriptTLV, String order)
  throws CardServiceResourceNotFoundException
  {
    int offset[]     = {0};
    TLV procedureTLV = null;
    TLV idTLV        = null;

    while ((procedureTLV = scriptTLV.findTag(SCTTag.PROCEDURE, procedureTLV)) != null) {
      idTLV = procedureTLV.findTag(SCTTag.ID, null);
      if (order.equals(new String(idTLV.valueAsByteArray()))) {
        return procedureTLV;
      }
    }
    throw new CardServiceResourceNotFoundException("Could not find " + order);
  }

  /**************************************************************************
  * Print the description area of the given script.
  * The Description area is a TLV that looks like this:
  * DESCRIPTION
  * (
  *   ATR_HISTORICAL_DATA ( ... )
  *   CARD_LEVEL ( ... )
  *   ID ( ... )
  *   CARD_OS ( ... )
  * )
  * @param scriptTLV - TLV containing the script
  **************************************************************************/
  private void printDescription(TLV scriptTLV)
  {
    TLV descriptionTLV = scriptTLV.findTag(SCTTag.DESCRIPTION, null);

    try {
      System.out.println("ATR_HISTORICAL_DATA = " +
                         new String(descriptionTLV.findTag(SCTTag.ATR_HISTORICAL_DATA, null)
                                                  .valueAsByteArray()));
      System.out.println("CARD_LEVEL          = " +
                         new String(descriptionTLV.findTag(SCTTag.CARD_LEVEL, null)
                                                  .valueAsByteArray()));
      System.out.println("ID                  = " +
                         new String(descriptionTLV.findTag(SCTTag.ID, null)
                                                  .valueAsByteArray()));
      System.out.println("CARD_OS             = " +
                         descriptionTLV.findTag(SCTTag.CARD_OS, null).valueAsNumber());
    } catch (NullPointerException e) {
      ;
    }
  }

  boolean arraysEqual(byte[] a, byte[] b)
  {
    if (a.length == b.length) {
      for (int i = 0; i < a.length; i++)
        if (a[i] != b[i]) return false;

      return true;
    }
    else
      return false;
  }

  private String msg(String s)
  {
    return s + getErrorMessage() + getStackTrace();
  }

  private String getErrorMessage()
  {
    return "\nProcedure : " + new String(currentProcedureID.valueAsByteArray()) +
           "\nRequest : " + ((currentRequestID != null) ? HexString.hexify(currentRequestID.valueAsByteArray()) : "" +
           "\n" + request);
  }
  private String getStackTrace()
  {
    String result = new String("\nCall Stack:\n");
    while (! callStack.empty())
      result += new String(((TLV) callStack.pop()).valueAsByteArray()) + "\n";

    return result;
  }

} // class ScriptInterpreter
