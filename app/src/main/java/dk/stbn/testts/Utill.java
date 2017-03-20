package dk.stbn.testts;

import java.util.*;

public class Utill
{
	
	static ArrayList<String> debugbesked;
	
	static void p(Object o){
		System.out.println(o);
		debugbesked.add("" +(System.currentTimeMillis()-Appl.ms)+" "+  o);
	}
}
