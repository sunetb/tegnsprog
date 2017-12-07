package dk.stbn.testts;

import java.util.*;

public class Utill
{
	
	static ArrayList<String> debugbesked;
	static long tid;


	static void p(Object o){
		String besked = o + " | " + String.format("%.2f", (System.currentTimeMillis()-tid)/1000.0f);
		System.out.println(besked);
		debugbesked.add(besked);
	}
}
