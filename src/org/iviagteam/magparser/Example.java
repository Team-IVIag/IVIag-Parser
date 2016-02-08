package org.iviagteam.magparser;

import java.io.IOException;
import java.util.ArrayList;

import org.iviagteam.magparser.callback.*;
import org.iviagteam.magparser.wrapper.*;

public class Example {
	
	//Run this class for TEST
	public static void main(String args[]) throws IOException, InterruptedException{
		System.out.println("IVIagParsing Test Progrem Launched");
		
		//Manga search test
		new MaruSearchParser("던전", new MaruSearchCallback() {
			
			@Override
			public void callback(ArrayList<MaruSearchWrapper> list) {
				// TODO Auto-generated method stub
				printMaruSearch(list);
				System.out.println("========== Search test FINISH");
			}
		}).start();
		
		System.out.println("========== Search test REQUESTED");
		Thread.sleep(5000);
		
		//Manga volume parse test
		new MaruVolumeParser("http://marumaru.in/b/manga/64026", new MaruVolumeCallback() {

			@Override
			public void callback(MaruVolumeWrapper result) {
				// TODO Auto-generated method stub
				printMaruVolume(result);
				System.out.println("========== Manga volume parsing test FINISH");
			}
		}).start();
		
		System.out.println("========== Manga volume parsing test REQUESTED");
		Thread.sleep(5000);
		
		//Manga parse test
		new MaruMangaParser("http://www.shencomics.com/archives/571592", new MaruMangaCallback() {
			
			@Override
			public void callback(MaruMangaWrapper result) {
				// TODO Auto-generated method stub
				printMaruManga(result);
				System.out.println("========== Manga parsing test FINISH");
			}
		}).start();
		
		System.out.println("========== Manga parsing test REQUESTED");
	}
	
	//Example
	public static void printMaruSearch(ArrayList<MaruSearchWrapper> list) {
		for(MaruSearchWrapper wrapper : list) {
			System.out.println(wrapper.getName() + " ["  + wrapper.getThumbUrl() + "] - " + wrapper.getMangaUrl());
		}
	}
	
	public static void printMaruVolume(MaruVolumeWrapper wrapper) {
		for(String thumbUrl : wrapper.getThumbs()) {
			System.out.println("Thumb: " + thumbUrl);
		}
		
		for(MaruVolumeUrlWrapper wrapper2 : wrapper.getVolumeList()) {
			System.out.println(wrapper2.getName() + " - " + wrapper2.getUrl());
		}
	}
	
	public static void printMaruManga(MaruMangaWrapper wrapper) {
		System.out.println("Title: " + wrapper.getName());
		for(String url : wrapper.getPages()) {
			System.out.println("Page: " + url);
		}
	}
}
