package Skyflow

import Skyflow.core.Messages


enum class SkyflowErrorCode(val code:Int, var message:String)
{

    INVALID_VAULT_ID(400, Messages.INVALID_VAULT_ID.message),
    INVALID_VAULT_URL(400,Messages.INVALID_VAULT_URL.message),
    EMPTY_VAULT_ID(400,Messages.EMPTY_VAULT_ID.message),
    EMPTY_VAULT_URL(400,Messages.EMPTY_VAULT_URL.message),
    INVALID_BEARER_TOKEN(400,Messages.INVALID_BEARER_TOKEN.message),
    INVALID_TABLE_NAME(400,Messages.INVALID_TABLE_NAME.message),
    EMPTY_TABLE_NAME(400,Messages.EMPTY_TABLE_NAME.message),
    RECORDS_KEY_NOT_FOUND(400,Messages.RECORDS_KEY_NOT_FOUND.message),
    EMPTY_RECORDS(400,Messages.EMPTY_RECORDS.message),
    TABLE_KEY_ERROR(400,Messages.TABLE_KEY_ERROR.message),
    FIELDS_KEY_ERROR(400,Messages.FIELDS_KEY_ERROR.message),
    INVALID_COLUMN_NAME(400,Messages.INVALID_COLUMN_NAME.message),
    EMPTY_COLUMN_NAME(400,Messages.EMPTY_COLUMN_NAME.message),
    INVALID_TOKEN_ID(400,Messages.INVALID_TOKEN_ID.message),  // response is in success only, getting both successful and unsuccessful records
    EMPTY_TOKEN_ID(400,Messages.EMPTY_TOKEN_ID.message),
    ID_KEY_ERROR(400,Messages.ID_KEY_ERROR.message),
    REDACTION_KEY_ERROR(400,Messages.REDACTION_KEY_ERROR.message),
    INVALID_REDACTION_TYPE(400,Messages.INVALID_REDACTION_TYPE.message),
    INVALID_FIELD(400,Messages.INVALID_FIELD.message),
    MISSING_TOKEN(400,Messages.MISSING_TOKEN.message),
    MISSING_IDS(404,Messages.MISSING_KEY_IDS.message),
    EMPTY_RECORD_IDS(400,Messages.EMPTY_RECORD_IDS.message),
    INVALID_RECORD_ID_TYPE(400,Messages.INVALID_RECORD_ID_TYPE.message),
    MISSING_TABLE(400,Messages.MISSING_TABLE.message),
    INVALID_RECORD_TABLE_VALUE(400,Messages.INVALID_RECORD_TABLE_VALUE.message),
    INVALID_GATEWAY_URL(400,Messages.INVALID_GATEWAY_URL.message),
    EMPTY_GATEWAY_URL(400,Messages.EMPTY_GATEWAY_URL.message),
    INVALID_INPUT(400,Messages.INVALID_INPUT.message),
    REQUIRED_INPUTS_NOT_PROVIDED(400,Messages.REQUIRED_INPUTS_NOT_PROVIDED.message),
    INVALID_EVENT_TYPE(400,Messages.INVALID_EVENT_TYPE.message),
    INVALID_EVENT_LISTENER(400,Messages.INVALID_EVENT_LISTENER.message),
    UNKNOWN_ERROR(400,Messages.UNKNOWN_ERROR.message),
    TRANSACTION_ERROR(400,Messages.TRANSACTION_ERROR.message),
    CONNECTION_ERROR(400,Messages.CONNECTION_ERROR.message),
    MISSING_REDACTION_VALUE(400,Messages.MISSING_REDACTION_VALUE.message),
    ELEMENT_NOT_MOUNTED(400,Messages.ELEMENT_NOT_MOUNTED.message),
    DUPLICATE_COLUMN_FOUND(400,Messages.DUPLICATE_COLUMN_FOUND.message),
    DUPLICATE_ELEMENT_FOUND(400,Messages.DUPLICATE_ELEMENT_FOUND.message),
    INVALID_RECORDS(400,Messages.INVALID_RECORDS_TYPE.message),
    INVALID_RECORD_IDS(400,Messages.INVALID_RECORD_IDS.message),
    MISSING_REDACTION(400,Messages.MISSING_REDACTION.message),
    EMPTY_KEY_IN_QUERY_PARAMS(400,Messages.EMPTY_KEY_IN_QUERY_PARAMS.message),
    EMPTY_KEY_IN_PATH_PARAMS(400,Messages.EMPTY_KEY_IN_PATH_PARAMS.message),
    EMPTY_KEY_IN_REQUEST_HEADER_PARAMS(400,Messages.EMPTY_KEY_IN_REQUEST_HEADER_PARAMS.message),
    INVALID_FIELD_IN_PATH_PARAMS(400,Messages.INVALID_FIELD_IN_PATH_PARAMS.message),
    INVALID_FIELD_IN_QUERY_PARAMS(400,Messages.INVALID_FIELD_IN_QUERY_PARAMS.message),
    INVALID_FIELD_IN_REQUEST_HEADER_PARAMS(400,Messages.INVALID_FIELD_IN_REQUEST_HEADER_PARAMS.message),
    FAILED_TO_REVEAL(400,Messages.FAILED_TO_REVEAL.message),
    NOT_FOUND_IN_RESPONSE(400,Messages.NOT_FOUND_IN_RESPONSE.message),
    BAD_REQUEST(400,Messages.BAD_REQUEST.message),
    MISSING_COLUMN(400,Messages.MISSING_COLUMN.message),
    EMPTY_FIELDS(400,Messages.EMPTY_FIELDS.message);

    @JvmName("getCode1")
    fun getCode() : Int
    {
        return this.code
    }

    @JvmName("getMessage1")
    fun getMessage() : String
    {
        return this.message
    }
}