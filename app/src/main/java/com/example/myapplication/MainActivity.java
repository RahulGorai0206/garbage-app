package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import org.json.JSONObject;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mSocket = IO.socket("https://garbage-tracking-backend.onrender.com/comcoordinates/send-coordinates");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (mSocket != null) {
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).on("coordinatesUpdated", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    final JSONObject data = (JSONObject) args[0];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                double latitude = data.getDouble("latitude");
                                double longitude = data.getDouble("longitude");
                                Toast.makeText(MainActivity.this, "Received coordinates: Lat: " + latitude + ", Long: " + longitude, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Disconnected from server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            mSocket.connect();
        }

        // Start sending coordinates
        sendCoordinatesToServer(0.0, 0.0); // You can pass initial coordinates here
    }

    private void sendCoordinatesToServer(double latitude, double longitude) {
        try {
            if (mSocket != null && mSocket.connected()) {
                mSocket.emit("coordinatesUpdated", new JSONObject()
                        .put("latitude", latitude)
                        .put("longitude", longitude));
                Toast.makeText(MainActivity.this, "Coordinates sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Socket not connected", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Failed to send coordinates", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            mSocket.off();
        }
    }
}
