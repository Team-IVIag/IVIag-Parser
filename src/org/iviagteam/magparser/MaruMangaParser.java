package org.iviagteam.magparser;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.iviagteam.magparser.callback.MaruMangaCallback;
import org.iviagteam.magparser.exception.FailDetourException;
import org.iviagteam.magparser.logger.DefaultLogger;
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
	private boolean useLegacy;
	
	
	
	public MaruMangaParser(String url, MaruMangaCallback callback, String[] args) {
		super(url, callback, args);
		this.url = url;
		this.callback = callback;
		this.useLegacy = Boolean.parseBoolean(args[0]);
	}
	
	
	
	public Status getStatus() {
		return this.status;
	}
	
	
	
	@Override
	public void run() {
		if(!useLegacy) parsing();
		else parsingLegacy(url, false);
	}
	
	
	
	private void parsing() {
		System.out.println(TAG + " ParseMag - Parsing Manga request: " + this.url);
		
		parsing2(url);
	}
	
	
	private void parsing2(String url) {

		if(url.startsWith("https")) url.replaceFirst("https", "http");

		try {
			this.status = Status.CONNECTING;
			DefaultLogger.getInstance().info("Imitating Chrome", TAG);
			final WebClient webClient = new WebClient(BrowserVersion.CHROME);
			webClient.getOptions().setActiveXNative(false);
			webClient.getOptions().setAppletEnabled(false);
			webClient.getOptions().setCssEnabled(false);

			this.status = Status.PARSING;
			DefaultLogger.getInstance().info("Connecting...: " + url, TAG);
			final HtmlPage htmlPage = webClient.getPage(url);

			String title;
			try {
				title = ((HtmlElement) htmlPage.getElementById("content")).getElementsByAttribute("h1", "class", "entry-title").get(0).getTextContent();
			}catch(Exception e) {
				title = htmlPage.getTitleText().substring(0, htmlPage.getTitleText().indexOf("|")-1);
			}
			DefaultLogger.getInstance().info("Parsing...: " + title, TAG);
			MaruMangaWrapper wrapper = new MaruMangaWrapper(title);
			DomNodeList<HtmlElement> list = htmlPage.getElementById("content").getElementsByTagName("img");
			for(HtmlElement element : list) {
				DefaultLogger.getInstance().debug("Parsed page: " + element.getAttribute("src"), TAG);
				wrapper.addPage(element.getAttribute("src"));
			}
			webClient.close();
			this.status = Status.DONE;
			this.callback.callback(wrapper, null);
		} catch (Exception e) {
			this.status = null;
			this.callback.callback(null, e);
		}

	}

	public void parsingLegacy(String url, Boolean repeat) {
		
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
				this.parsingLegacy(doc.location(), true);
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
