package com.ronak.viral.adda.providers.radio.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.ronak.viral.adda.util.Helper;
import com.ronak.viral.adda.util.Log;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * This class is used to get Album Art of a song based on search query. Uses the spotify API.
 */
public class AlbumArtGetter {

    public static String getImageForQuery(final String query, final AlbumCallback callback, final Context c){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... unused) {
                JSONObject o = Helper.getJSONObjectFromUrl("https://api.spotify.com/v1/search?q=" + URLEncoder.encode(query) + "&type=track&limit=1");

                try {
                    if (o != null
                            && o.has("tracks")
                            && o.getJSONObject("tracks").has("items")
                            && o.getJSONObject("tracks").getJSONArray("items").length() > 0){
                        JSONObject track = o.getJSONObject("tracks").getJSONArray("items").getJSONObject(0);
                        JSONObject image = track.getJSONObject("album").getJSONArray("images").getJSONObject(0);
                        return image.getString("url");
                    } else {
                        Log.v("INFO", "No items in Album Art Request");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(final String imageurl){
                if (imageurl != null)
                Picasso.with(c)
                    .load(imageurl)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                            callback.finished(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            callback.finished(null);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
                else
                    callback.finished(null);
            }
        }.execute();


        return null;
    }

    public interface AlbumCallback {
        void finished(Bitmap b);
    }
}
