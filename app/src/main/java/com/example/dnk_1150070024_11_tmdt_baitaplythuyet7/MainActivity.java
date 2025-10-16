package com.example.dnk_1150070024_11_tmdt_baitaplythuyet7;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText edtUrl;
    private Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUrl = findViewById(R.id.edtUrl);
        btnDownload = findViewById(R.id.btnDownload);

        btnDownload.setOnClickListener(v -> {
            String url = edtUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập URL!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, DownloadService.class);
            intent.putExtra("url", url);
            startForegroundService(intent);
        });
    }
}
