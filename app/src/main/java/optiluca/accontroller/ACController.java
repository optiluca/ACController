package optiluca.accontroller;

import android.hardware.ConsumerIrManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ACController extends AppCompatActivity implements View.OnClickListener {

    private Button spamACClick;
    private TextView spamACStatusText;
    private int spamCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accontroller);
        spamACClick = (Button) findViewById(R.id.spamAcButton);
        spamACClick.setOnClickListener(this);

        spamACStatusText = (TextView) findViewById(R.id.spamAcStatus);
        //button.setOnClickListener(new View.OnClickListener() {
        //   public void onClick(View v) {
        //        ConsumerIrManager ir = (ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
        //    }
        //});
    }

    @Override
    public void onClick(View v)
    {
        if (v == spamACClick)
        {
            spamCount += 1;
            ConsumerIrManager ir = (ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
            spamACStatusText.setText("Spammed codes " + spamCount + " times");
        }

    }
}
