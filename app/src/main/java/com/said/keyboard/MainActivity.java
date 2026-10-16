package com.said.keyboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEnableSys = findViewById(R.id.btn_enable_sys);
        btnEnableSys.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            startActivity(intent);
        });

        // Klavye Yükseklik Ayarları
        findViewById(R.id.btn_size_small).setOnClickListener(v -> saveSettings(230, null));
        findViewById(R.id.btn_size_normal).setOnClickListener(v -> saveSettings(270, null));
        findViewById(R.id.btn_size_large).setOnClickListener(v -> saveSettings(320, null));

        // Tema Ayarları
        findViewById(R.id.btn_theme_dark).setOnClickListener(v -> saveSettings(-1, "#1E1E1E"));
        findViewById(R.id.btn_theme_blue).setOnClickListener(v -> saveSettings(-1, "#1A2530"));
    }

    private void saveSettings(int height, String colorHex) {
        SharedPreferences prefs = getSharedPreferences("KeyboardPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (height != -1) {
            editor.putInt("keyboard_height", height);
            Toast.makeText(this, "Boyut Kaydedildi!", Toast.LENGTH_SHORT).show();
        }
        if (colorHex != null) {
            editor.putString("bg_color", colorHex);
            Toast.makeText(this, "Tema Kaydedildi!", Toast.LENGTH_SHORT).show();
        }
        editor.apply();
    }
}
