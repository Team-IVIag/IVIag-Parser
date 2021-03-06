package org.iviagteam.magparser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.iviagteam.magparser.callback.*;
import org.iviagteam.magparser.exception.FailDetourException;
import org.iviagteam.magparser.wrapper.*;

public class Example {
	
	//Run this class for TEST
	public static void main(String args[]) throws IOException, InterruptedException{
		System.out.println("IVIagParsing Test Progrem Launched");
		
		//Manga search test
		MaruSearchParser searchParser = new MaruSearchParser("던전", new MaruSearchCallback() {
			
			@Override
			public void callback(ArrayList<MaruSearchWrapper> list, Exception whenError) {
				if(whenError != null) {
					System.out.println("Search test: FAIL CONNECT >> " + whenError.toString());
					return;
				}
				printMaruSearch(list);
			}
		});
		searchParser.start();
		
		System.out.println("========== Search test REQUESTED");
		while(searchParser.getStatus() != null && searchParser.getStatus() != MaruSearchParser.Status.DONE) {
			Thread.sleep(100); //Async wait...
		}
		System.out.println("========== Search test FINISH");
		
		//Manga volume parse test
		MaruVolumeParser volumeParser = new MaruVolumeParser("http://marumaru.in/b/manga/64026", new MaruVolumeCallback() {

			@Override
			public void callback(MaruVolumeWrapper result, Exception whenError) {
				if(whenError != null) {
					if(whenError instanceof IllegalArgumentException) {
						System.out.println("Manga volume test: WRONG URL >> " + whenError.toString());
					}else {
						System.out.println("Manga volume test: FAIL CONNECT >> " + whenError.toString());
					}
					return;
				}
				printMaruVolume(result);
			}
		});
		volumeParser.start();
		
		System.out.println("========== Manga volume parsing test REQUESTED");
		while(volumeParser.getStatus() != null && volumeParser.getStatus() != MaruVolumeParser.Status.DONE) {
			Thread.sleep(100); //Async wait...
		}
		System.out.println("========== Manga volume parsing test FINISH");
		
		//Manga parse test
		MaruMangaParser mangaParser = new MaruMangaParser("http://www.shencomics.com/archives/571592", new MaruMangaCallback() {
			
			@Override
			public void callback(MaruMangaWrapper result, Exception whenError) {
				if(whenError != null) {
					if(whenError instanceof FailDetourException) {
						System.out.println("Manga parsing test: FAIL TO DETOUR CLOUDPROXY >> " + whenError.toString());
					}else if(whenError instanceof MalformedURLException) {
						System.out.println("Manga parsing test: WRONG URL >> " + whenError.toString());
					}else {
						System.out.println("Manga parsing test: FAIL CONNECT >> " + whenError.toString());
					}
					return;
				}
				printMaruManga(result);
				
			}
		}, new String[] {"false"});
		mangaParser.start();
		
		System.out.println("========== Manga parsing test REQUESTED");
		while(mangaParser.getStatus() != null && mangaParser.getStatus() != MaruMangaParser.Status.DONE) {
			Thread.sleep(100); //Async wait...
		}
		System.out.println("========== Manga parsing test FINISH");
		
		System.out.println("========== Cookie cache test START");
		HashMap<String, String> cookieMap = IVIagParser.getCookies(); //cookie get
		String cookieStr = IVIagParser.cookieStringify(cookieMap); //cookie stringify (when save cookie cache)
		System.out.println("Cookie cache: " + cookieStr); // print
		try {
			cookieMap = IVIagParser.cookieParsing(cookieStr); //cookie parsing (when load cookie cache)
		} catch (Exception e) {} //When this is not instance of cookie string
		try {
			IVIagParser.setCookies(cookieMap); //cookie set
		} catch (Exception e) {} //When this is not instance of cookie
		System.out.println("========== Cookie cache test FINISH");
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
