package org.iviagteam.magparser.wrapper;

import java.util.ArrayList;

public class MangaWrapper {
	public final String name;
	public final ArrayList<String> pages;
	
	public MangaWrapper(String name) {
		this.name = name;
		this.pages = new ArrayList<>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public ArrayList<String> getPages() {
		return this.pages;
	}
	
	public void addPage(String url) {
		this.pages.add(url);
	}	
}
