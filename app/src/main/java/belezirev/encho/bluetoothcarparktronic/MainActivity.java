package belezirev.encho.bluetoothcarparktronic;

import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.function.Consumer;

public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothProxy proxy;
    private TextView textViewLeftMost;
    private TextView textViewLeft;
    private TextView textViewRight;
    private TextView textViewRightMost;

    private ImageView leftClose;
    private ImageView leftTooClose;
    private ImageView centerClose;
    private ImageView centerTooClose;
    private ImageView rightClose;
    private ImageView rightTooClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewLeftMost = (TextView) findViewById(R.id.leftMost);
        textViewLeft = (TextView) findViewById(R.id.left);
        textViewRight = (TextView) findViewById(R.id.right);
        textViewRightMost = (TextView) findViewById(R.id.rightMost);

        leftClose = (ImageView) findViewById(R.id.a1);
        leftTooClose = (ImageView) findViewById(R.id.a2);
        centerClose = (ImageView) findViewById(R.id.c1);
        centerTooClose = (ImageView) findViewById(R.id.c2);
        rightClose = (ImageView) findViewById(R.id.d1);
        rightTooClose = (ImageView) findViewById(R.id.d2);

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothValidator validator = new BluetoothValidator();
        BluetoothValidator.BluetoothValidationStatus status = validator.valudatedBluetooth(adapter);
        if (status == BluetoothValidator.BluetoothValidationStatus.NOT_SUPPORTED) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_LONG).show();
            return;
        }
        if (status == BluetoothValidator.BluetoothValidationStatus.NOT_ACTIVATED) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        DistanceReported leftDistanceReporter = new DistanceReported(textViewLeftMost, leftClose, leftTooClose);
        DistanceReported centerDistanceReporter = new DistanceReported(textViewLeft, centerClose, centerTooClose);
        DistanceReported rightDistanceReporter = new DistanceReported(textViewRightMost, rightClose, rightTooClose);

        Consumer<String> left = (message) -> {
            runOnUiThread(() -> leftDistanceReporter.reportDistanceToUi(message));
        };
        Consumer<String> center = (message) -> {
            runOnUiThread(() -> centerDistanceReporter.reportDistanceToUi(message));
        };
        Consumer<String> right = (message) -> {
            runOnUiThread(() -> rightDistanceReporter.reportDistanceToUi(message));
        };
        BluetoothMessageHandler messageHandler = new BluetoothMessageHandler(left, center, right);
        Handler bluetoothHandler = createHandler(messageHandler);
        proxy = new BluetoothProxy(adapter, bluetoothHandler);
        try {
            proxy.init();
            messageHandler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Handler createHandler(BluetoothMessageHandler messageHandler) {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String readMessage = (String) msg.obj;
                System.out.println(MessageFormat.format("Message read from the data.... {0}\n", readMessage));
                messageHandler.handleMessage(readMessage);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "On start method was entered.....", Toast.LENGTH_LONG);
        Toast.makeText(this, "On start method was entered.....", Toast.LENGTH_LONG);
        Toast.makeText(this, "On start method was entered.....", Toast.LENGTH_LONG).show();
    }

    private class DistanceReported {
        private TextView distanceTextView;
        private ImageView distanceViewClose;
        private ImageView distanceViewCloser;

        public DistanceReported(TextView distanceTextView, ImageView distanceViewClose, ImageView distanceViewCloser) {
            this.distanceTextView = distanceTextView;
            this.distanceViewClose = distanceViewClose;
            this.distanceViewCloser = distanceViewCloser;
        }

        public void reportDistanceToUi(String distanceMessage) {
            try {
                int distance = Integer.parseInt(distanceMessage);
                distanceTextView.setText(distanceMessage);
                int normalClose = ResourcesCompat.getColor(getResources(), R.color.normalClose, null);
                int tooClose = ResourcesCompat.getColor(getResources(), R.color.tooClose, null);
                int notInRange = ResourcesCompat.getColor(getResources(), R.color.notInRange, null);
                if (distance >= 140 && distance <= 50) {
                    setColor(normalClose, distanceViewClose);
                    setColor(notInRange, distanceViewCloser);
                } else if (distance > 50 && distance <= 19) {
                    setColor(normalClose, distanceViewClose);
                    setColor(tooClose, distanceViewCloser);
                } else {
                    setColor(notInRange, distanceViewClose);
                    setColor(notInRange, distanceViewCloser);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        private void setColor(int color, ImageView imageView) {
            imageView.setColorFilter(color);
        }
    }
}
