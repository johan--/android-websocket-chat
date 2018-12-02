package com.example.lmorda.websocketchat;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class WebSocketActivity extends AppCompatActivity {

    private TextView tvOutput;

    private static final int NORMAL_CLOSURE_STATUS = 1000;

    public class Identifier {
        String channel;
//        int userId;
//        int roomId;
    };

    public class Subscription {
        String command;
        Identifier identifier;
        Subscription() {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvOutput = findViewById(R.id.output);



        // WebSocket
        Request request = new Request.Builder().url("ws://sockets.nxtstepdsgn.com/cable").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        OkHttpClient okHttpClient = new OkHttpClient();
        final WebSocket webSocket = okHttpClient.newWebSocket(request, listener);
        okHttpClient.dispatcher().executorService().shutdown();

        final Handler pingHandler = new Handler();
        Runnable pingRunnable = new Runnable() {
            @Override public void run() {
                String ping = "{\"type\":\"ping\",\"message\":\"hello\"}";
                output("Tx: " + ping);
                webSocket.send(ping);
                pingHandler.postDelayed(this, 10000);
            }
        };
        pingHandler.postDelayed(pingRunnable, 10000);
    }

    // WebSocket
    private final class EchoWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {

            output("WebSocket connected to ws://sockets.nxtstepdsgn.com/cable");
            output("Actively listening to sockets.nxtstepdsgn.com for WebSocket traffic");
            output("Sending test echo message");
            String json = "{\"type\":\"chat\",\"message\":\"im online, whats up\"}";

            Identifier identifier = new Identifier();
            identifier.channel = "MessagesChannel";
//            identifier.userId = 1;
//            identifier.roomId = 1;

            Subscription subscription = new Subscription();
            subscription.command = "subscribe";
            subscription.identifier = identifier;

            Gson gson = new Gson();
            String subscriptionJson = gson.toJson(subscription);

            output("Tx: " + subscriptionJson);

            webSocket.send(subscriptionJson);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output("Rx: " + text);
            // Check the WebSocket message type
            if (text.contains("chat")) {
                // do something chat related
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output("Rx bytes: " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            output("Closed: " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            output("Error: " + t.getMessage());
        }
    }

    private void output(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("websocketchat", txt);
                tvOutput.setText(tvOutput.getText().toString() + "\n\n" + txt);
            }
        });
    }

}
