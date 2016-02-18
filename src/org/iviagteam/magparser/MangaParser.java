package org.iviagteam.magparser;

import org.iviagteam.magparser.callback.MangaCallback;

public class MangaParser extends Thread{
	public enum Status{IDLE, CONNECTING, DETOUR, PARSING, DONE};
	
	public MangaParser(String url, MangaCallback callback) {
		
	}
}
