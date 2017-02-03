package org.iviagteam.magparser;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
//import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.iviagteam.magparser.callback.MaruMangaCallback;
import org.iviagteam.magparser.exception.FailDetourException;
import org.iviagteam.magparser.logger.DefaultLogger;
import org.iviagteam.magparser.wrapper.MaruMangaWrapper;
import org.jsoup.Connection.Response;
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
	
	
	private void parsing2(String url, String passcode) {

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
			DomNodeList<DomNode> list = htmlPage.querySelectorAll("gallery-template img");
			for(DomNode elem : list) {
				String pageUrl = "";
				HtmlElement element = (HtmlElement) elem;
				
				if(element.hasAttribute("ks-token")) pageUrl = element.getAttribute("ks-token");
				else if(element.hasAttribute("data-lazy-src")) pageUrl = element.getAttribute("data-lazy-src");
				else if(element.hasAttribute("data-src")) pageUrl = element.getAttribute("data-src");
				else pageUrl = element.getAttribute("src");
				
				DefaultLogger.getInstance().debug("Parsed page: " + pageUrl, TAG);
				wrapper.addPage(pageUrl);
			}
			webClient.close();
			this.status = Status.DONE;
			this.callback.callback(wrapper, null);
		} catch (Exception e) {
			this.status = null;
			this.callback.callback(null, e);
		}

	}
	
	public void parsing2(String url){
		parsing2(url, IVIagParser.passcode);
	}

	public void parsingLegacy(String url, boolean repeat, String passcode) {
		
		//init
		MaruMangaWrapper list;
		Document doc;
		
		if(url.startsWith("https")) url.replaceFirst("https", "http");
		
		//Try connect
		this.status = Status.CONNECTING;
		try {
			System.out.println(TAG + " Try to connect '" + url + "'...");

			Response resp = Jsoup.connect(url)
					.userAgent(IVIagParser.USER_AGENT_TOKEN)
					.followRedirects(true)
					.referrer(IVIagParser.REFERRER_PAGE)
					.timeout(IVIagParser.TIME_OUT)
					.cookies(IVIagParser.getCookies())
					.data("pass", passcode)
					.execute();
			url = resp.url().toString();
			doc = resp.parse();
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
		Elements title = doc.select(".article-title");
		try {
			//String titleStr = IVIagParser.getOwnText(title.get(0));
			String titleStr = title.attr("title");
			list = new MaruMangaWrapper(titleStr);
			System.out.println(TAG + " Title parsing success: " + titleStr);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(TAG + " Fail parsing Manga Title");
			list = new MaruMangaWrapper("Undefined");
		}
		
		//Manga parsing
		Elements pages = doc.select(".gallery-template img[ks-token], .gallery-template img[data-src], .gallery-template img[data-lazy-src], .gallery-template img[src~=(?i)\\.(png|jpe?g|gif|bmp)]");
		System.out.println(TAG + " Page find: " + pages.size());
		URI a;
		try {
			a = new URI(url);
		}catch(Exception e){
			e.printStackTrace();
			this.callback.callback(null, e);
			return;
		}
		
		for(Element page : pages) {
			try {
				//LazyLoad Check
				String pageUrl = "";
				if(page.hasAttr("ks-token")) pageUrl = page.attr("ks-token");
				else if(page.hasAttr("data-lazy-src")) pageUrl = page.attr("data-lazy-src");
				else if(page.hasAttr("data-src")) pageUrl = page.attr("data-src");
				else pageUrl = page.attr("src");
				
				String pageUri = null;
				
				try{
					pageUri = a.resolve(pageUrl).toString();
				}catch(Exception e){
					String[] split = pageUrl.split("/");
					split[split.length - 1] = URLEncoder.encode(split[split.length - 1], "UTF-8");
					pageUrl = Arrays.stream(split).collect(Collectors.joining("/"));
					pageUri = a.resolve(pageUrl).toString().replace("+", "%20");
				}
				
				list.addPage(pageUri.toString());
				System.out.println(TAG + " Page parsing Success: " + pageUrl);
			}catch(Exception e) {
				e.printStackTrace();
				System.out.println(TAG + " Page parsing Fail: " + page.toString());
			}
		}
		
		this.status = Status.DONE;
		this.callback.callback(list, null);
	}
	
	public void parsingLegacy(String url, boolean repeat){
		parsingLegacy(url, repeat, IVIagParser.passcode);
	}

	
}
