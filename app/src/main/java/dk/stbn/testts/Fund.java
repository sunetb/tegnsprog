package dk.stbn.testts;

import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by sune on 3/20/17.
 */

class Fund {

    Uri videourl;
    Uri billedurl;

    ArrayList<String> ord;

    public Fund(Uri vurl, Uri burl, ArrayList <String> a){
        videourl=vurl;
        billedurl = burl;
        ord = a;
    }
}
