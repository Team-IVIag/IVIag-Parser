package org.iviagteam.magparser.wrapper;

import java.net.MalformedURLException;
import java.net.URL;

public class VolumeUrlWrapper {

	private final String name;
	private final String url;
	private String host;
	
	public VolumeUrlWrapper(String name, String url) {
		this.name = name;
		this.url = url;
		try {
			URL mUrl = new URL(url);
			this.host = mUrl.getHost();
		} catch (MalformedURLException e) {
			this.host = "undefined";
		}
	}
	
	public VolumeUrlWrapper(String name, String url, String host){
		this.name = name;
		this.url = url;
		this.host = host;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getHost() {
		return this.host;
	}
}
