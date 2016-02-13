package org.iviagteam.magparser.wrapper;

public class VolumeUrlWrapper {

	private final String name;
	private final String url;
	
	public VolumeUrlWrapper(String name, String url) {
		this.name = name;
		this.url = url;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getUrl() {
		return this.url;
	}
}
