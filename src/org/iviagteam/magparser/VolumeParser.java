package org.iviagteam.magparser;

import org.iviagteam.magparser.callback.VolumeCallback;

public abstract class VolumeParser extends Thread{
	public enum Status{IDLE, CONNECTING, TITLE_PARSING, IMG_PARSING, VOLUME_PARSING, DONE};
	
	public VolumeParser(String url, VolumeCallback callback) {
		
	}
	
	public abstract Status getStatus();
}
