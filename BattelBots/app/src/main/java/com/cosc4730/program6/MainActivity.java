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

import javax.xml.transform.TransformerException;

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
        upBTN.setOnClickListener(this::OnClick);
        downBTN.setOnClickListener(this::OnClick);
        rBTN.setOnClickListener(this::OnClick);
        lBTN.setOnClickListener(this::OnClick);




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
        String message = "";

        switch (buttonTxt) {
            case "UP":
                message = "move 0 -1";
                break;

            case "DOWN":
                message = "move 0 1";
                break;
            case "L":
                message = "move -1 0";
                break;
            case "R":
                message = "move 1 0";
                break;
            case "PEW":
                message = " fire 0";
                //message = "PEW";
                break;
            default:
                message = "";
        }

        if (!message.isEmpty()){
            command = message;
            Log.v("command", command);
            loggerTV.setText(command);
        }
        final String finalMessage = message;
        new Thread(() -> {
            if( out != null && !finalMessage.isEmpty()){
                out.println(finalMessage);
                runOnUiThread(() -> mkmsg("Sent " + finalMessage + "\n"));

                out.println("noop");
                runOnUiThread(() -> mkmsg("sent noop \n"));
            } else {
                runOnUiThread(() -> mkmsg("Something went wrong \n") );
            }
        }).start();
    }

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


                socket = new Socket(serverAddr, PORT);
                mkmsg("Socket initialized" + "\n");

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                mkmsg("Streams initialized\n");

                try {
                    Log.v("doNetwork", "Try block 2");
                    mkmsg("Attempting to send message ...\n ");
                    out.println(command);
                    Log.v("doNetwork", "Sending initial message.");
                    mkmsg("Message sent" + "\n");

                    boolean gameRunning = true;
                    while (gameRunning){
                       String serverMessage = in.readLine();
                        if (serverMessage != null){
                            mkmsg("Received message " + serverMessage + "\n");
                            Log.v( "Server Message", serverMessage);

                            if(serverMessage.startsWith("Status")){
                                out.println(command);
                                Log.v("Server Message", serverMessage);
                                mkmsg("Sent command: " + command + "\n");

                            }else if(serverMessage.startsWith("Info Dead") || serverMessage.startsWith("Info GameOver")){
                                gameRunning =false;
                                Log.v("You died", serverMessage);
;                            }

                        }

                    }


                    mkmsg("Connection established \n");
                } catch (Exception e) {
                    mkmsg("Error sending or receiving \n ");
                }

                socket.close();
                in.close();
                out.close();

            } catch (Exception e) {
                mkmsg("Unable to connect" + e.getMessage() + "\n");
                Log.v("doNetwork", "Unable to connect ");

            }
        }
    }
}