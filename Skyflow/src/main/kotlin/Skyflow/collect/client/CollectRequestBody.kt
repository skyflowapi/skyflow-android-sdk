package Skyflow.collect.client

import Skyflow.Callback
import org.json.JSONArray
import org.json.JSONObject
import Skyflow.Element
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.LogLevel
import Skyflow.collect.elements.validations.ElementValueMatchRule
import com.google.gson.JsonObject
import kotlin.Exception

class CollectRequestBody {
    companion object {
        private val tag = CollectRequestBody::class.qualifiedName
        internal fun createRequestBody(
            elements: MutableList<Element>,
            additionalFields: JSONObject,
            callback: Callback,
            logLevel: LogLevel
        ) : String
        {
            val tableMap: HashMap<String,MutableList<CollectRequestRecord>> = HashMap()
            val tableWithColumn : HashSet<String> = HashSet()
            for (element in elements) {
                if (tableMap[(element.tableName)] != null){
                    if(tableWithColumn.contains(element.tableName+element.columnName))
                    {
                     //   callback.onFailure(Exception("duplicate column "+element.columnName+ " found in "+element.tableName))
                        var hasElementValueMatchRule: Boolean = false
                        for(validation in element.collectInput.validations.rules) {
                            if(validation is ElementValueMatchRule) {
                                hasElementValueMatchRule = true
                                break;
                            }
                        }
                        if(!hasElementValueMatchRule)
                        {
                            val error = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,
                                tag, logLevel, arrayOf(element.tableName,element.columnName))
                            callback.onFailure(error)
                            return ""
                        }
                        continue;
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
                    if(additionalFields.get("records") !is JSONArray)
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.INVALID_RECORDS, tag, logLevel))
                        return ""
                    }
                    val records = additionalFields.getJSONArray("records")
                    if(records.length() == 0)
                    {
                        throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, tag,logLevel)

                    }
                    var i = 0
                    while (i < records.length()) {
                        val jsonobj = records.getJSONObject(i)
                        if(!jsonobj.has("table"))
                        {
                            callback.onFailure(SkyflowError(SkyflowErrorCode.MISSING_TABLE, tag, logLevel))
                            return ""
                        }
                        else if(!jsonobj.has("fields"))
                        {
                            callback.onFailure(SkyflowError(SkyflowErrorCode.FIELDS_KEY_ERROR, tag, logLevel))
                            return ""

                        }
                        else if(jsonobj.getJSONObject("fields").toString() == "{}")
                        {
                            callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_FIELDS, tag, logLevel))
                            return ""

                        }
                        val tableName = jsonobj.get("table")
                        if(tableName !is String)
                           throw SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME, tag, logLevel)
                        if(tableName.isEmpty())
                            throw SkyflowError(SkyflowErrorCode.EMPTY_TABLE_NAME, tag, logLevel)
                        if(!jsonobj.getJSONObject("fields").toString().equals("{}")) {
                            val fields = jsonobj.getJSONObject("fields")
                            val keys = fields.names()
                            val field_list = mutableListOf<CollectRequestRecord>()
                            for (j in 0 until keys!!.length()) {
                                if(keys.getString(j).isEmpty())
                                {
                                    callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_NAME, tag, logLevel))
                                    return ""
                                }
                                val obj = CollectRequestRecord(keys.getString(j),
                                    fields.get(keys.getString(j)))
                                field_list.add(obj)
                            }
                            if (tableMap[tableName] != null) {
                                for (k in 0 until field_list.size) {
                                    if (tableWithColumn.contains(tableName + field_list.get(k).columnName)) {
                                        val error = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,
                                            tag, logLevel, arrayOf(tableName,field_list[k].columnName))
                                        callback.onFailure(error)
                                        return ""

                                    } else {
                                        tableWithColumn.add(tableName + field_list.get(k).columnName)
                                    }
                                }
                                tableMap[tableName]!!.addAll(field_list)
                            } else {
                                val tempArray = mutableListOf<CollectRequestRecord>()
                                for (k in 0 until field_list.size) {
                                    if (tableWithColumn.contains(tableName + field_list.get(k).columnName)) {
//                                        callback.onFailure(Exception("duplicate column " + field_list.get(
//                                            k).columnName + " found in " + tableName))
                                        val error = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,
                                            tag, logLevel, arrayOf(tableName,field_list[k].columnName))
                                        callback.onFailure(error)
                                        return ""

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
                   // val skyflowError = SkyflowError(tag = tag, logLevel = logLevel, params = arrayOf(e.message.toString()))
                    callback.onFailure(exception = e)
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