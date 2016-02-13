package org.iviagteam.magparser;

import org.iviagteam.magparser.callback.MaruMangaCallback;
import org.iviagteam.magparser.wrapper.MaruMangaWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MaruMangaParser extends MangaParser {

	enum Status{IDLE, CONNECTING, DETOUR, PARSING, DONE};
	
	private Status status = Status.IDLE;
	private String url;
	private MaruMangaCallback callback;
	
	
	
	public MaruMangaParser(String url, MaruMangaCallback callback) {
		super(url, callback);
		this.url = url;
		this.callback = callback;
	}
	
	
	
	public Status getStatus() {
		return this.status;
	}
	
	
	
	@Override
	public void run() {
		parsing();
	}
	
	
	
	private void parsing() {
		System.out.println(TAG + " ParseMag - Parsing Manga request: " + this.url);
		
		parsing(url, false);
	}
	
	
	
	private void parsing(String url, Boolean repeat) {
		
		//init
		MaruMangaWrapper list;
		Document doc;
		
		if(url.startsWith("https")) url.replaceFirst("https", "http");
		
		//Try connect
		this.status = Status.CONNECTING;
		try {
			System.out.println(TAG + " Try to connect '" + url + "'...");

			doc = Jsoup.connect(url)
					.userAgent(USER_AGENT_TOKEN)
					.followRedirects(true)
					.referrer(REFERRER_PAGE)
					.timeout(30000)
					.cookie(IVIagParser.CLOUD_PROXY_COOKIE[0], IVIagParser.CLOUD_PROXY_COOKIE[1])
					.get();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(TAG + " Connection error");
			this.status = null;
			this.callback.callback(null);
			return;
		}
		
		//Check CloudProxy
		if(doc.title().equals("You are being redirected...")) {
			
			if(repeat) {
				System.out.println(TAG + " Fail detour CloudProxy.");
				this.status = null;
				this.callback.callback(null);
				return;
			}
			
			//Try detour
			this.status = Status.DETOUR;
			if(detourCloudProxy(doc)) {
				this.parsing(url, true);
			}else {
				this.status = null;
				this.callback.callback(null);
			}
			return;
		}
		
		//Title parsing
		this.status = Status.PARSING;
		Elements title = doc.select("#content .entry-title");
		try {
			String titleStr = getOwnText(title.get(0));
			list = new MaruMangaWrapper(titleStr);
			System.out.println(TAG + " Title parsing success: " + titleStr);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(TAG + " Fail parsing Manga Title");
			list = new MaruMangaWrapper("Undefined");
		}
		
		//Manga parsing
		Elements pages = doc.select("#content img[src~=(?i)\\.(png|jpe?g|gif|bmp)]");
		System.out.println(TAG + " Page find: " + pages.size());
		for(Element page : pages) {
			try {
				//LazyLoad Check
				String pageUrl = page.attr("data-lazy-src");
				if(pageUrl.equals("")) {
					pageUrl = page.attr("src");	
				}
				
				list.addPage(pageUrl);
				System.out.println(TAG + " Page parsing Success: " + pageUrl);
			}catch(Exception e) {
				e.printStackTrace();
				System.out.println(TAG + " Page parsing Fail: " + page.toString());
			}
		}
		
		this.status = Status.DONE;
		this.callback.callback(list);
	}
}
