package org.iviagteam.magparser.wrapper;

import java.util.ArrayList;

public class MaruVolumeWrapper extends VolumeWrapper{
	private final ArrayList<MaruVolumeUrlWrapper> list;
	
	public MaruVolumeWrapper(){
		super();
		list = new ArrayList<>();
	}
	
	@Override
	public void addVolume(String name, String url) {
		this.list.add(new MaruVolumeUrlWrapper(name, url));
	}
	
	@Override
	public void addVolume(String name, String url, String host){
		this.list.add(new MaruVolumeUrlWrapper(name, url, host));
	}
	
	@Override
	public ArrayList<MaruVolumeUrlWrapper> getVolumeList(){
		return this.list;
	}
}
