package com.cosc4730.program6;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    View view;
    private Button connBTN;
    private Button upBTN;
    private Button downBTN;
    private Button rBTN;
    private Button lBTN;
    private EditText addressET;
    private TextView loggerTV;
    private Thread myNet;
    private int PORT = 3012;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String command = "compsciBot 0 3 2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        upBTN = findViewById(R.id.upButton);
        downBTN = findViewById(R.id.downButton);
        rBTN = findViewById(R.id.rightButton);
        lBTN = findViewById(R.id.leftButton);
        connBTN = findViewById(R.id.connectBTN);
        addressET = findViewById(R.id.addressET);
        loggerTV = findViewById(R.id.loggerTV);
        addressET.getText().toString();

        connBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doNetwork stuff = new doNetwork();
                Log.v("CONNECT", "Connect button pressed.");
                myNet = new Thread(stuff);
                myNet.start();
                Log.v("CONNECT", "Network thread started.");

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            loggerTV.append(msg.getData().getString("msg"));
        }
    };

    public void mkmsg(String str) {
        Log.v("mkmsg", str);
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    public void OnClick(View view) {
        Button button = (Button) view;
        String buttonTxt = button.getText().toString();
        String message;

        switch (buttonTxt) {
            case "UP":
                message = "UP";
                break;

            case "DOWN":
                message = "DOWN";
                break;
            case "L":
                message = "LEFT";
                break;
            case "R":
                message = "RIGHT";
                break;
            case "PEW":
                message = "PEW";
                break;
            default:
                message = "";
        }
        if (out != null && !message.isEmpty()) {
            out.println(message);
            mkmsg("Sent" + message + "\n");
        } else {
            mkmsg("Something went wrong \n");
        }

    }

    //connect to server

    class doNetwork implements Runnable {
        @Override
        public void run() {
            Log.v("doNetwork","running");
            String h = addressET.getText().toString();
            mkmsg(" host is " + h + "\n");
            Log.v("Address", h);
            mkmsg("port is " + PORT + "\n");

            try {
                Log.v("doNetwork", "Try block 1");
                InetAddress serverAddr = InetAddress.getByName(h);
                mkmsg("Attempting connection ..." + h + "\n");
                Log.v("doNetwork", "Connecting to " + h + " on port " + PORT);


                socket = new Socket(serverAddr, PORT);
                mkmsg("Socket initialized" + "\n");
                String initialMessage = "Connected!!!";

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.v("doNetwork", "Streams initialized.");
                mkmsg("Streams initialized\n");

                try {
                    Log.v("doNetwork", "Try block 2");
                    mkmsg("Attempting to send message ...\n ");
                    out.println(initialMessage);
                    Log.v("doNetwork", "Sending initial message.");
                    mkmsg("Message sent");

                    mkmsg("Attempting to receive message ...\n ");
                    String str = in.readLine();
                    mkmsg("Received message:\n" + str + "\n ");
                    Log.v("doNetwork", "Receiving message.");

                    mkmsg("Connection established \n");
                } catch (Exception e) {
                    mkmsg("Error sending or receiving \n ");
                    Log.e("doNetwork", "Error sending or receiving", e);
                }

            } catch (Exception e) {
                mkmsg("Unable to connect" + e.getMessage() + "\n");
                Log.v("doNetwork", "Unable to connect ");
            }
        }
    }
}