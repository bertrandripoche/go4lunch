package com.openclassrooms.go4lunch.controller.activity;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.openclassrooms.go4lunch.R;

import java.util.Objects;

public class RestaurantWebPageActivity extends BaseActivity {
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

        if (mRestaurantUrl != null) {
            displayWebView();
        }
    }

    /**
     * This method displays the webview with the restaurant webpage
     */
    private void displayWebView() {
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(mRestaurantUrl);
    }

    /**
     * This method configures the toolbar
     */
    private void configureToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        Objects.requireNonNull(ab).setDisplayHomeAsUpEnabled(true);
    }

    /**
     * This method allows to get the bundle passed from previous activity
     * @return the Bundle
     */
    private Bundle getBundle() {
        return this.getIntent().getExtras();
    }

    /**
     * This method allows to get the restaurant url from the previous activity
     * @return the url from the restaurant if any
     */
    private String getRestaurantUrlFromBundle() {
        if (mExtras.get(RESTAURANT_URL) != null) return mExtras.get(RESTAURANT_URL).toString();
        else return null;
    }
}
