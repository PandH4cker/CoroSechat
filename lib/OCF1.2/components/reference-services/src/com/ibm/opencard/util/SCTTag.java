/*==============================================================================
 *  (C) COPYRIGHT INTERNATIONAL BUSINESS MACHINES CORPORATION 1997.
 *                       ALL RIGHTS RESERVED
 *                 IBM Deutschland Entwicklung GmbH
 *============================================================================*/

package com.ibm.opencard.util;

import opencard.opt.util.Tag;

public interface SCTTag
{
  public final static Tag ID                      = new Tag(   0, (byte)2, false);
  public final static Tag SIZE                    = new Tag(   1, (byte)2, false);
  public final static Tag OFFSET                  = new Tag(   2, (byte)2, false);
  public final static Tag DATA                    = new Tag(   3, (byte)2, false);
  public final static Tag DATA_ENTRY              = new Tag(   4, (byte)2, false);
  public final static Tag ITEM_ENTRY              = new Tag(   5, (byte)2, false);
  public final static Tag ITEM_PATH               = new Tag(   6, (byte)2, false);
  public final static Tag ITEM_TYPE               = new Tag(   7, (byte)2, false);
  public final static Tag ACCESS_MODE             = new Tag(   8, (byte)2, false);
  public final static Tag RECORD_MODE             = new Tag(   9, (byte)2, false);
  public final static Tag RECORD_NUMBER           = new Tag(  10, (byte)2, false);
  public final static Tag CHV_NUMBER              = new Tag(  11, (byte)2, false);
  public final static Tag CHV_DOMAIN              = new Tag(  12, (byte)2, false);
  public final static Tag KEY_NUMBER              = new Tag(  13, (byte)2, false);
  public final static Tag KEY_DOMAIN              = new Tag(  14, (byte)2, false);
  public final static Tag AUTHENTICATION_DOMAIN   = new Tag(  15, (byte)2, false);
  public final static Tag ARGUMENT                = new Tag(  16, (byte)2, false);
  public final static Tag PARAMETER               = new Tag(  17, (byte)2, false);
  public final static Tag RESULT                  = new Tag(  18, (byte)2, false);
  public final static Tag STATUSWORDS             = new Tag(  19, (byte)2, false);
  public final static Tag CARD_REQUEST            = new Tag(  20, (byte)2, false);
  public final static Tag AGENT_REQUEST           = new Tag(  21, (byte)2, false);
  public final static Tag BUFFER_REQUEST          = new Tag(  22, (byte)2, false);
  public final static Tag BUFFER_ENTRY            = new Tag(  23, (byte)2, false);
  public final static Tag BUFFER_PART             = new Tag(  24, (byte)2, false);
  public final static Tag CONSTANT_PART           = new Tag(  25, (byte)2, false);
  public final static Tag PLACEHOLDER_PART        = new Tag(  26, (byte)2, false);
  public final static Tag AGENT_COMMAND           = new Tag(  27, (byte)2, false);
  public final static Tag ITEM_ALIAS              = new Tag(  28, (byte)2, false);
  public final static Tag ATR_HISTORICAL_DATA     = new Tag(  29, (byte)2, false);
  public final static Tag CARD_OS                 = new Tag(  30, (byte)2, false);
  public final static Tag ACCESS_CONDITION        = new Tag(  31, (byte)2, false);
  public final static Tag AGENCY_REQUEST          = new Tag(  32, (byte)2, false);
  public final static Tag ALGORITHM               = new Tag(  33, (byte)2, false);
  public final static Tag APDU_HEADER             = new Tag(  34, (byte)2, false);
  public final static Tag APPLICATION             = new Tag(  35, (byte)2, false);
  public final static Tag AUTHENTICATION_REQUEST  = new Tag(  36, (byte)2, false);
  public final static Tag CARD_LAYOUT             = new Tag(  37, (byte)2, false);
  public final static Tag CARD_LEVEL              = new Tag(  38, (byte)2, false);
  public final static Tag CARD_TABLE              = new Tag(  39, (byte)2, false);
  public final static Tag CHAINING_REQUEST        = new Tag(  40, (byte)2, false);
  public final static Tag CHV_ACCESS              = new Tag(  41, (byte)2, false);
  public final static Tag CLOSE_LINK_REQUEST      = new Tag(  42, (byte)2, false);
  public final static Tag COMMENT                 = new Tag(  43, (byte)2, false);
  public final static Tag CONDITION               = new Tag(  44, (byte)2, false);
  public final static Tag CONTEXT                 = new Tag(  45, (byte)2, false);
  public final static Tag DECIPHER_REQUEST        = new Tag(  46, (byte)2, false);
  public final static Tag DESCRIPTION             = new Tag(  47, (byte)2, false);
  public final static Tag DESTINATION             = new Tag(  48, (byte)2, false);
  public final static Tag DEVICE                  = new Tag(  49, (byte)2, false);
  public final static Tag DICTIONARY              = new Tag(  50, (byte)2, false);
  public final static Tag ENCIPHER_REQUEST        = new Tag(  51, (byte)2, false);
  public final static Tag ENVIRONMENT             = new Tag(  52, (byte)2, false);
  public final static Tag EXPORT_REQUEST          = new Tag(  53, (byte)2, false);
  public final static Tag FUNCTION                = new Tag(  54, (byte)2, false);
  public final static Tag FUNCTION_REQUEST        = new Tag(  55, (byte)2, false);
  public final static Tag HANDLE                  = new Tag(  56, (byte)2, false);
  public final static Tag IDENTIFICATION_REQUEST  = new Tag(  57, (byte)2, false);
  public final static Tag IMPORT_REQUEST          = new Tag(  58, (byte)2, false);
  public final static Tag INITIAL_CHAINING_VALUE  = new Tag(  59, (byte)2, false);
  public final static Tag ITEM_DF_NAME            = new Tag(  60, (byte)2, false);
  public final static Tag ITEM_TAG                = new Tag(  61, (byte)2, false);
  public final static Tag KEY_ENTRY               = new Tag(  62, (byte)2, false);
  public final static Tag KEY_REFERENCE           = new Tag(  63, (byte)2, false);
  public final static Tag KEY_TABLE               = new Tag(  64, (byte)2, false);
  public final static Tag MASK                    = new Tag(  65, (byte)2, false);
  public final static Tag MATCH                   = new Tag(  66, (byte)2, false);
  public final static Tag MESSAGE_REQUEST         = new Tag(  67, (byte)2, false);
  public final static Tag OPEN_LINK_REQUEST       = new Tag(  68, (byte)2, false);
  public final static Tag ORDER                   = new Tag(  69, (byte)2, false);
  public final static Tag PATTERN                 = new Tag(  70, (byte)2, false);
  public final static Tag PERSONALIZATION_DATA    = new Tag(  71, (byte)2, false);
  public final static Tag PROCEDURE               = new Tag(  72, (byte)2, false);
  public final static Tag PROCEDURE_CALL          = new Tag(  73, (byte)2, false);
  public final static Tag REFERENCE               = new Tag(  74, (byte)2, false);
  public final static Tag REFERENCE_ENTRY         = new Tag(  75, (byte)2, false);
  public final static Tag REPOSITORY              = new Tag(  76, (byte)2, false);
  public final static Tag REPOSITORY_ENTRY        = new Tag(  77, (byte)2, false);
  public final static Tag RESOURCE                = new Tag(  78, (byte)2, false);
  public final static Tag RETURN_CODE             = new Tag(  79, (byte)2, false);
  public final static Tag SCOPE                   = new Tag(  80, (byte)2, false);
  public final static Tag SCRIPT                  = new Tag(  81, (byte)2, false);
  public final static Tag SHORT_IDENTIFIER        = new Tag(  82, (byte)2, false);
  public final static Tag SIGNATURE               = new Tag(  83, (byte)2, false);
  public final static Tag SIGNATURE_REQUEST       = new Tag(  84, (byte)2, false);
  public final static Tag SOURCE                  = new Tag(  85, (byte)2, false);
  public final static Tag TIMESTAMP               = new Tag(  86, (byte)2, false);
  public final static Tag VALIDATION_REQUEST      = new Tag(  87, (byte)2, false);
  public final static Tag VERSION                 = new Tag(  88, (byte)2, false);
  public final static Tag ANSWER_TO_RESET         = new Tag(  89, (byte)2, false);
  public final static Tag SERVER                  = new Tag(  90, (byte)2, false);
  public final static Tag BUFFER_DELETE           = new Tag(  91, (byte)2, false);
  public final static Tag PAD_BYTE                = new Tag(  92, (byte)2, false);
  public final static Tag PAD_SIZE                = new Tag(  93, (byte)2, false);
  public final static Tag PADDING                 = new Tag(  94, (byte)2, false);
  public final static Tag KEY_TYPE                = new Tag(  95, (byte)2, false);
  public final static Tag KEY_OFFSET              = new Tag(  96, (byte)2, false);
  public final static Tag MODULE                  = new Tag(  97, (byte)2, false);
  public final static Tag BUFFER_SIZE             = new Tag(  98, (byte)2, false);
  public final static Tag ITEM_ORGANIZATION       = new Tag(  99, (byte)2, false);
  public final static Tag USER                    = new Tag(1024, (byte)2, false);
}
