package org.iviagteam.magparser;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.iviagteam.magparser.callback.MaruMangaCallback;
import org.iviagteam.magparser.exception.FailDetourException;
import org.iviagteam.magparser.wrapper.MaruMangaWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class MaruMangaParser extends IVIagParser {

	enum Status{IDLE, CONNECTING, PARSING, DONE, ERROR};
	
	private Status status = Status.IDLE;
	private String url;
	private MaruMangaCallback callback;
	
	
	
	public MaruMangaParser(String url, MaruMangaCallback callback) {
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
		
		parsing2(url);
	}
	
	
	
	private void parsing2(String url) {

		if(url.startsWith("https")) url.replaceFirst("https", "http");

		try {
			this.status = Status.CONNECTING;
			IVIagParser.log(TAG, "Imitating Chrome...");
			final WebClient webClient = new WebClient(BrowserVersion.CHROME);

			this.status = Status.PARSING;
			IVIagParser.log(TAG, "Connecting...: " + url);
			final HtmlPage htmlPage = webClient.getPage(url);

			String title;
			try {
				title = ((HtmlElement) htmlPage.getElementById("content")).getElementsByAttribute("h1", "class", "entry-title").get(0).getTextContent();
			}catch(Exception e) {
				title = htmlPage.getTitleText().substring(0, htmlPage.getTitleText().indexOf("|")-1);
			}
			IVIagParser.log(TAG, "Parsing...: " + title);
			MaruMangaWrapper wrapper = new MaruMangaWrapper(title);
			DomNodeList<HtmlElement> list = htmlPage.getElementById("content").getElementsByTagName("img");
			for(HtmlElement element : list) {
				IVIagParser.log(TAG, "Parsed page: " + element.getAttribute("src"));
				wrapper.addPage(element.getAttribute("src"));
			}
			webClient.close();
			this.status = Status.DONE;
			this.callback.callback(wrapper, null);
		} catch (Exception e) {
			this.status = Status.ERROR;
			this.callback.callback(null, e);
		}

	}

	/*
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
			if(detourCloudProxy(doc)) {
				this.parsing(url, true);
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
		this.callback.callback(list, null);
	}
	*/
}
