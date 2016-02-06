package org.iviagteam.magparser;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class IVIagParser {

	private final static String userAgentToken = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36";
	private final static String referrerPage = "http://www.google.com";
	private final static String searchLinkSample = "http://marumaru.in/?r=home&mod=search&keyword={%k}&x=0&y=0";
	
	public static ArrayList<String[]> ParseMagSearch(String keyword) {
		System.out.println("ParseMagSearch - search request: " + keyword);
		
		ArrayList<String[]> urlList = new ArrayList<>();
		String url = searchLinkSample.replace("{%k}", keyword);
		Document doc;
		
		try {
			System.out.println("try to connect '" + url + "'...");
			doc = Jsoup.connect(url)
					.userAgent(userAgentToken)
					.followRedirects(true)
					.referrer(referrerPage)
					.get();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Connection error");
			return null;
		}
		
		Elements magList = doc.getElementsByClass("postbox").get(0)
					.getElementsByTag("a");
		
		for(Element ele : magList) {
			try{
				Elements tmpList = ele.getElementsByTag("table").get(0)
						.getElementsByTag("tbody").get(0)
						.getElementsByTag("tr").get(0)
						.getElementsByTag("td");
				
				String title = tmpList.get(1).getElementsByClass("sbjbox").get(0).getElementsByTag("b").get(0).ownText();
				String thumb = tmpList.get(0).getElementsByClass("thumb").get(0).getElementsByTag("img").get(0).attr("src");
				
				urlList.add(new String[] {title, thumb});
				System.out.println("Parsing success: " + title);
			}catch(Exception e) {
				e.printStackTrace();
				System.out.println("Fail to parseing: " + ele.toString());
			}
		}
		
		
		return urlList;
	}
	
	public static ArrayList<String> ParseMagVolume(String url) {
		ArrayList<String> dataList = new ArrayList<>();
		
		return dataList;
	}
	
	public static ArrayList<String> ParseMag(String url) {
		ArrayList<String> dataList = new ArrayList<>();
		
		return dataList;
	}
}
