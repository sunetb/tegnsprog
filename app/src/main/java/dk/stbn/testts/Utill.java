package dk.stbn.testts;

import java.util.*;

import dk.stbn.testts.lytter.Lytter;

public class Utill
{
	
	static ArrayList<String> debugbesked = new ArrayList();
	static long tid;
	static String changelog = "\n"+
            "Hvis parentes i glossen, bliver kun glossen med stort\n"+
            ""
			;


	static void p(Object o){
		String besked = o + " | " + String.format("%.2f", (System.currentTimeMillis()-tid)/1000.0f);
		System.out.println(besked);
		debugbesked.add(besked);
		if (Appl.a.lyttere != null && Appl.a.lyttere.size() > 1) for (Lytter l : Appl.a.lyttere) if (l != null) l.logOpdateret(); //Kun hvis debug-aktivitetetn er åben
	}
}
