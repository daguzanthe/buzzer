package com.example.buzzer_thomas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    ClientMQTT clientMQTT = null;
    ListView leaderboard;

    String selected_user = null;
    boolean available = true;

    Button btn_vrai;
    Button btn_faux;
    Button btn_reset;

    TextView selected;

    CustomAdapter customAdapter;

    ArrayList<User> userList = new ArrayList<User>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_vrai = findViewById(R.id.button_vrai);
        btn_faux = findViewById(R.id.button_faux);
        btn_reset = findViewById(R.id.btn_reset);

        leaderboard = findViewById(R.id.leaderboard);
        customAdapter = new CustomAdapter(getApplicationContext(), userList);
        leaderboard.setAdapter(customAdapter);

        clientMQTT = new ClientMQTT(getApplicationContext());

        demarrerMQTT();

        btn_reset.setOnClickListener(v -> {
            available = true;
            selected_user = null;
            while(userList.size() != 0) {
                userList.remove(0);
            }
            clientMQTT.publishMessage("false");
            customAdapter.notifyDataSetChanged();
        });

        btn_faux.setOnClickListener(v -> {
            available = true;
            selected_user = null;
            clientMQTT.publishMessage("false");
        });
        btn_vrai.setOnClickListener(v -> {
            if (selected_user == null) {
                clientMQTT.publishMessage("false");
                available = true;
            } else {
                clientMQTT.publishMessage("true");
                available = true;
                for (User user : userList) {
                    if (user.getMac().equals(selected_user)) {
                        user.setPts(user.getPts() + 1);
                        break;
                    }
                }
                selected_user = null;
                customAdapter.notifyDataSetChanged();
            }
        });
    }

    private void demarrerMQTT()
    {
        clientMQTT.reconnecter();

        clientMQTT.mqttAndroidClient.setCallback(new MqttCallbackExtended()
        {
            @Override
            public void connectComplete(boolean b, String s)
            {
                Log.w(TAG,"connectComplete");
                clientMQTT.souscrire("player/login");
                clientMQTT.souscrire("player/action");
            }

            @Override
            public void connectionLost(Throwable throwable)
            {
                Log.w(TAG,"connectionLost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception
            {
                Log.w(TAG, "messageArrived : " + mqttMessage.toString());
                if (topic.equals("player/login")) {
                    String[] log = mqttMessage.toString().split(",");
                    boolean is_new = true;
                    for (User user : userList) {
                        if (user.getMac().equals(log[0])) {
                            is_new = false;
                            break;
                        }
                    }
                    if (is_new) {
                        userList.add(new User(log[0], log[1], 0));
                        customAdapter.notifyDataSetChanged();
                        Log.i(TAG, "User added. Total : " + userList.size());
                    }
                }
                if (topic.equals("player/action") && available) {
                    String mac = mqttMessage.toString();
                    boolean logged = false;
                    for(User user : userList) {
                        if (user.getMac().equals(mac)) {
                            logged = true;
                            break;
                        }
                    }
                    if (logged) {
                        selected_user = mqttMessage.toString();
                        available = false;
                        clientMQTT.publishMessage(selected_user);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
            {
                Log.w(TAG, "deliveryComplete");
            }
        });
    }
}