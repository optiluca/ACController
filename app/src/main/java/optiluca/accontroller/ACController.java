package optiluca.accontroller;


import android.hardware.ConsumerIrManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

public class ACController extends AppCompatActivity implements View.OnClickListener {

    private Button powerButtonClick;
    private Button offButtonClick;
    private Button quitButtonClick;

    private TextView spamACStatusText;
    private int spamCount = 0;

    private Intent notificationIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accontroller);
        powerButtonClick = (Button) findViewById(R.id.onButton);
        powerButtonClick.setOnClickListener(this);

        offButtonClick = (Button) findViewById(R.id.offButton);
        offButtonClick.setOnClickListener(this);

        quitButtonClick = (Button) findViewById(R.id.quitButton);
        quitButtonClick.setOnClickListener(this);

        spamACStatusText = (TextView) findViewById(R.id.spamAcStatus);

        notificationIntent = new Intent(this,ACControllerService.class);

        startService(notificationIntent);
    }

    @Override
    public void onClick(View v) {
        if (v == powerButtonClick) {
            spamCount += 1;
            spamACStatusText.setText("Spammed codes " + spamCount + " times");
            // mode, temp, fan
            ACControllerBackend.ACOn((ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE),21);
        }
        if (v == offButtonClick) {
            spamCount += 1;
            spamACStatusText.setText("Spammed codes " + spamCount + " times");
            ACControllerBackend.ACOff((ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE));
        }
        if (v == quitButtonClick) {
            stopService(notificationIntent);
        }

    }

}
