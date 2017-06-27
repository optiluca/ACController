package optiluca.accontroller;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.IBinder;
import android.util.Log;
import android.app.Notification;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ACControllerService extends Service {

    private boolean isRunning = false;

    private static final int CONTROL_PORT = 11000;
    private static final int STATUS_PORT = 11001;

    public ACControllerService() {
    }

    @Override
    public void onCreate()  {
        Intent notificationIntent = new Intent(this, ACController.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("ACController")
                .setContentText("Ready")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {


        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR
        new Thread(new Runnable() {
            @Override
            public void run() {

                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM,100);

                //Your logic that service will perform will be placed here
                //In this example we are just looping and waits for 1000 milliseconds in each loop.
                /*for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(1000);

                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,200);

                    } catch (Exception e) {
                    }

                    if(isRunning){
                        Log.i("A", "Service running");
                    }
                }*/

                try {
                    DatagramSocket dSocket = new DatagramSocket(CONTROL_PORT);
                    byte[] buffer = new byte[1];
                    DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

                    while(!dSocket.isClosed()) {
                        dSocket.receive(packet);

                        Log.i("PACKET", new String(packet.getData()));

                        switch(packet.getData()[0]) {
                            case 0x01:
                                ACControllerBackend.ACOn((ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE),21);
                                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,200);
                                break;
                            case 0x02:
                                ACControllerBackend.ACOff((ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE));
                                toneG.startTone(ToneGenerator.TONE_CDMA_ANSWER,200);
                                break;
                        }
                        if (!isRunning) return;
                    }
                } catch(IOException e){
                    e.printStackTrace();
                }


            }
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        Log.i("A", "Service onDestroy");
    }
}
