package org.iviagteam.magparser;

import org.iviagteam.magparser.callback.MaruMangaCallback;
import org.iviagteam.magparser.exception.FailDetourException;
import org.iviagteam.magparser.wrapper.MaruMangaWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MaruMangaParser extends MangaParser {

	public static final String TAG = "MaruMangaParser";
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
					.userAgent(IVIagParser.USER_AGENT_TOKEN)
					.followRedirects(true)
					.referrer(IVIagParser.REFERRER_PAGE)
					.timeout(IVIagParser.TIME_OUT)
					.cookies(IVIagParser.getCookies())
					.get();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(TAG + " Connection error");
			this.status = null;
			this.callback.callback(null, e);
			return;
		}
		
		//Check CloudProxy
		if(doc.title().equals("You are being redirected...")) {
			
			if(repeat) {
				System.out.println(TAG + " Fail detour CloudProxy.");
				this.status = null;
				this.callback.callback(null, new FailDetourException("Unknown cookie type"));
				return;
			}
			
			//Try detour
			this.status = Status.DETOUR;
			if(IVIagParser.detourCloudProxy(doc)) {
				this.parsing(doc.location(), true);
			}else {
				this.status = null;
				this.callback.callback(null, new FailDetourException("Fail to detour CloudProxy"));
			}
			return;
		}
		
		//Title parsing
		this.status = Status.PARSING;
		Elements title = doc.select("#content .entry-title");
		try {
			String titleStr = IVIagParser.getOwnText(title.get(0));
			list = new MaruMangaWrapper(titleStr);
			System.out.println(TAG + " Title parsing success: " + titleStr);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(TAG + " Fail parsing Manga Title");
			list = new MaruMangaWrapper("Undefined");
		}
		
		//Manga parsing
		Elements pages = doc.select("#content img[ks-token], #content img[data-src], #content img[data-lazy-src], #content img[src~=(?i)\\.(png|jpe?g|gif|bmp)]");
		System.out.println(TAG + " Page find: " + pages.size());
		for(Element page : pages) {
			try {
				//LazyLoad Check
				String pageUrl = "";
				if(page.hasAttr("ks-token")) pageUrl = page.attr("ks-token");
				else if(page.hasAttr("data-lazy-src")) pageUrl = page.attr("data-lazy-src");
				else if(page.hasAttr("data-src")) pageUrl = page.attr("data-src");
				else pageUrl = page.attr("src");
				
				list.addPage(pageUrl);
				System.out.println(TAG + " Page parsing Success: " + pageUrl);
			}catch(Exception e) {
				e.printStackTrace();
				System.out.println(TAG + " Page parsing Fail: " + page.toString());
			}
		}
		
		this.status = Status.DONE;
		this.callback.callback(list, null);
	}
}
