package dk.stbn.testts;

import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by sune on 3/20/17.
 */

class Fund {
    String nøgle;
    Uri videourl;
    Uri billedurl;
    ArrayList<String> ordliste;

    public Fund(Uri vurl, Uri burl, ArrayList<String> ord){
        videourl=vurl;
        billedurl = burl;
        ordliste = ord;
}
    public Fund(Uri vurl, ArrayList<String>  ord){
        videourl=vurl;
        ordliste = ord;
    }

    public String getTekst(){
        String resultatStreng = "";//"Søgeord:      \"" + nøgle + "\"\n\n";

        for (String s : ordliste)
            resultatStreng += s + "\n";

        return resultatStreng;
    }

    public String toString () {
        if (ordliste == null) return nøgle;
        return getTekst();
    }
}
