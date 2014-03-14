package com.triplelift.exampleapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import android.widget.TextView;

import org.json.JSONException;

import com.triplelift.sponsoredimages.SponsoredImage;
import com.triplelift.sponsoredimages.SponsoredImageFactory;

import java.io.IOException;

public class MainActivity extends Activity {
    private static String DEBUG_TAG = "NativeAdActivity";
    private static String INV_CODE = "pinger_main_feed";
    
    private ImageView mImageView;
    private TextView mTextView;
    private SponsoredImage mSponsoredImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image);
        mImageView.setImageResource(R.drawable.ic_launcher);

        mTextView = (TextView) findViewById(R.id.text);
        this.populateSponsoredImage();
    }

    public void shareImage(View view) {
        if(mSponsoredImage != null) {
            mSponsoredImage.logShare();
        }
    }
    public void clickThroughLink(View view) {
        if(mSponsoredImage != null) {
            mSponsoredImage.logClickThrough();
            // do clickthrough logic here
        }
    }
    public void reloadAd(View view) {
        this.populateSponsoredImage();
    }

    private void populateSponsoredImage() {
        new SponsoredImageTask().execute(INV_CODE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class SponsoredImageTask extends AsyncTask<String, Void, SponsoredImage> {
        @Override
        protected SponsoredImage doInBackground(String... params) {
            Log.i(DEBUG_TAG, "creating the factory");
            SponsoredImageFactory sif = new SponsoredImageFactory(params[0]);
            try {
                Log.i(DEBUG_TAG, "trying to get the sponsored image...");
                mSponsoredImage = sif.getSponsoredImage();
                Log.i(DEBUG_TAG, mSponsoredImage.getImageUrl());
                return mSponsoredImage;
            } catch (NetworkOnMainThreadException e) {
                Log.e(DEBUG_TAG, "Network connection on main thread encountered");
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "IO Exception encountered");
            } catch (JSONException e) {
                Log.e(DEBUG_TAG, "JSON Exception encountered");
            }
            return null;
        }

        @Override
        protected void onPostExecute(SponsoredImage sponsoredImage) {
            if(isCancelled()) {
                sponsoredImage = null;
            }
            mTextView.setText(sponsoredImage.getCaption());
            new SetImageTask().execute(sponsoredImage);
            sponsoredImage.logImpression();
        }
    }

    private class SetImageTask extends AsyncTask<SponsoredImage, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(SponsoredImage... sponsoredImages) {
            try {
                SponsoredImage sponsoredImage = sponsoredImages[0];
                return sponsoredImage.getImage();
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "IO Exception encountered when trying to do sponsoredImage.getImage()");
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(isCancelled()) {
                bitmap = null;
            }
            mImageView.setImageBitmap(bitmap);
        }
    }
}
