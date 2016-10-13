/* FetchProfilePictureTask.java
 * 2016 - Etienne Boespflug
 * This file is dedicated to the public domain and is free to use (https://creativecommons.org/publicdomain/zero/1.0/).
 */

package fr.etienne_boespflug.samples.facebook_api_sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The FetchProfilePictureTask class represents an {@link AsyncTask} used to
 * download a Facebook profile picture from Facebook and to display this
 * image in the specified ImageView.
 *
 * @author Etienne Boespflug
 */
public class FetchProfilePictureTask extends AsyncTask<Uri, Void, Bitmap> {
    /** The View in which the downloaded image will be shown. */
    private ImageView imageView;

    /**
     * Constructs a new FetchProfilePictureTask with the specified ImageView.
     *
     * @param imageView the ImageView in which the downloaded image will be
     * shown. This parameter cannot be {@code null}.
     */
    public FetchProfilePictureTask(ImageView imageView) {
        this.imageView = imageView;
    }

    /**
     * Launch the download of the image at the specified URL.
     * <p>
     * If the download succeed, call the method {@link #onPostExecute(Bitmap)}
     * with the downloaded bitmap.
     *
     * @param urls an array of uri containing the URL of the image to download.
     * Only the first uri will be download.
     * @return
     */
    protected Bitmap doInBackground(Uri... urls) {
        Bitmap img = null;
        try {
            InputStream in = new URL(urls[0].toString()).openStream();
            img = BitmapFactory.decodeStream(in);
        } catch (IOException e) { Log.e("fetch_profile_pic", Log.getStackTraceString(e)); }

        return img;
    }

    /**
     * Set the content of the ImageView to the
     * downloaded Bitmap.
     * <p>
     * The image isn't erased if the download fail
     * (i.e. if  {@code result} is {@code null}).
     *
     * @param result the downloaded image.
     */
    protected void onPostExecute(Bitmap result) {
        if(result != null) imageView.setImageBitmap(result);
    }
}