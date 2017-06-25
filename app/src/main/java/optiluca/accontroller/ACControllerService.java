package optiluca.accontroller;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.IBinder;
import android.util.Log;
import android.app.Notification;
import android.support.v4.app.NotificationCompat;

public class ACControllerService extends Service {

    private boolean isRunning = false;

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

                //Your logic that service will perform will be placed here
                //In this example we are just looping and waits for 1000 milliseconds in each loop.
                for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(1000);
                        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM,100);
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,200);

                    } catch (Exception e) {
                    }

                    if(isRunning){
                        Log.i("A", "Service running");
                    }
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
