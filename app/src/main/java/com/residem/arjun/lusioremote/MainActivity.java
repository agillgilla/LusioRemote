package com.residem.arjun.lusioremote;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Vibrator;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static BlockingQueue<String> commandQueue;

    private Vibrator vibrator;
    private static final int BUTTON_VIBRATE_DURATION = 50;
    private static final int TOGGLE_VIBRATE_DURATION = 25;

    private MainActivity thisActivity;

    private Button[] textButtons;
    private ImageButton[] imageButtons;

    private int[] stepSizes;
    private String[] stepSizeLabels;
    private int stepSizeIndex = 0;
    private int stepSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        thisActivity = this;

        initStepSizes();
        this.stepSize = this.stepSizes[this.stepSizeIndex];

        Button stepSizeButton = (Button) findViewById(R.id.stepSizeButton);
        stepSizeButton.setText(stepSizeLabels[stepSizeIndex]);
        stepSizeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(TOGGLE_VIBRATE_DURATION);
                cycleStepSize();
            }
        });


        Button connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connect();
            }
        });

        final ImageButton upButton = findViewById(R.id.upButton);
        upButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);
                commandQueue.offer("up");
            }
        });
        final ImageButton downButton = findViewById(R.id.downButton);
        downButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);
                commandQueue.offer("down");
            }
        });
        final ImageButton leftButton = findViewById(R.id.leftButton);
        leftButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);
                commandQueue.offer("left");
            }
        });
        final ImageButton rightButton = findViewById(R.id.rightButton);
        rightButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);
                commandQueue.offer("right");
            }
        });

        final ImageButton selectButton = findViewById(R.id.selectButton);
        selectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);
                commandQueue.offer("select");
            }
        });

        final Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);
                commandQueue.offer("back");
            }
        });

        final Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);

                AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
                builder.setCancelable(true);
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure you want to exit?");
                builder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            commandQueue.offer("q");
                            vibrate(BUTTON_VIBRATE_DURATION);
                        }
                    }
                );
                builder.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            vibrate(BUTTON_VIBRATE_DURATION);
                        }
                    }
                );

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        final ImageButton stepBackwardButton = findViewById(R.id.stepBackwardButton);
        stepBackwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);
                commandQueue.offer("sb:" + stepSize);
            }
        });

        final ImageButton stepForwardButton = findViewById(R.id.stepForwardButton);
        stepForwardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);
                commandQueue.offer("sf:" + stepSize);
            }
        });

        final ImageButton playPauseButton = findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);
                commandQueue.offer("p");
            }
        });

        final Button powerOffButton = findViewById(R.id.powerButton);
        powerOffButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vibrate(BUTTON_VIBRATE_DURATION);

                AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
                builder.setCancelable(true);
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure you want to power off?");
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                commandQueue.offer("power");
                                vibrate(BUTTON_VIBRATE_DURATION);
                            }
                        }
                );
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                vibrate(BUTTON_VIBRATE_DURATION);
                            }
                        }
                );

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        this.textButtons = new Button[]{backButton, exitButton, stepSizeButton, powerOffButton};

        this.imageButtons = new ImageButton[]{upButton, downButton, rightButton, leftButton, selectButton, stepBackwardButton, stepForwardButton, playPauseButton};

        disableButtons();
        enableConnectButton();
    }

    public void connect() {
        GetSubnetTask getSubnetTask = new GetSubnetTask();
        String subnet = null;
        try {
            subnet = getSubnetTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Got subnet: " + subnet);

        TestHostTask.cleanHostsList();

        AtomicInteger numTasks = new AtomicInteger(254);

        toast("Scanning subnet: " + subnet + ".*");

        int timeout = 100;
        for (int i = 1; i < 255; i++) {
            String host = subnet + "." + i;
            //System.out.println("Attempting to reach: " + host);
            TestHostTask testHostTask = new TestHostTask(thisActivity, numTasks, host);

            Thread taskThread = new Thread(testHostTask);
            taskThread.start();
        }
    }

    public void initStepSizes() {
        this.stepSizes = new int[]{5, 15, 30, 60, 180, 300, 600};
        this.stepSizeLabels = new String[this.stepSizes.length];

        for (int i = 0; i < stepSizes.length; i++) {
            int currStepSize = stepSizes[i];

            int minutes = currStepSize / 60;
            int seconds = currStepSize % 60;

            String label = "";
            if (minutes != 0) {
                label += minutes + "m";
            }
            if (seconds != 0) {
                label += seconds + "s";
            }

            stepSizeLabels[i] = label;
        }
    }

    public void cycleStepSize() {
        this.stepSizeIndex += 1;
        this.stepSizeIndex %= this.stepSizes.length;

        this.stepSize = this.stepSizes[this.stepSizeIndex];

        Button stepSizeButton = (Button) findViewById(R.id.stepSizeButton);
        stepSizeButton.setText(stepSizeLabels[stepSizeIndex]);
    }

    public void disableButtons() {
        thisActivity.runOnUiThread(new Runnable() {
            public void run() {
                for (ImageButton imageButton : imageButtons) {
                    imageButton.setClickable(false);
                    imageButton.setEnabled(false);
                    imageButton.setColorFilter(Color.argb(122, 255, 255, 255));
                    //navButton.setBackgroundColor(Color.argb(122, 0, 0, 0));
                }

                for (Button textButton : textButtons) {
                    textButton.setClickable(false);
                    textButton.setEnabled(false);
                }
            }
        });
    }

    public void enableButtons() {
        thisActivity.runOnUiThread(new Runnable() {
            public void run() {
                for (ImageButton imageButton : imageButtons) {
                    imageButton.setClickable(true);
                    imageButton.setEnabled(true);
                    imageButton.clearColorFilter();
                }

                for (Button textButton : textButtons) {
                    textButton.setClickable(true);
                    textButton.setEnabled(true);
                }
            }
        });
    }

    private void vibrate(int durationMillis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrator.vibrate(durationMillis);
        }
    }

    public void attemptConnection(List<String> availableHosts) {
        for (String host : availableHosts) {
            System.out.println("Attempting to connect to: " + host);
        }

        commandQueue = new LinkedBlockingQueue<String>();

        CommandSender commandSender = new CommandSender(this, commandQueue);

        boolean isConnected = commandSender.scanHostsAndConnect(availableHosts);

        if (isConnected) {
            toast("Connected to host: " + commandSender.getConnectedHost() + ":" + Integer.toString(CommandSender.PORT));

            Thread commandThread = new Thread(commandSender);
            commandThread.start();

            disableConnectButton();
            enableButtons();
        } else {
            toast("No connection made");
        }
        /*
        try {
            this.socket = new Socket("192.168.0.147", 65432);
            this.socketStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        */

        //socketStream.writeObject("exit");
    }

    public void toast(final String message) {
        thisActivity.runOnUiThread(new Runnable() {
            public void run() {
                Context context = getApplicationContext();
                CharSequence text = message;
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }

    public void snackbar(final String message, final String action) {
        final View parentLayout = findViewById(android.R.id.content);
        thisActivity.runOnUiThread(new Runnable() {
            public void run() {
                Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG)
                        .setAction(action, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("Reconnecting...");
                                connect();
                            }
                        })
                        .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                        .show();
            }
        });
    }

    public void enableConnectButton() {
        thisActivity.runOnUiThread(new Runnable() {
            public void run() {
                Button connectButton = findViewById(R.id.connectButton);
                connectButton.setClickable(true);
                connectButton.setEnabled(true);
                connectButton.setTextColor(Color.parseColor("#FFFFFF"));
                connectButton.setBackgroundColor(Color.parseColor("#03A9F4"));
            }
        });
    }

    public void disableConnectButton() {
        thisActivity.runOnUiThread(new Runnable() {
            public void run() {
                Button connectButton = findViewById(R.id.connectButton);
                connectButton.setClickable(false);
                connectButton.setEnabled(false);
                connectButton.setTextColor(Color.parseColor("#666666"));
                connectButton.setBackgroundColor(Color.parseColor("#555555"));
            }
        });
    }
}


