package com.example.buzzer_thomas;


import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ClientMQTT {
    private static final String TAG = "ClientMQTT";
    Context context;
    public MqttAndroidClient mqttAndroidClient = null;
    String serverUri = "tcp://broker.hivemq.com:1883";
    int portTTN = 1883;
    String clientId = "mqtt_dg4h6dkjkevkeqbkMKL";
    String subscriptionTopic = "player/login";
    String publishTopic = "buzz";
    String username = "appli";
    String password = "";

    public ClientMQTT(Context context)
    {
        this.context = context;

        creer();
        //connecter();
    }

    public ClientMQTT(Context context, String serverTTN, int portTTN, String applicationId, String deviceId, String password)
    {
        this.context = context;
        this.serverUri = "mqtt://" + serverTTN + ":" + portTTN;
        this.portTTN = portTTN;
        this.clientId = applicationId;
        this.subscriptionTopic = "BuzzerINEM";
        this.publishTopic = "BuzzerINEM";
        this.username = applicationId;
        this.password = password;
        Log.i(TAG, "MqttAndroidClient : serverUri -> " + serverUri);
        Log.i(TAG, "MqttAndroidClient : applicationId -> " + applicationId);
        Log.i(TAG, "MqttAndroidClient : deviceId -> " + deviceId);
        Log.i(TAG, "MqttAndroidClient : subscriptionTopic -> " + subscriptionTopic);
        Log.i(TAG, "MqttAndroidClient : publishTopic -> " + publishTopic);

        creer();
        //connecter();
    }

    public void creer()
    {
        Log.i(TAG, "MqttAndroidClient.creer : serverUri -> " + serverUri);
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended()
        {
            @Override
            public void connectComplete(boolean b, String s)
            {
                Log.w(TAG, "MqttAndroidClient : connectComplete -> " + s + " (" + b + ")");
            }

            @Override
            public void connectionLost(Throwable throwable)
            {
                Log.w(TAG, "MqttAndroidClient : connectionLost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception
            {
                Log.w(TAG, "MqttAndroidClient : messageArrived -> " + mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
            {
                Log.w(TAG, "MqttAndroidClient : deliveryComplete");
            }
        });
    }

    public void setCallback(MqttCallbackExtended callback)
    {
        mqttAndroidClient.setCallback(callback);
    }

    public void connecter()
    {
        Log.i(TAG, "MqttAndroidClient.connecter : username -> " + username);
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        Log.w(TAG, "test5");

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    Log.i(TAG, "connecter : onSuccess");
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    souscrire(subscriptionTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    Log.i(TAG, "connecter : onFailure -> " + serverUri + exception.toString());
                }
            });
        }
        catch (MqttException ex)
        {
            ex.printStackTrace();
            Log.e(TAG, "connecter : exception");
        }
    }

    public void reconnecter()
    {
        Log.w(TAG, "MqttAndroidClient : reconnecter");
        if(estConnecte())
            deconnecter();
        connecter();
    }

    public void deconnecter()
    {
        Log.w(TAG, "MqttAndroidClient : deconnecter");
        Thread deconnexion = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    mqttAndroidClient.disconnect();
                }
                catch (MqttException e)
                {
                    e.printStackTrace();
                    Log.e(TAG, "MqttAndroidClient : deconnecter -> exception");
                }
            }
        });
        // Démarrage
        deconnexion.start();
    }

    public boolean estConnecte()
    {
        Log.w(TAG, "MqttAndroidClient : estConnecte -> " + mqttAndroidClient.isConnected());
        return mqttAndroidClient.isConnected();
    }
    void souscrire(String topic)
    {
        try
        {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    Log.w(TAG,"souscrire : onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    Log.w(TAG, "souscrire : onFailure");
                }
            });

        }
        catch (MqttException ex)
        {
            Log.e(TAG, "souscrire : exception");
            ex.printStackTrace();
        }
    }

    public void publishMessage(String payload) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(publishTopic, message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "publish succeed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public void setParametres(String serverTTN, int portTTN, String applicationId, String deviceId, String password)
    {
        this.serverUri = "tcp://" + serverTTN + ":" + portTTN;
        this.portTTN = portTTN;
        this.clientId = applicationId;
        this.subscriptionTopic = applicationId + "/devices/" + deviceId + "/up";
        this.publishTopic = applicationId + "/devices/" + deviceId + "/down";
        this.username = applicationId;
        this.password = password;
        Log.w(TAG, "MqttAndroidClient : serverUri -> " + serverUri);
        Log.w(TAG, "MqttAndroidClient : applicationId -> " + applicationId);
        Log.w(TAG, "MqttAndroidClient : deviceId -> " + deviceId);
        Log.w(TAG, "MqttAndroidClient : subscriptionTopic -> " + subscriptionTopic);
        Log.w(TAG, "MqttAndroidClient : publishTopic -> " + publishTopic);
    }
}

