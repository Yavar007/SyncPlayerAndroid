package com.yavar007.syncplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Locale;


public class RoleActivity extends AppCompatActivity {
    private EditText etUsername;
    private SwitchCompat swTheme;
    private SwitchCompat swLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_role);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etUsername = findViewById(R.id.etUsername);
        Button btnClient = findViewById(R.id.btnClient);
        Button btnServer = findViewById(R.id.btnServer);
        TextView textView=findViewById(R.id.textView);
        swTheme = findViewById(R.id.sw_Theme);
        swLang = findViewById(R.id.sw_Lang);
        CheckThemeLang();
        if (Locale.getDefault().getLanguage().equals("fa")) {
            Typeface typeface = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                typeface = getResources().getFont(R.font.yekan);
            }
            etUsername.setTypeface(typeface);
            textView.setTypeface(typeface);
            btnClient.setTypeface(typeface);
            btnServer.setTypeface(typeface);
            swTheme.setTypeface(typeface);
            swLang.setTypeface(typeface);
        }
        btnClient.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            if (!username.isEmpty()) {
                Intent intent = new Intent(RoleActivity.this, MainActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("isServer", false);
                startActivity(intent);
            }
            else {
                Toast.makeText(this, R.string.errEnterUN, Toast.LENGTH_SHORT).show();
            }
        });
        btnServer.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            if (!username.isEmpty()) {
                Intent intent = new Intent(RoleActivity.this, MainActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("isServer", true);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.errEnterUN, Toast.LENGTH_SHORT).show();
            }
        });
        swTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                if (b) {
                    System.out.println(b);
                    swTheme.setText(R.string.swThemeL);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor.putString("apptheme", "light");
                    editor.apply();
                } else {
                    System.out.println(b);
                    swTheme.setText(R.string.swThemeD);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor.putString("apptheme", "dark");
                    editor.apply();
                }
            }
        });
        swLang.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                if (b) {
                    setLocale("fa");
                    editor.putString("My_Lang", "fa");
                    editor.apply();
                } else {
                    setLocale("en");
                    editor.putString("My_Lang", "en");
                    editor.apply();
                }
            }
        });
    }
    private void CheckThemeLang() {
        //getResources().getConfiguration().setLocale(new Locale("fa"));
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", null);
        String theme = prefs.getString("apptheme", null);
        //setTheme(theme);
        if (language!=null){
            if (language.equals("fa")) {
                swLang.setChecked(true);
                swLang.setText(R.string.swLangFa);
            } else if (language.equals("en")) {
                swLang.setChecked(false);
                swLang.setText(R.string.swLangEn);
            }
        }
        else{
            swLang.setChecked(false);
            swLang.setText(R.string.swLangEn);
        }
        if (theme != null){
            if (theme.equals("light")){
                swTheme.setText(R.string.swThemeL);
                swTheme.setChecked(true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            else if (theme.equals("dark")){
                swTheme.setText(R.string.swThemeD);
                swTheme.setChecked(false);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }else{
            swTheme.setText(R.string.swThemeL);
            swTheme.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        String defaultlang = Locale.getDefault().getLanguage();
        if (!defaultlang.equals(lang)){
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            recreate();
        }
        else{
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }
    public void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", null);
        if (language != null){
            setLocale(language);
        }
        else{
            setLocale("en");
        }
    }
}