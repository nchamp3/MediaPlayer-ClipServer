package com.example.clipserver;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class ClipServerService extends Service {

    private MyImpl impl = new MyImpl();
    private Notification notification;
    private int mStartID;
    private static final int NOTIFICATION_ID = 1;
    private static String CHANNEL_ID = "Music player style" ;
    int clip_length_played = -1;
    MediaPlayer mediaPlayer;
    int[] clips = {R.raw.t1, R.raw.t2, R.raw.t3, R.raw.t4, R.raw.t5};

    public ClipServerService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(getApplicationContext(), "Service Started", +  Toast.LENGTH_LONG).show();
        this.createNotificationChannel();


        mediaPlayer = MediaPlayer.create(this, R.raw.t1);

        if (null != mediaPlayer) {

            mediaPlayer.setLooping(false);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    Intent i = new Intent();
                    i.setAction("IS_SONG_FINISHED");
                    sendBroadcast(i);
                }
            });

        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Music player notification";
            String description = "The channel for music player notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaPlayer.stop();
        stopSelf(mStartID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mStartID = startId;

        final Intent notificationIntent = getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.example.audioclient");

        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0) ;

        notification =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setOngoing(true).setContentTitle("Music Playing")
                        .setContentText("Click to Access Music Player")
                        .setTicker("Music is playing!")
                        .setFullScreenIntent(pendingIntent, false)
                        .build();


        startForeground(NOTIFICATION_ID, notification);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return impl;
    }


    private class MyImpl extends ClipServerAIDL.Stub
    {
        @Override
        public void playClip(int clip_pos) throws RemoteException {

            if(clip_length_played == -1)
            {
                mediaPlayer.reset();
                mediaPlayer = MediaPlayer.create(getApplicationContext(), clips[clip_pos - 1]);
            }
            mediaPlayer.start();

        }

        @Override
        public void stopClip() throws RemoteException {

            mediaPlayer.reset();
            mediaPlayer.stop();
            clip_length_played = -1;

        }

        @Override
        public void pauseClip() throws RemoteException {

            mediaPlayer.pause();
            clip_length_played = mediaPlayer.getCurrentPosition();
        }


    }
}
