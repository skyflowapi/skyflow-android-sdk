package com.Skyflow

import Skyflow.*
import Skyflow.soap.SoapConnectionConfig
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log


class SoapActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soap)


        val config = Configuration(tokenProvider = CollectActivity.DemoTokenProvider())
        val skyflowClient = Skyflow.init(config)
        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val collectContainer = skyflowClient.container(ContainerType.COLLECT)

        val cardNumberInput = CollectElementInput(type =  SkyflowElementType.CARD_NUMBER)
        val cardNumberElement = collectContainer.create(this,cardNumberInput)

        val expiryDateInput = CollectElementInput(type =  SkyflowElementType.EXPIRATION_DATE)
        val expiryDateElement = collectContainer.create(this,expiryDateInput)

        val cvvInput = RevealElementInput()
        val cvvElement = revealContainer.create(this,cvvInput)


        val cardNumberID = cardNumberElement.getID()  // to get element ID
        val expiryDateID = expiryDateElement.getID()
        val cvvElementID = cvvElement.getID()

        val requestXML = """
                        <soapenv:Envelope>
                    <soapenv:Header>
                    <ClientID>1234</ClientID>
                    </soapenv:Header>
                    <soapenv:Body>
                    <GenerateCVV>
                    <CardNumber>
                    <Skyflow>${cardNumberID}</Skyflow>
                    </CardNumber>
                    <ExpiryDate>
                    <Skyflow>${expiryDateID}</Skyflow>
                    </ExpiryDate>
                    </GenerateCVV>
                    </soapenv:Body>
                    </soapenv:Envelope>
        """.trimIndent()

        val httpHeaders = HashMap<String, String>()
        httpHeaders.put("SOAPAction", "")

        val responseXML = """
                        <soapenv:Envelope>
                    <soapenv:Body>
                    <GenerateCVV>
                    <CVV>
                    <Skyflow>${cvvElementID}</Skyflow>
                    </CVV>
                    </GenerateCVV>
                    </soapenv:Body>
                    </soapenv:Envelope>
        """.trimIndent()

        val connectionUrl = "https://www.url.com"
        val soapConnectionConfig =  SoapConnectionConfig(connectionUrl, httpHeaders, requestXML, responseXML)

        skyflowClient.invokeSoapConnection(soapConnectionConfig, object : Callback {
            override fun onSuccess(responseBody: Any) {
                Log.d("result:", responseBody.toString())
            }
            override fun onFailure(exception: Any) {
                Log.d("exception:", exception.toString())
                val error = exception as SkyflowError
                val xml = error.getXml()
                if(xml.isNotEmpty()){
                    Log.d("error from server",xml)
                }
            }

        })

    }
}