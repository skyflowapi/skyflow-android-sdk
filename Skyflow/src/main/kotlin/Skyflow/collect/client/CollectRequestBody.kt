package Skyflow.collect.client

import Skyflow.Callback
import org.json.JSONArray
import org.json.JSONObject
import Skyflow.Element
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import com.google.gson.JsonObject
import kotlin.Exception

class CollectRequestBody {
    companion object {
        internal fun createRequestBody(
            elements: MutableList<Element>,
            additionalFields: JSONObject,
            callback: Callback
        ) : String
        {
            val tableMap: HashMap<String,MutableList<CollectRequestRecord>> = HashMap()
            val tableWithColumn : HashSet<String> = HashSet()
            for (element in elements) {
                if (tableMap[(element.tableName)] != null){
                    if(tableWithColumn.contains(element.tableName+element.columnName))
                    {
                        val error = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND)
                        error.setErrorResponse(element.tableName,element.columnName)
                        callback.onFailure(error)
                        return ""
                    }
                    tableWithColumn.add(element.tableName+element.columnName)
                    val obj = CollectRequestRecord(element.columnName,element.getValue())
                    tableMap[(element.tableName)]!!.add(obj)
                }
                else{
                    val obj = CollectRequestRecord(element.columnName,element.getValue())
                    val tempArray = mutableListOf<CollectRequestRecord>()
                    tempArray.add(obj)
                    tableWithColumn.add(element.tableName+element.columnName)
                    tableMap[(element.tableName)] = tempArray
                }
            }

            if(!additionalFields.equals(JsonObject()) && additionalFields.has("records"))
            {
                try {
                    if(!(additionalFields.get("records") is JSONArray))
                    {
                        throw SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
                    }
                    val records = additionalFields.getJSONArray("records")
                    if(records.length() == 0)
                    {
                        throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
                    }
                    var i = 0
                    while (i < records.length()) {
                        val jsonobj = records.getJSONObject(i)
                        if(!jsonobj.has("table"))
                        {
                            throw SkyflowError(SkyflowErrorCode.MISSING_TABLE)
                        }
                        else if (jsonobj.get("table") !is String)
                        {
                            throw SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME)
                        }
                        else if(!jsonobj.has("fields"))
                        {
                           throw SkyflowError(SkyflowErrorCode.FIELDS_KEY_ERROR)

                        }
                        else if(jsonobj.getJSONObject("fields").toString().equals("{}"))
                        {
                            throw SkyflowError(SkyflowErrorCode.EMPTY_FIELDS)
                        }
                        val tableName = jsonobj.get("table")
                        if(tableName !is String)
                           throw SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME)
                        if(tableName.isEmpty())
                            throw SkyflowError(SkyflowErrorCode.EMPTY_TABLE_NAME)
                        if(!jsonobj.getJSONObject("fields").toString().equals("{}")) {
                            val fields = jsonobj.getJSONObject("fields")
                            val keys = fields.names()
                            val field_list = mutableListOf<CollectRequestRecord>()
                            for (j in 0 until keys!!.length()) {
                                if(keys.getString(j).isEmpty())
                                {
                                    callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_NAME))
                                    return ""
                                }
                                val obj = CollectRequestRecord(keys.getString(j),
                                    fields.get(keys.getString(j)))
                                field_list.add(obj)
                            }
                            if (tableMap[tableName] != null) {
                                for (k in 0 until field_list.size) {
                                    if (tableWithColumn.contains(tableName + field_list.get(k).columnName)) {
                                        val error = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND)
                                        error.setErrorResponse(tableName,field_list.get(k).columnName)
                                        throw error
                                    } else {
                                        tableWithColumn.add(tableName + field_list.get(k).columnName)
                                    }
                                }
                                tableMap[tableName]!!.addAll(field_list)
                            } else {
                                val tempArray = mutableListOf<CollectRequestRecord>()
                                for (k in 0 until field_list.size) {
                                    if (tableWithColumn.contains(tableName + field_list.get(k).columnName)) {
                                        val error = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND)
                                        error.setErrorResponse(tableName,field_list.get(k).columnName)
                                        throw error
                                    } else
                                        tableWithColumn.add(tableName + field_list.get(k).columnName)
                                }
                                tempArray.addAll(field_list)
                                tableMap[tableName] = tempArray
                            }
                        }
                        i++
                    }
                }
                catch (e:Exception)
                {
                    val skyflowError = SkyflowError()
                    skyflowError.setErrorMessage(e.message.toString())
                    callback.onFailure(skyflowError)
                    return ""
                }
            }
            val recordsArray = JSONArray()
            val requestObject = JSONObject()
            for ((key, value ) in tableMap){
                val recordObject = JSONObject()
                recordObject.put("table", key)
                val fieldsObject = JSONObject()
                for (element in value){
                    createJSONKey(fieldsObject, element.columnName, element.value)
                }
                recordObject.put("fields", fieldsObject)
                recordsArray.put(recordObject)
            }
            requestObject.put("records", recordsArray)
            return requestObject.toString()
        }

        private fun createJSONKey(fieldsObject: JSONObject, columnName: String, value: Any){
            val keys = columnName.split(".").toTypedArray()
            if(fieldsObject.has(keys[0])){
                if(keys.size > 1){
                    createJSONKey(fieldsObject.get(keys[0]) as JSONObject, keys.drop(1).joinToString("."), value)
                }
            }else{
                if(keys.size > 1){
                    val tempObject = JSONObject()
                    fieldsObject.put(keys[0], tempObject)
                    createJSONKey(tempObject, keys.drop(1).joinToString("."), value)
                }
                else {
                    fieldsObject.put(keys[0], value)
                }
            }
        }
    }
}