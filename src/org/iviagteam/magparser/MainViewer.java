package org.iviagteam.magparser;

import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainViewer extends Application{

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("???");
	}
	
	
	public static void main(String args[]) throws IOException{
		System.out.println("IVIagParsing Test Progrem Launched");
		//TEST CODE
		ArrayList<String[]> testList = IVIagParser.ParseMagVolume("http://marumaru.in/b/manga/128400");
		
		for(String[] ele : testList) {
			System.out.println(ele[0] + " - " + ele[1]);
		}
	}
}
