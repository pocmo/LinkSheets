/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.androidzeitgeit.linksheets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SheetActivity extends Activity {
    private static final String TAG = "SheetActivity";

    private View containerView;
    private View sheetView;
    private WebView webView;
    private TextView titleView;
    private TextView urlView;

    private String lastUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sheet);

        containerView = findViewById(R.id.container);
        containerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeOut();
            }
        });

        sheetView = findViewById(R.id.sheet);
        titleView = (TextView) findViewById(R.id.title);
        urlView = (TextView) findViewById(R.id.url);

        final ProgressBar progressView = (ProgressBar) findViewById(R.id.progress);
        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                android.util.Log.d(TAG, "STARTED");

                lastUrl = url;

                urlView.setText(url);

                progressView.setProgress(1);
                progressView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                android.util.Log.d(TAG, "FINISHED");

                progressView.setVisibility(View.GONE);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                android.util.Log.d(TAG, "PROGRESS=" + newProgress);

                progressView.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                android.util.Log.d(TAG, "TITLE=" + title);

                titleView.setText(title);
            }
        });

        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just a hack for testing purposes
                if (!TextUtils.isEmpty(lastUrl)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(lastUrl));
                    intent.setPackage("org.mozilla.fennec");

                    fadeOut();
                    startActivity(Intent.createChooser(intent, "Open with"));
                }
            }
        });

        webView.setInitialScale(100);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; // Disable scrolling and interacting with the view in general
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(false);

        fadeIn();
    }

    private void fadeIn() {
        float height = getResources().getDimension(R.dimen.sheet_height);
        sheetView.setTranslationY(height);

        final Animator translateAnimator = ObjectAnimator.ofFloat(sheetView, "translationY", 0);
        translateAnimator.setDuration(200);

        containerView.setAlpha(0f);

        final Animator alphaAnimator = ObjectAnimator.ofFloat(containerView, "alpha", 1);
        alphaAnimator.setDuration(200);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(translateAnimator, alphaAnimator);
        set.setStartDelay(100);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                final Intent intent = getIntent();
                final Uri uri = intent.getData();

                if (uri != null) {
                    android.util.Log.d(TAG, "Opening " + uri.toString());

                    webView.loadUrl(uri.toString());
                }
            }
        });
        set.start();
    }

    private void fadeOut() {
        final Animator translateAnimator = ObjectAnimator.ofFloat(sheetView, "translationY", sheetView.getHeight());
        translateAnimator.setDuration(200);

        final Animator alphaAnimator = ObjectAnimator.ofFloat(containerView, "alpha", 0);
        alphaAnimator.setDuration(200);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(translateAnimator, alphaAnimator);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
            }
        });
        set.start();
    }

    @Override
    public void onBackPressed() {
        fadeOut();
    }

    @Override
    public void finish() {
        super.finish();

        // Don't perform an activity-dismiss animation.
        overridePendingTransition(0, 0);
    }
}
