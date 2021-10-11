package Skyflow.core

import Skyflow.Callback
import Skyflow.GatewayConfiguration
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.utils.Utils
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class GatewayApiCallback(
    val gatewayConfig : GatewayConfiguration,
    val callback: Callback
) : Callback{

    private val okHttpClient = OkHttpClient()

    override fun onSuccess(responseBody: Any) {
        try{
            if(gatewayConfig.gatewayURL.isEmpty())
            {
                val finalError = JSONObject()
                val errors = JSONArray()
                errors.put(SkyflowError(SkyflowErrorCode.EMPTY_GATEWAY_URL))
                finalError.put("errors",errors)
                callback.onFailure(finalError)
            }
            //adding path params
            val gatewayUrl = Utils.addPathparamsToURL(gatewayConfig.gatewayURL,gatewayConfig.pathParams,callback)
            if(gatewayUrl.equals(""))
                return
            val requestUrlBuilder = HttpUrl.parse(gatewayUrl)?.newBuilder()
            if(requestUrlBuilder == null){
                val error = SkyflowError(SkyflowErrorCode.INVALID_GATEWAY_URL)
                error.setErrorResponse(gatewayConfig.gatewayURL)
                callback.onFailure(Utils.constructError(error))
                return
            }
            //creating url with query params
            val isQueryparamsAdded = Utils.addQueryParams(requestUrlBuilder,gatewayConfig,callback)
            if(!isQueryparamsAdded)
                return
            val requestUrl = requestUrlBuilder.build()

            //body for API
            val body: RequestBody = RequestBody.create(
                MediaType.parse("application/json".toByteArray().toString()), gatewayConfig.requestBody.toString()
            )
            val request = Request
                .Builder()
                .method(gatewayConfig.methodName.toString(), body)
                .addHeader("X-Skyflow-Authorization",responseBody.toString().split("Bearer ")[1])
                .addHeader("Content-Type","application/json")
                .url(requestUrl)
            //adding header
            val isHeaderAdded = Utils.addRequestHeader(request,gatewayConfig,callback)
            if(!isHeaderAdded)
                return

              // Building request
           val  requestBuild = request.build()
           okHttpClient.newCall(requestBuild).enqueue(object : okhttp3.Callback{
                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailure(Utils.constructError(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful)
                        {
                            callback.onFailure(Utils.constructError(Exception(" ${response.body()?.string()}"),response.code()))

                        }
                        else
                        {
                            val responseFromGateway =JSONObject(response.body()!!.string())
                            Utils.constructResponseBodyFromGateway(gatewayConfig.responseBody,responseFromGateway,callback)
                            callback.onSuccess(responseFromGateway)
                        }
                    }
                }
            })
        }catch (e: Exception){
            callback.onFailure(Utils.constructError(e))
        }
    }

    override fun onFailure(exception: Any) {
        callback.onFailure(exception)
    }
}