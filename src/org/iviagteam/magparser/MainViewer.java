package org.iviagteam.magparser;

import java.io.IOException;
import java.util.ArrayList;

public class MainViewer {
	
	public static void main(String args[]) throws IOException{
		System.out.println("IVIagParsing Test Progrem Launched");
		//TEST CODE
		ArrayList<String[]> testList = IVIagParser.ParseMag("http://www.shencomics.com/archives/12826");
		
		for(String[] ele : testList) {
			System.out.println(ele[0] + " - " + ele[1]);
		}
	}
}
