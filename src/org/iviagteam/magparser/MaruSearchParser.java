package org.iviagteam.magparser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.iviagteam.magparser.callback.MaruSearchCallback;
import org.iviagteam.magparser.wrapper.MaruSearchWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MaruSearchParser extends SearchParser{
	
	public static final String TAG = "MaruSearchParser";
	private Status status = Status.IDLE;
	private String key;
	private MaruSearchCallback callback;

	public static final String SEARCH_LINK_SAMPLE = "http://marumaru.in/?r=home&mod=search&keyword={%k}&x=0&y=0";
	public static final String MANGA_PREFIX_URL = "http://marumaru.in";
	
	
	
	public MaruSearchParser(String keyword, MaruSearchCallback callback) {
		super(keyword, callback);
		this.key = keyword;
		this.callback = callback;
	}
	
	
	
	public Status getStatus() {
		return this.status;
	}
	
	
	
	@Override
	public void run() {
		this.status = Status.CONNECTING;
		System.out.println(TAG + " ParseMagSearch - Search request: " + this.key);
		
		ArrayList<MaruSearchWrapper> urlList = new ArrayList<>();
		String url;
		try {
			url = SEARCH_LINK_SAMPLE.replace("{%k}", URLEncoder.encode(this.key, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return;
		}
		Document doc;
		
		//Try Connect
		try {
			System.out.println(TAG + " try to connect '" + url + "'...");
			doc = Jsoup.connect(url)
					.userAgent(IVIagParser.USER_AGENT_TOKEN)
					.followRedirects(true)
					.referrer(IVIagParser.REFERRER_PAGE)
					.timeout(IVIagParser.TIME_OUT)
					.cookies(IVIagParser.getCookies())
					.get();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(TAG + " Connection error");
			this.status = null;
			this.callback.callback(null, e);
			return;
		}
		
		this.status = Status.PARSING;
		Elements magList = doc.select(".postbox a");
		
		for(Element ele : magList) {
			try{
				Elements tmpList = ele.select("table tbody tr td");
			
				String magUrl = ele.attr("href");
				String title = tmpList.get(1).select(".sbjbox b").get(0).ownText();
				String thumb = tmpList.get(0).select(".thumb img").get(0).attr("src");
				
				urlList.add(new MaruSearchWrapper(title, thumb, MaruSearchParser.MANGA_PREFIX_URL + magUrl));
				System.out.println(TAG + " Parsing success: " + title);
			}catch(Exception e) {
				e.printStackTrace();
				System.out.println(TAG + " Failed parsing: " + ele.toString());
			}
		}
		this.status = Status.DONE;
		this.callback.callback(urlList, null);
	}
}