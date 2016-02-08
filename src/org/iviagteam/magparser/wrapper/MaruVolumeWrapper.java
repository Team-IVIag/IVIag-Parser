package org.iviagteam.magparser.wrapper;

import java.util.ArrayList;

public class MaruVolumeWrapper {
	
	private final ArrayList<String> thumbList;
	private final ArrayList<MaruVolumeUrlWrapper> list;
	
	public MaruVolumeWrapper() {
		this.thumbList = new ArrayList<>();
		this.list = new ArrayList<>();
	}
	
	public void addThumb(String url) {
		this.thumbList.add(url);
	}
	
	public void addVolume(String name, String url) {
		this.list.add(new MaruVolumeUrlWrapper(name, url));
	}
	
	public ArrayList<String> getThumbs() {
		return this.thumbList;
	}
	
	public ArrayList<MaruVolumeUrlWrapper> getVolumeList() {
		return this.list;
	}
}
