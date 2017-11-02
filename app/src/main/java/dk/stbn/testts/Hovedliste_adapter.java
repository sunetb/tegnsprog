package dk.stbn.testts;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.util.ArrayList;

/**
 * Created by sune on 11/2/17.
 */

public class Hovedliste_adapter extends RecyclerView.Adapter<Hovedliste_adapter.ViewHolder> {

    ArrayList<Fund> data;
    Context c;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView c;
        com.google.android.exoplayer2.ui.SimpleExoPlayerView playerv;
        CheckBox loop, hast;
        TextView  fundtekst;
        ImageView pil;

        public ViewHolder(View v) {
            super(v);
            c = (CardView) v.findViewById(R.id.kort);
            playerv = (SimpleExoPlayerView) v.findViewById(R.id.afspillerview);
            loop = (CheckBox) v.findViewById(R.id.loopcb);
            hast = (CheckBox) v.findViewById(R.id.langsomcb);
            fundtekst = (TextView) v.findViewById(R.id.fundtekst);
            pil = (ImageView) v.findViewById(R.id.mere);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public Hovedliste_adapter (ArrayList søgeresultater, Context ctx) {
        data = søgeresultater;
        c = ctx;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public Hovedliste_adapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        LinearLayout rod = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.kort, parent, false);

        ViewHolder vh = new ViewHolder(rod);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {

        Fund f = data.get(pos);


        holder.playerv.setPlayer(ExoPlayerFactory.newSimpleInstance(c, new DefaultTrackSelector(), new DefaultLoadControl()));

/*

        holder.playerv
        holder.loop
        holder.hast
        holder.fundtekst
        holder.pil.
*/

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }
}


