package dk.stbn.testts;
import android.app.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Appl extends Application
{

	
	public static Appl a;
	ArrayList<Indgang> søgeindeks = new ArrayList<>();
	String glosseurl = "http://tegnsprog.dk/indeks/glosse.js";
	static long ms;
	
	
	
	
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
				sendOgLæs(glosseurl);
				return null;
			}

			@Override
			protected void onPostExecute(Object resultat){
				t("Data hentet");
				p("Data hentet");
			}

		}.execute();
		
	}
	
	public void sendOgLæs(String u) {

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
			p("########=#=#=#=#=#=#=#=#=#=#=#=");
			byte[] contents = new byte[1024];
			String heleIndholdet = "";
			int bytesRead = 0;
			//bytesRead = is.read(contents); //skipper første linie
			//bytesRead = is.read(contents); //skipper anden linie

			while((bytesRead = is.read(contents)) != -1) {
				String linie = new String(contents, 0, bytesRead);
				heleIndholdet += linie;
				//p("\nLinie_______________________________: "+linie);


			}
			
			
			p("Efter while: " + (System.currentTimeMillis() - ms));
			String [] temp = heleIndholdet.split(",");
			boolean begynd = false;
			for (String s : temp) {
				//s = s.replaceAll("\"", "");

				int ixStreg = s.indexOf("|");
				//p("IND: " + s);
				String søgeord ="";
				if (ixStreg > 0) {
					søgeord = s.substring(2,ixStreg);
					//p("Søgeord: "+søgeord);
					if (søgeord.equals("--") ) begynd = true;
					//else søgeord = s;
					s = s.substring(ixStreg+1,s.length());

					ArrayList<String> index = new ArrayList<>();

					//String [] udarray = s.split("|");
					String indeksnummer ="";

					for (int i = 0; i < s.length(); i++) {
						String tegn = s.substring(i,i+1);
						if ((tegn != null) && begynd) {
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
					//p("UD:  " + i.toString());
				}
			}
			p("Efter for ydre " + (System.currentTimeMillis() - ms));
			
		} catch (Exception ex) {
			ex.printStackTrace();
			p(ex);
			p(ex.getMessage());
		}


	} // end sendOgLæs
	void t (String s){

		Toast.makeText(this, s, Toast.LENGTH_LONG).show();
	}
	
	void p (Object o){
		Utill.p("Appl."+o);
	}
}
