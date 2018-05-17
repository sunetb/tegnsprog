package dk.stbn.testts;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.ArrayList;

/**
 * Created by sune on 3/20/17.
 */

class Fund {
    String nøgle;
    String index;
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
        StringBuilder resultatStreng = new StringBuilder();

        for (String s : ordliste)
            resultatStreng.append(s).append("\n");

        return resultatStreng.toString();
    }

    void initAfsp(Context c){
        p("initAfsp plev kaldt");
        afsp = ExoPlayerFactory.newSimpleInstance(c, new DefaultTrackSelector());//, new DefaultLoadControl());
        try {
            afsp.prepare(lavKilde(videourl));
        }catch (Exception e) {
            e.printStackTrace();
            p("Fejl ved afsp.prepare() "+e.getMessage());
        }
        afsp.addListener(new Player.EventListener() {


            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                //p("Fra onPlayerStateChanged: playback state: "+playbackState);
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

                p("Fejl fra onPlaybackError: "+ nøgle + " " +error);

                Appl.a.givBesked("Afspilningsfejl");
            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }



            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        afsp.setVideoDebugListener(new VideoRendererEventListener() {
            @Override
            public void onVideoEnabled(DecoderCounters counters) {

                p("onVideoEnabled");
            }

            @Override
            public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
                p("onVideoDecoderInitialized");
            }

            @Override
            public void onVideoInputFormatChanged(Format format) {

            }

            @Override
            public void onDroppedFrames(int count, long elapsedMs) {

                p("fejl onDroppedFrames "+nøgle + " "+ index);
            }

            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

            }

            @Override
            public void onRenderedFirstFrame(Surface surface) {

            }

            @Override
            public void onVideoDisabled(DecoderCounters counters) {

            }
        });
        p("VideoDecoderCounters: "+afsp.getVideoDecoderCounters());
        p("Videoformat: "+afsp.getVideoFormat());

        if (afsp == null) p("initAfsp: Fejl: afsp var allerede null");
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
        if (kilde == null) p("Fejl kilde i lavKilde() var null!");
        MediaSource ms = new ExtractorMediaSource(
                s,
                kilde,
                new DefaultExtractorsFactory(), null, null);
        if (ms == null) p("Fejl mediasource i lavKilde() var null!");
        return ms;
    }

    void p (Object o){
        Utill.p("Fund."+o);
    }
}
