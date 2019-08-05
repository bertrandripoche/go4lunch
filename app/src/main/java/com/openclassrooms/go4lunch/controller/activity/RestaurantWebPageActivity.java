package com.openclassrooms.go4lunch.controller.activity;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.openclassrooms.go4lunch.R;

import java.util.Objects;

public class RestaurantWebPageActivity extends AppCompatActivity {
    final String RESTAURANT_URL = "restaurantUrl";
    private WebView webView;
    private Bundle mExtras;
    private String mRestaurantUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_web_page);

        this.configureToolbar();

        mExtras = getBundle();
        mRestaurantUrl = getRestaurantUrlFromBundle();
        System.out.println("Adresse web "+mRestaurantUrl);

        if (mRestaurantUrl != null) {
            webView = (WebView) findViewById(R.id.webView);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(mRestaurantUrl);
        }
    }

    private void configureToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        Objects.requireNonNull(ab).setDisplayHomeAsUpEnabled(true);
    }

    private Bundle getBundle() {
        return this.getIntent().getExtras();
    }

    private String getRestaurantUrlFromBundle() {
        if (mExtras.get(RESTAURANT_URL) != null) return mExtras.get(RESTAURANT_URL).toString();
        else return null;
    }
}
