package com.beloushkin.learning.android.superbrowser;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.mySwipeRefreshLayout)
    SwipeRefreshLayout mSwipeLayout;
    @BindView(R.id.myProgressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.myImageView)
    ImageView mImageView;
    @BindView(R.id.myWebView)
    WebView mWebView;
    @BindView(R.id.myLinearLayout)
    LinearLayout mLinearLayout;

    String currentUrl;


    private Toast mToast;

    private void showMessage(String msgText) {
        if (mToast != null)
            mToast.cancel();

        mToast = Toast.makeText(this,msgText,Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mProgressBar.setMax(100);

        mWebView.loadUrl("https://www.google.com");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mLinearLayout.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mLinearLayout.setVisibility(View.GONE);
                mSwipeLayout.setRefreshing(false);
                super.onPageFinished(view, url);
                currentUrl = url;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                getSupportActionBar().setTitle(title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                mImageView.setImageBitmap(icon);
            }
        });

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request mRequest = new DownloadManager.Request(Uri.parse(url));
                mRequest.allowScanningByMediaScanner();
                mRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                DownloadManager mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                mDownloadManager.enqueue(mRequest);
                showMessage("Downloading the file...");
            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.super_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_back:
                onBackPressed();
                break;
            case R.id.menu_forward:
                onForwardMenuPressed();
                break;
            case R.id.menu_refresh:
                mWebView.reload();
                break;
            case R.id.menu_share:
                shareUrl(currentUrl);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onForwardMenuPressed() {
        if (mWebView.canGoForward()) {
            mWebView.goForward();
        } else {
            showMessage("Can't go further!");
        }

    }

    private void shareUrl(String url) {
        if (url.isEmpty()) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentUrl);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Copied URL...");
        startActivity(Intent.createChooser(shareIntent, "Share url with somebody..."));
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }
}
