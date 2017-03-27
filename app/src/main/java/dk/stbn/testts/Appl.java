package dk.stbn.testts;
import android.app.*;
import android.net.Uri;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Appl extends Application
{
	/// Aide x
	/// As 
	
	public static Appl a;
	ArrayList<Indgang> søgeindeks = new ArrayList<>();
	String glosseurl = "http://tegnsprog.dk/indeks/aekvivalent_hel.js";
	static long ms;

	boolean dataKlar = false;

	Runnable main;
	
	
	
	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		a=this;
		ms = System.currentTimeMillis();
		Utill.debugbesked = new ArrayList<>();
		
		new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
				hentSøgeindeks(glosseurl);
				return null;
			}

			@Override
			protected void onPostExecute(Object resultat){
				//t("Data hentet");
				p("Data hentet");
				if (!(main == null)) main.run();
				//else !!!! todo
			}

		}.execute();
		
	}
	
	public void hentSøgeindeks(String u) {

		try { // Henter XML-fil
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
			InputStream is = new URL(u).openStream();
			is = new BufferedInputStream(is);
			is.mark(1);
			if (is.read() == 0xef) {
				is.read();
				is.read();

			} else {
				is.reset();
			}
			p("######## hentArtikel() =#=#=#=#=#=#=#=#=#=#=#=");
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
}
