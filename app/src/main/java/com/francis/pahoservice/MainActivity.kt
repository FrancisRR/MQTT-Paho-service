package com.francis.pahoservice

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.francis.pahoservice.base.AppController
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.internal.Token
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private val TAG by lazy { MainActivity::class.java.simpleName }
    private var clientId: String = ""
    private val topic: String = "foo/bar"
    private val qos: Int = 1
    private val serverUri: String = "tcp://broker.hivemq.com:1883"
    private var mqttAndroidClient: MqttAndroidClient? = null
    private val stringBuilder: StringBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edMessage.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            checkInputMessage()
            return@setOnEditorActionListener false
        }


        btPublish.setOnClickListener {
            checkInputMessage()
        }

        clientId = MqttClient.generateClientId()
        mqttAndroidClient = MqttAndroidClient(AppController.instance, serverUri, clientId)
        mqttAndroidClient?.connect()?.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                UiUtils.appErrorLog(TAG, "Connection success")
                subscribe()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                UiUtils.appErrorLog(TAG, "Connection fail")
            }
        }


        mqttAndroidClient?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                stringBuilder.append("${message.toString()}")
                stringBuilder.append("\n")
                tvMessageView.text = stringBuilder.toString()
                UiUtils.appErrorLog(TAG, "${stringBuilder.toString()} ${message.toString()}")
            }

            override fun connectionLost(cause: Throwable?) {
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                edMessage.text.clear()
            }
        })


    }

    private fun checkInputMessage() {
        val inputMessage: String = edMessage.text.toString().trim()
        if (!inputMessage.isEmpty()) {
            publishMessage("${Build.MODEL} : $inputMessage")
        } else {
            UiUtils.showToast("Please enter the message")
        }
    }

    private fun subscribe() {
        mqttAndroidClient?.subscribe(topic, qos)?.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                UiUtils.appErrorLog(TAG, "Subscription success")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                UiUtils.appErrorLog(TAG, "Subscription fail")
            }
        }
    }

    private fun publishMessage(message: String) {
        try {
            val byteArray: ByteArray = message.toByteArray()
            val mqttMessage = MqttMessage()
            mqttMessage.payload = byteArray
            mqttMessage.isRetained = false
            mqttAndroidClient?.publish(topic, mqttMessage)?.actionCallback =
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        UiUtils.appErrorLog(TAG, "Message send successfully")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        UiUtils.appErrorLog(TAG, "Message send fail")
                    }
                }

        } catch (e: MqttException) {
            e.printStackTrace();
        }

    }

}