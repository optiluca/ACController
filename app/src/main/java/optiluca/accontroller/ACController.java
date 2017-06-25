package optiluca.accontroller;

import android.hardware.ConsumerIrManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import java.util.*;

public class ACController extends AppCompatActivity implements View.OnClickListener {

    private Button powerButtonClick;
    private Button offButtonClick;
    private Button quitButtonClick;

    private TextView spamACStatusText;
    private int spamCount = 0;

    private Intent notificationIntent;

    //private static final int[] IR_SIGNAL_TIME_LENGTH = {4499, 4499, 578, 1683, 578, 1683, 578, 1683, 578, 552, 578, 552, 578, 552, 578, 552, 578, 552, 578, 1683, 578, 1683, 578, 1683, 578, 552, 578, 552, 578, 552, 578, 552, 578, 552, 578, 552, 578, 552, 578, 1683, 578, 552, 578, 552, 578, 552, 578, 552, 578, 552, 578, 1683, 578, 1683, 578, 552, 578, 1683, 578, 1683, 578, 1683, 578, 1683, 578, 1683, 578, 23047};

    // Daikin timing constants
    private static final int DAIKIN_AIRCON2_HDR_MARK = 5050;
    private static final int DAIKIN_AIRCON2_HDR_SPACE = 2100;
    private static final int DAIKIN_AIRCON2_BIT_MARK = 391;
    private static final int DAIKIN_AIRCON2_ONE_SPACE = 1725;
    private static final int DAIKIN_AIRCON2_ZERO_SPACE = 667;
    private static final int DAIKIN_AIRCON2_MSG_SPACE = 30000;

    // Daikin codes
    private static final int DAIKIN_AIRCON2_MODE_AUTO = 0x10; // Operating mode
    //private static final int DAIKIN_AIRCON2_MODE_HEAT = 0x40;
    //private static final int DAIKIN_AIRCON2_MODE_COOL = 0x30;
    //private static final int DAIKIN_AIRCON2_MODE_DRY = 0x20;
    //private static final int DAIKIN_AIRCON2_MODE_FAN = 0x60;
    private static final int DAIKIN_AIRCON2_MODE_OFF = 0x00; // Power OFF
    private static final int DAIKIN_AIRCON2_MODE_ON = 0x01;
    private static final int DAIKIN_AIRCON2_FAN_AUTO = 0x0A; // Fan speed
    //private static final int DAIKIN_AIRCON2_FAN1 = 0x03;
    //private static final int DAIKIN_AIRCON2_FAN2 = 0x04;
    //private static final int DAIKIN_AIRCON2_FAN3 = 0x05;
    //private static final int DAIKIN_AIRCON2_FAN4 = 0x06;
    //private static final int DAIKIN_AIRCON2_FAN5 = 0x07;

    private ConsumerIrManager ir;

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

        ir = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);

        notificationIntent = new Intent(this,ACControllerService.class);

        startService(notificationIntent);
    }

    @Override
    public void onClick(View v) {
        if (v == powerButtonClick) {
            spamCount += 1;
            spamACStatusText.setText("Spammed codes " + spamCount + " times");
            // mode, temp, fan
            sendDaikin(DAIKIN_AIRCON2_MODE_ON | DAIKIN_AIRCON2_MODE_AUTO,21, DAIKIN_AIRCON2_FAN_AUTO);
        }
        if (v == offButtonClick) {
            spamCount += 1;
            spamACStatusText.setText("Spammed codes " + spamCount + " times");
            sendDaikin(DAIKIN_AIRCON2_MODE_OFF,21, DAIKIN_AIRCON2_FAN_AUTO);
        }
        if (v == quitButtonClick) {
            stopService(notificationIntent);
        }

    }

    public void sendDaikin(int mode, int temp, int fan) {
        int[] daikinTemplate = {
            0x11, 0xDA, 0x27, 0xF0, 0x0D, 0x00, 0x0F, // First header
                    //  0     1     2     3     4     5     6
                    0x11, 0xDA, 0x27, 0x00, 0xD3, 0x11, 0x00, 0x00, 0x00, 0x1E, 0x0A, 0x08, 0x26 };
        //  7     8     9    10    11    12    13    14    15    16    17    18    19

        daikinTemplate[12] = mode; // mode
        daikinTemplate[16] = (temp << 1) - 20; // temperature
        daikinTemplate[17] = fan; //fan speed

        // Calculate checksum
        int checksum = 0x00;

        for (int i=7; i<19; i++) {
            checksum += daikinTemplate[i];
        }

        daikinTemplate[19] = checksum;

        // Set carrier frequency
        int freq = 38000;

        // Header
        ArrayList<Integer> msg = new ArrayList<>();

        msg.add(DAIKIN_AIRCON2_HDR_MARK);
        msg.add(DAIKIN_AIRCON2_HDR_SPACE);

        // First header
        for (int i=0; i<7; i++) {
            ArrayList<Integer> tmpMsg = sendIRByte(daikinTemplate[i],DAIKIN_AIRCON2_BIT_MARK,DAIKIN_AIRCON2_ZERO_SPACE,DAIKIN_AIRCON2_ONE_SPACE);
            msg.addAll(tmpMsg);
        }

        // New header
        msg.add(DAIKIN_AIRCON2_BIT_MARK);
        msg.add(DAIKIN_AIRCON2_MSG_SPACE);
        msg.add(DAIKIN_AIRCON2_HDR_MARK);
        msg.add(DAIKIN_AIRCON2_HDR_SPACE);

        //
        // First header
        for (int i=7; i<20; i++) {
            ArrayList<Integer> tmpMsg = sendIRByte(daikinTemplate[i],DAIKIN_AIRCON2_BIT_MARK,DAIKIN_AIRCON2_ZERO_SPACE,DAIKIN_AIRCON2_ONE_SPACE);
            msg.addAll(tmpMsg);
        }

        msg.add(DAIKIN_AIRCON2_BIT_MARK);

        msg.add(1);

        ir.transmit(freq,toIntArray(msg));
    }

    int[] toIntArray(ArrayList<Integer> list){
        int[] ret = new int[list.size()];
        for(int i = 0;i < ret.length;i++)
            ret[i] = list.get(i);
        return ret;
    }

    public ArrayList<Integer> sendIRByte(int sendByte,int bitMarkLength, int zeroSpaceLength, int oneSpaceLength) {

        ArrayList<Integer> byteOut = new ArrayList<>();

        for (int j=0; j<8; j++) {
            if ((sendByte & 0x01) == 0x01) {
                byteOut.add(bitMarkLength);
                byteOut.add(oneSpaceLength);
            } else {
                byteOut.add(bitMarkLength);
                byteOut.add(zeroSpaceLength);
            }
            sendByte = sendByte >> 1;
        }

        return byteOut;

    }
}
