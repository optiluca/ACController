package optiluca.accontroller;

import android.hardware.ConsumerIrManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.*;

public class ACController extends AppCompatActivity implements View.OnClickListener {

    private Button spamACClick;
    private TextView spamACStatusText;
    private int spamCount = 0;

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
    private static final int DAIKIN_AIRCON2_MODE_HEAT = 0x40;
    private static final int DAIKIN_AIRCON2_MODE_COOL = 0x30;
    private static final int DAIKIN_AIRCON2_MODE_DRY = 0x20;
    private static final int DAIKIN_AIRCON2_MODE_FAN = 0x60;
    private static final int DAIKIN_AIRCON2_MODE_OFF = 0x00; // Power OFF
    private static final int DAIKIN_AIRCON2_MODE_ON = 0x01;
    private static final int DAIKIN_AIRCON2_FAN_AUTO = 0x0A; // Fan speed
    private static final int DAIKIN_AIRCON2_FAN1 = 0x03;
    private static final int DAIKIN_AIRCON2_FAN2 = 0x04;
    private static final int DAIKIN_AIRCON2_FAN3 = 0x05;
    private static final int DAIKIN_AIRCON2_FAN4 = 0x06;
    private static final int DAIKIN_AIRCON2_FAN5 = 0x07;

    private ConsumerIrManager ir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accontroller);
        spamACClick = (Button) findViewById(R.id.spamAcButton);
        spamACClick.setOnClickListener(this);

        spamACStatusText = (TextView) findViewById(R.id.spamAcStatus);

        ir = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
    }

    @Override
    public void onClick(View v) {
        if (v == spamACClick) {
            spamCount += 1;
            spamACStatusText.setText("Spammed codes " + spamCount + " times");
            sendDaikin();
        }

    }

    public void sendDaikin() {
        int[] daikinTemplate = {
            0x11, 0xDA, 0x27, 0xF0, 0x0D, 0x00, 0x0F, // First header
                    //  0     1     2     3     4     5     6
                    0x11, 0xDA, 0x27, 0x00, 0xD3, 0x11, 0x00, 0x00, 0x00, 0x1E, 0x0A, 0x08, 0x26 };
        //  7     8     9    10    11    12    13    14    15    16    17    18    19
        //ir.transmit(38400, IR_SIGNAL_TIME_LENGTH);

        // TODO: Input as parameters
        daikinTemplate[12] = DAIKIN_AIRCON2_MODE_OFF | DAIKIN_AIRCON2_MODE_AUTO; // mode

        int temperatureCmd = 18; // deg C
        daikinTemplate[16] = (temperatureCmd << 1) - 20;
        daikinTemplate[17] = DAIKIN_AIRCON2_FAN_AUTO; //fan speed

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
