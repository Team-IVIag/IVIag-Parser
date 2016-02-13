package org.iviagteam.magparser.wrapper;

import java.util.ArrayList;

public abstract class VolumeWrapper {
	
	private final ArrayList<String> thumbList;
	private String title = "";
	
	public VolumeWrapper() {
		this.thumbList = new ArrayList<>();
	}
	
	public void addThumb(String url) {
		this.thumbList.add(url);
	}
	
	public abstract void addVolume(String name, String url);
	
	public ArrayList<String> getThumbs() {
		return this.thumbList;
	}
	
	public abstract ArrayList<? extends VolumeUrlWrapper> getVolumeList();
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public String getTitle(){
		return this.title;
	}
}
