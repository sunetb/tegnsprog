package dk.stbn.testts;

import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by sune on 3/20/17.
 */

class Fund {
    String nøgle;
    Uri videourl;
    //Uri billedurl;
    ArrayList<String>  ordliste;

    //public Fund(Uri vurl, Uri burl, String ord){
    public Fund(Uri vurl, ArrayList<String>  ord){
        videourl=vurl;
        //billedurl = burl;
        ordliste = ord;
    }

    public String toString () {return "Fund: "+videourl +"\n"+ ordliste.toString();}
}
