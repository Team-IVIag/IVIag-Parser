package org.iviagteam.magparser;

import org.iviagteam.magparser.callback.SearchCallback;

public class SearchParser extends Thread{
	public enum Status{IDLE, CONNECTING, PARSING, DONE};
	
	public SearchParser(String keyword, SearchCallback callback) {
		
	}
}
