package dk.stbn.testts;
import android.app.*;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.widget.*;

import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer2.PlaybackParameters;

import io.fabric.sdk.android.Fabric;
import java.io.*;
import java.net.*;
import java.util.*;

public class Appl extends Application
{
	/// Aide
	/// As x

	//-- Tilstand
	boolean dataKlar = false;
	boolean visPil = true;
	boolean loop = true;
	boolean slowmotion = false;
	String aktueltSøgeord = "";
	long position;

	//-- System
	public static Appl a;
	static long ms;
	Runnable main;


	//-- Data
	SharedPreferences sp;
	ArrayList<Indgang> søgeindeks = new ArrayList<>();
	String glosseurl = "http://tegnsprog.dk/indeks/aekvivalent_hel.js";
	String nyUrl = "http://tegnsprog.dk/m/app-indeks/app-indeks.csv";
	ArrayList<String> tilAutoComplete = new ArrayList();
	ArrayList<Fund> søgeresultat = new ArrayList();


	//int antalSøgninger = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		a=this;
		ms = System.currentTimeMillis();
		Utill.debugbesked = new ArrayList<>();
		sp= PreferenceManager.getDefaultSharedPreferences(this);
		loop = sp.getBoolean("loop", true);

		new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
				hentSøgeindeks2(glosseurl);
				return null;
			}

			@Override
			protected void onPostExecute(Object resultat){
				p("Data hentet");
				if (!(main == null)) {
					main.run();
				}
				else {
					p("FEJL Main fandtes ikke da den skulle opdateres");
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {

							if (!(main == null)) {
								main.run();
								dataKlar = true;
							}
							else t("FEJL Main fandtes ikke da den skulle opdateres II");

						}
						}, 1150);
				}


			}

		}.execute();
		
	}

	public void hentSøgeindeks2(String u) {

		try { // Henter fil
			InputStream is = new URL(nyUrl).openStream();
			is = new BufferedInputStream(is);
			is.mark(1);
			if (is.read() == 0xef) {
				is.read();
				is.read();

			} else {
				is.reset();
			}
			p("######## hentSøgeindeks() =#=#=#=#=#=#=#=#=#=#=#=");
			byte[] contents = new byte[1024];
			String heleIndholdet = "";
			int bytesRead = 0;

			//bytesRead = is.read(contents); //skipper første linie
			//bytesRead = is.read(contents); //skipper anden linie

			//--Først hentes al tekst ind i én stor streng

			while((bytesRead = is.read(contents)) != -1) {


				String linie =  new String(contents, 0, bytesRead, "iso-8859-1");
				//char [] chars = linie.toCharArray();
				/*for (int i = 0; i < chars.length; i++){
					char c = chars[i];
					p(linie.substring(i,i+1)+ " : "+ Character.getNumericValue(c)+ " : " +contents[i]);
				}*/

				//String linie =  new String(contents, "UTF-8");
				heleIndholdet += linie;
				//p("\nLinie_______________________________: "+linie);


			}

			String [] linjesplit = heleIndholdet.split("\n");
			//p("Array længde: "+linjesplit.length);
			for (int i = 0; i < linjesplit.length; i++){
				String indgangS = linjesplit[i];
				//p("Ind__"+indgangS);
				String [] indgangSA = indgangS.split("\t");
				String søgeordet = indgangSA[0];
				String [] ix = indgangSA[1].split(";");
				//p("Array længde: "+ix.length);
				ArrayList<String> ix2 = new ArrayList();
				String temp= "";
				for (int j = 0; j < ix.length; j++) {
					String gammel = "";					//tjek for dubletter
					if (j>0) gammel = ix[j-1].trim();

					String s = ix[j].trim();

					if(!gammel.equalsIgnoreCase(s)) ix2.add(s); // add kun hvis den ikke findes i forvejen
				}
				//p("Ud1___"+søgeordet+ " "+ix2);
				Indgang indgang = new Indgang(søgeordet.trim(), ix2);
				søgeindeks.add(indgang);
				tilAutoComplete.add(søgeordet);
				//p("Ud2___"+indgang);

			}


		} catch (Exception ex) {
			ex.printStackTrace();
			p(ex);
			p(ex.getMessage());
		}


	}
	
	public void hentSøgeindeks(String u) {

		try { // Henter fil
			InputStream is = new URL(u).openStream();
			is = new BufferedInputStream(is);
			is.mark(1);
			if (is.read() == 0xef) {
				is.read();
				is.read();

			} else {
				is.reset();
			}
			p("######## hentSøgeindeks() =#=#=#=#=#=#=#=#=#=#=#=");
			byte[] contents = new byte[1024];
			String heleIndholdet = "";
			int bytesRead = 0;
			//bytesRead = is.read(contents); //skipper første linie
			//bytesRead = is.read(contents); //skipper anden linie

			//--Først hentes al tekst ind i én stor streng

			while((bytesRead = is.read(contents)) != -1) {
				String linie = new String(contents, 0, bytesRead);
				heleIndholdet += linie;
				//p("\nLinie_______________________________: "+linie);


			}
			
			//-- Så konverteres strengen (oprindeligt et javascript-array med elementer af formen: "kaffe|386|1093")
			//p("Efter while: " + (System.currentTimeMillis() - ms));
			String [] temp = heleIndholdet.split(",");
			boolean begynd = false;
			for (String s : temp) {
				//s = s.replaceAll("\"", "");

				//-- Først skal vi finde søgeordet:
				int ixStreg = s.indexOf("|");
				//p("IND: " + s);
				String søgeord ="";
				if (ixStreg > 0) {
					søgeord = s.substring(2,ixStreg);
					//p("Søgeord: "+søgeord);

					s = s.substring(ixStreg+1,s.length());

					//--- Så skal vi finde indeksnumrene
					ArrayList<String> index = new ArrayList<>();

					//String [] udarray = s.split("|"); //-- Virker af en eller anden grund ikke. Heller ikke med escape\
					String indeksnummer ="";

					for (int i = 0; i < s.length(); i++) {
						String tegn = s.substring(i,i+1);
						if ((tegn != null)) {
							//p(str+ " _ "+ str.codePointAt(0));
							if (tegn.codePointAt(0) == 124 || tegn.codePointAt(0) == 34){ // | eller "
								index.add(indeksnummer);
								indeksnummer = "";
								if (tegn.codePointAt(0) == 34) break;
							}

							else indeksnummer += tegn;

						}
					}
					//p("Efter for indre: " + (System.currentTimeMillis() - ms));
					
					Indgang indgang = new Indgang(søgeord, index);
					a.søgeindeks.add(indgang);
					//p("UD:  " + indgang.toString());
				}
			}
			//p("Efter for ydre " + (System.currentTimeMillis() - ms));

			Indgang første = søgeindeks.get(0);
			p("________tjekker første indgang "+første);
			første.søgeord = første.søgeord.substring(34);
			p(første);

			//tjekIndgang("i tirsdags");
			//tjekIndgang("kaffe");

			//--Tjek Indgang i søgenindeks
//p("--------------------tjekker---søgeindeks---------------------");
			//for (Indgang i : søgeindeks) p(i);

		} catch (Exception ex) {
			ex.printStackTrace();
			p(ex);
			p(ex.getMessage());
		}


	}
	
	void tjekIndgang (String søgeord){
		for (int i = 0; i<søgeindeks.size(); i++) {
			Indgang indg = søgeindeks.get(i);
			p("TjekIndgang søgte efter: "+ søgeord);
			if (indg.søgeord.equalsIgnoreCase(søgeord)){
				 p(" og fandt: "+i);
				p("Pos "+i);
				break;
			}
		}

	}

	public Fund hentArtikel(String u) {
		p("hentArtikel("+u+")");
		String vUrl = "";
		ArrayList <String>  beskrivelser = new ArrayList<>();


		try {
			InputStream is = new URL("http://m.tegnsprog.dk/artikler/"+u+".html").openStream();
			is = new BufferedInputStream(is);
			is.mark(1);
			if (is.read() == 0xef) {
				is.read();
				is.read();

			} else {
				is.reset();
			}
			p("#=#=#=#=#=#=#=#= hentArtikel() =#=#=#=#=#=#=#=#=#=#=#");
			byte[] contents = new byte[1024];
			String heleIndholdet = "";
			int bytesRead = 0;
			//bytesRead = is.read(contents); //skipper første linie
			//bytesRead = is.read(contents); //skipper anden linie

			while((bytesRead = is.read(contents)) != -1) {
				String linie = new String(contents, 0, bytesRead);
				heleIndholdet += linie;
				


			}
			
			//p("\nArtikel_______________________________: "+heleIndholdet);



			//-- Vi finder video-urlen

			int startindeks = heleIndholdet.indexOf("src=\"");
			String tempUrl = heleIndholdet.substring(startindeks+5);
			int slutIndeks= tempUrl.indexOf("\" type='video/mp4'>Your browser does not support the video tag.");
			vUrl = tempUrl.substring(0,slutIndeks);
			p("videourl::::::::::::::::::::::::::::::::"+vUrl);
			///////////////Eksperiment med webm
			//int underscore = vUrl.lastIndexOf("_")+1;
			///int sidstepunktum = vUrl.lastIndexOf(".");

			//String indeksnr = vUrl.substring(underscore,sidstepunktum);
			//p("indeksnr: "+indeksnr);
			//vUrl = "http://m.tegnsprog.dk/video/mobil/t-webm/t_"+indeksnr+".webm";
			//p(vUrl);




			//-- Vi finder de danske ord

			startindeks = tempUrl.indexOf("<TABLE>");
			slutIndeks = tempUrl.indexOf("<TABLE width=\"100%\">");
			String tabelDKOrd = tempUrl.substring(startindeks, slutIndeks);
			//p("__________min html:");
			//p(tabelDKOrd);
			//p("----------");
			//dKOrd = tabelDKOrd.replaceAll("\\/TR><TR", "</TR><BR><TR>").replaceAll("\\&middot\\;", " , ");
			String dKOrd = tabelDKOrd
					.replaceAll("<TABLE>", "")
					.replaceAll("</TD><TD>", " ")
					.replaceAll("\\&middot\\;", " , ")
					.replaceAll("<TABLE/>", "")
					.replaceAll("</TABLE>", "")
					.replaceAll("&nbsp;", "")
					.replaceAll("<TR>", "")
					.replaceAll("<TR style=\"vertical-align:top;\">", "")
					.replaceAll("</TR>", "")
					.replaceAll("</TD>", "ønskernylinie")
					.replaceAll("<TD>", "")
					.replaceAll("<BR>","\n")
				.replaceAll("<BR/>","\n");

			//Erstat html-koder for danske vokaler
			//æ: &aelig;
			//ø: &oslash;
			//å: &aring;

			dKOrd = dKOrd
					.replaceAll("&aelig;", "æ")
					.replaceAll("&oslash;", "ø")
					.replaceAll("&aring;", "å");





			beskrivelser = new ArrayList<String>(Arrays.asList(dKOrd.split("ønskernylinie")));


			p("Efter rens");
			p(beskrivelser.toString());



/*
			<HTML>
				<video width="100%" autoplay>
			 		<source  src="http://www.tegnsprog.dk/video/t/t_317.mp4" type='video/mp4'>
			 		Your browser does not support the video tag.
			 	</video>
				<TABLE>
					<TR style="vertical-align:top;">
						<TD>1. </TD>
						<TD>brun</TD>
					</TR>
					<TR style="vertical-align:top;">
						<TD>2. </TD>
						<TD>kaffe</TD>
					</TR>
				</TABLE>
				<TABLE width="100%">
					<TR>
						<TD>
							<A TITLE="&Aring;bn den fulde ordbogsartikel i browseren" href="#" onclick="var ref = window.open('http://www.tegnsprog.dk/#|soeg|tegn|386', '_system');">
							<IMG SRC="http://www.tegnsprog.dk/billeder/web/logo-mini.png"/>
								Vis p&aring; tegnsprog.dk</A></TD><TD style="text-align: right;">
							<A TITLE="G&aring; til toppen" href="#0">
								&#8679;
							</A>
						</TD></TR></TABLE></HTML>


						ANDEN VERSION______________________________

				<TABLE>
					<TR>
						<TD>
							&nbsp;
						</TD>
						<TD>
							foran &middot; forrest &middot; f&oslash;re &middot; komme f&oslash;rst
						</TD>
					</TR>
				</TABLE>
				<TABLE width="100%"><TR><TD><A TITLE="&Aring;bn den fulde ordbogsartikel i browseren" href="#" onclick="var ref = window.open('http://www.tegnsprog.dk/#|soeg|tegn|1000', '_system');"><IMG SRC="http://www.tegnsprog.dk/billeder/web/logo-mini.png"/>Vis p&aring; tegnsprog.dk</A></TD><TD style="text-align: right;"><A TITLE="G&aring; til toppen" href="#0">&#8679;</A></TD></TR></TABLE></HTML>

*/
		} catch (Exception ex) {
			ex.printStackTrace();
			p(ex);
			p(ex.getMessage());
		}

		return new Fund(Uri.parse(vUrl), beskrivelser);
	}
	
	
	
	void t (String s){

		Toast.makeText(this, s, Toast.LENGTH_LONG).show();
	}
	
	void p (Object o){
		Utill.p("Appl."+o);
	}

    public void releaseAlle() {
		for (Fund f : søgeresultat) f.afsp.release();
    }

	public void opdaterHastighed() {
		float hast =  (a.slowmotion) ? 0.25f : 1.0f;
		for (Fund f : søgeresultat) f.afsp.setPlaybackParameters(new PlaybackParameters(hast, 1));
	}

	public void opdaterLoop() {
		for (Fund f : søgeresultat) f.afsp.setPlayWhenReady(true);
	}
}
