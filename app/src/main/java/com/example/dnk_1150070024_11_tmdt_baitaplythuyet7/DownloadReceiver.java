package com.example.dnk_1150070024_11_tmdt_baitaplythuyet7;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case "ACTION_PAUSE":
                Toast.makeText(context, "⏸️ Tạm dừng tải xuống", Toast.LENGTH_SHORT).show();
                break;
            case "ACTION_RESUME":
                Toast.makeText(context, "▶️ Tiếp tục tải xuống", Toast.LENGTH_SHORT).show();
                break;
            case "ACTION_CANCEL":
                Toast.makeText(context, "❌ Hủy tải xuống", Toast.LENGTH_SHORT).show();
                context.stopService(new Intent(context, DownloadService.class));
                break;
        }
    }
}


