package org.iviagteam.magparser;

import org.iviagteam.magparser.callback.VolumeCallback;

public class VolumeParser extends IVIagParser{
	public enum Status{IDLE, CONNECTING, TITLE_PARSING, IMG_PARSING, VOLUME_PARSING, DONE};
	
	public VolumeParser(String url, VolumeCallback callback) {
		
	}
}
