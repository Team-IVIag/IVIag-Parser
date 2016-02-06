package org.iviagteam.magparser;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class IVIagParser {
	
	public static final String TAG = "[IVIagParser]";
	
	public static enum SearchParse{TYPE1};
	public static enum VolumeParse{TYPE1};
	public static enum MagParse{TYPE1};

	private final static String userAgentToken = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36";
	private final static String referrerPage = "http://www.google.com";
	private final static String searchLinkSample = "http://marumaru.in/?r=home&mod=search&keyword={%k}&x=0&y=0";
	
	public final static String VOLUME_IMG_TAG = "{%img}";
	
	public static ArrayList<String[]> ParseMagSearch(String keyword) throws Exception {
		System.out.println(TAG + " ParseMagSearch - Search request: " + keyword);
		
		ArrayList<String[]> urlList = new ArrayList<>();
		String url = searchLinkSample.replace("{%k}", keyword);
		Document doc;
		SearchParse type;
		
		
		//Try Connect
		try {
			System.out.println(TAG + " try to connect '" + url + "'...");
			doc = Jsoup.connect(url)
					.userAgent(userAgentToken)
					.followRedirects(true)
					.referrer(referrerPage)
					.timeout(30000)
					.get();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(TAG + " Connection error");
			return null;
		}
		
		//Choose type
		Elements magList = doc.getElementsByClass("postbox").get(0)
					.getElementsByTag("a");
		if(!magList.isEmpty()) {
			type = SearchParse.TYPE1;
		}else {
			type = null;
		}
		
		//Read Elements
		switch(type) {
		
		case TYPE1:
			for(Element ele : magList) {
				try{
					Elements tmpList = ele.getElementsByTag("table").get(0)
							.getElementsByTag("tbody").get(0)
							.getElementsByTag("tr").get(0)
							.getElementsByTag("td");
					
					String magUrl = ele.attr("href");
					String title = tmpList.get(1).getElementsByClass("sbjbox").get(0).getElementsByTag("b").get(0).ownText();
					String thumb = tmpList.get(0).getElementsByClass("thumb").get(0).getElementsByTag("img").get(0).attr("src");
					
					urlList.add(new String[] {title, thumb, magUrl});
					System.out.println(TAG + " Parsing success: " + title);
				}catch(Exception e) {
					e.printStackTrace();
					System.out.println(TAG + " Fail to parseing: " + ele.toString());
				}
			}
			break;
		
		default:
			throw new Exception(TAG + " Unknown parsing type");
		}
		
		return urlList;
	}
	
	public static ArrayList<String[]> ParseMagVolume(String url) {
		System.out.println(TAG + " ParseMagVolume - Search volume request: " + url);
		
		if(url.startsWith("https")) url.replaceFirst("https", "http");
		
		ArrayList<String[]> list = new ArrayList<>();
		Document doc;
		VolumeParse type;
		
		//Try connect
		try {
			System.out.println(TAG + " try to connect '" + url + "'...");
			doc = Jsoup.connect(url)
					.userAgent(userAgentToken)
					.followRedirects(true)
					.referrer(referrerPage)
					.timeout(30000)
					.get();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(TAG + " Connection error");
			return null;
		}
		
		//Choose type
		Elements magList = doc.getElementsByClass("content").get(0)
				.getElementsByTag("div");
		type = VolumeParse.TYPE1;
		
		//Read Elements
		for(Element ele : magList) {
			try{
				if(ele.hasClass("tag")) {
					System.out.println(TAG + " Tag found. Search finished.");
					break;
				}
				
				String title;
				String link;
				Elements eles;
				
				if(!(eles = ele.getElementsByTag("span")).isEmpty()) {
					title = eles.get(0).getElementsByTag("a").get(0).ownText();
					link = eles.get(0).getElementsByTag("a").get(0).attr("href");
				}else if(!(eles = ele.getElementsByTag("img")).isEmpty()) {
					title = VOLUME_IMG_TAG;
					link = eles.get(0).attr("src");
				}else {
					continue;
				}
				
				//Check repetition
				int rpIndex = hasUrl(list, link);
				if(rpIndex >= 0) {
					System.out.println(TAG + " link is repetition (index:" + rpIndex + ") <" + link + "> will be remove");
					list.remove(rpIndex);
				}
				
				list.add(new String[] {title, link});
				System.out.println(TAG + " Parsing success: " + title);
			}catch(Exception e) {
				e.printStackTrace();
				System.out.println(TAG + " Fail to parseing: " + ele.toString());
			}
		}
		
		return list;
	}
	
	public static ArrayList<String> ParseMag(String url) {
		ArrayList<String> dataList = new ArrayList<>();
		
		return dataList;
	}
	
	private static int hasUrl(ArrayList<String[]> list, String url) {
		for(String[] eles : list) {
			for(String ele : eles) {
				if(ele == url) return list.indexOf(eles);
			}
		}
		return -1;
	}
}
