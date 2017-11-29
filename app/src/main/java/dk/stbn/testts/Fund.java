package dk.stbn.testts;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.util.ArrayList;

/**
 * Created by sune on 3/20/17.
 */

class Fund {
    String nøgle;
    Uri videourl;
    Uri billedurl;
    ArrayList<String> ordliste;
    SimpleExoPlayer afsp;

    public Fund(Uri vurl, Uri burl, ArrayList<String> ord){ //til senere brug
        videourl=vurl;
        billedurl = burl;
        ordliste = ord;
    }

    public Fund(Uri vurl, ArrayList<String>  ord){
        videourl=vurl;
        ordliste = ord;

    }

    public String getTekst(){
        if (ordliste == null) return nøgle;
        String resultatStreng = "Søgeord:      \"" + nøgle + "\"\n\n";

        for (String s : ordliste)
            resultatStreng += s + "\n";

        return resultatStreng;
    }

    void initAfsp(Context c){
        afsp = ExoPlayerFactory.newSimpleInstance(c, new DefaultTrackSelector(), new DefaultLoadControl());
        afsp.prepare(lavKilde(videourl));
    }

    public String toString () {
        if (ordliste == null) return nøgle;
        return getTekst();
    }

    MediaSource lavKilde (Uri s){

        HttpDataSource.Factory kilde = new DefaultHttpDataSourceFactory("mig", new TransferListener<DataSource>() {
            @Override
            public void onTransferStart(DataSource source, DataSpec dataSpec) {
            }

            @Override
            public void onBytesTransferred(DataSource source, int bytesTransferred) {

            }
            @Override
            public void onTransferEnd(DataSource source) {

            }
        });

        MediaSource ms = new ExtractorMediaSource(
                s,
                kilde,
                new DefaultExtractorsFactory(), null, null);

        return ms;
    }


}
