package com.example.dnk_1150070024_11_tmdt_baitaplythuyet7;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {
    private static final String CHANNEL_ID = "download_channel";
    private boolean isPaused = false;
    private boolean isCancelled = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String fileUrl = intent.getStringExtra("url");

        if (fileUrl == null || fileUrl.isEmpty()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        createChannel();

        // Hiển thị notification mặc định khi bắt đầu
        Notification startNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bắt đầu tải file...")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .build();
        startForeground(1, startNotification);

        new Thread(() -> downloadFile(fileUrl)).start();
        return START_NOT_STICKY;
    }

    private void downloadFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();

            int fileLength = con.getContentLength();
            if (fileLength <= 0) fileLength = 1; // tránh chia 0

            InputStream input = new BufferedInputStream(con.getInputStream());
            FileOutputStream output = new FileOutputStream(getFilesDir() + "/downloaded_file");

            byte[] data = new byte[1024];
            int count;
            long total = 0;

            while ((count = input.read(data)) != -1) {
                if (isCancelled) break;
                while (isPaused) Thread.sleep(200);

                total += count;
                int progress = (int) (total * 100 / fileLength);
                showProgressNotification(progress);
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            if (isCancelled) {
                showDoneNotification("❌ Đã hủy tải xuống");
                stopForeground(true);
                return;
            }

            showDoneNotification("✅ Hoàn tất tải xuống");
            stopForeground(true);

        } catch (Exception e) {
            Log.e("DownloadService", "Lỗi tải file: " + e.getMessage());
            showDoneNotification("⚠️ Lỗi khi tải file");
            stopForeground(true);
        }
    }

    private void showProgressNotification(int progress) {
        // Tạo PendingIntent cho các action
        PendingIntent pausePending = PendingIntent.getBroadcast(
                this, 0,
                new Intent(this, DownloadReceiver.class).setAction("ACTION_PAUSE"),
                PendingIntent.FLAG_IMMUTABLE
        );
        PendingIntent resumePending = PendingIntent.getBroadcast(
                this, 1,
                new Intent(this, DownloadReceiver.class).setAction("ACTION_RESUME"),
                PendingIntent.FLAG_IMMUTABLE
        );
        PendingIntent cancelPending = PendingIntent.getBroadcast(
                this, 2,
                new Intent(this, DownloadReceiver.class).setAction("ACTION_CANCEL"),
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Đang tải xuống...")
                .setContentText(progress + "%")
                .setProgress(100, progress, false)
                .addAction(android.R.drawable.ic_media_pause, "Pause", pausePending)
                .addAction(android.R.drawable.ic_media_play, "Resume", resumePending)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPending)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(1, builder.build());
    }

    private void showDoneNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(2, builder.build());
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID,
                            "Download Manager",
                            NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Kênh thông báo tải xuống");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void pauseDownload() { isPaused = true; }
    public void resumeDownload() { isPaused = false; }
    public void cancelDownload() { isCancelled = true; }
}
