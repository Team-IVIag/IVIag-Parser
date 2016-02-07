package org.iviagteam.magparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class IVIagParser {
	
	public static final String TAG = "[IVIagParser]";

	private final static String USER_AGENT_TOKEN = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36";
	private final static String REFERRER_PAGE = "http://www.google.com";
	private final static String SEARCH_LINK_SAMPLE = "http://marumaru.in/?r=home&mod=search&keyword={%k}&x=0&y=0";
	private final static String MANGA_PREFIX = "http://www.shencomics.com/archives/";
	
	public final static String VOLUME_IMG_TAG = "{%img}";
	
	public static String[] CLOUD_PROXY_COOKIE = new String[] {"sucuri_cloudproxy_uuid_000000000", "00000000000000000000000000000000"};
	
	
	
	public static ArrayList<String[]> ParseMagSearch(String keyword) throws Exception {
		System.out.println(TAG + " ParseMagSearch - Search request: " + keyword);
		
		ArrayList<String[]> urlList = new ArrayList<>();
		String url = SEARCH_LINK_SAMPLE.replace("{%k}", keyword);
		Document doc;
		
		//Try Connect
		try {
			System.out.println(TAG + " try to connect '" + url + "'...");
			doc = Jsoup.connect(url)
					.userAgent(USER_AGENT_TOKEN)
					.followRedirects(true)
					.referrer(REFERRER_PAGE)
					.timeout(30000)
					.get();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(TAG + " Connection error");
			return null;
		}
		
		Elements magList = doc.select(".postbox a");
		
		for(Element ele : magList) {
			try{
				Elements tmpList = ele.select("table tbody tr td");
			
				String magUrl = ele.attr("href");
				String title = tmpList.get(1).select(".sbjbox b").get(0).ownText();
				String thumb = tmpList.get(0).select(".thumb img").get(0).attr("src");
				
				urlList.add(new String[] {title, thumb, magUrl});
				System.out.println(TAG + " Parsing success: " + title);
			}catch(Exception e) {
				e.printStackTrace();
				System.out.println(TAG + " Fail to parseing: " + ele.toString());
			}
		}
		
		return urlList;
	}
	
	
	
	public static ArrayList<String[]> ParseMagVolume(String url) {
		System.out.println(TAG + " ParseMagVolume - Search volume request: " + url);
		
		//init
		ArrayList<String[]> list = new ArrayList<>();
		Document doc;
		
		if(url.startsWith("https")) url.replaceFirst("https", "http");
		
		//Try connect
		try {
			System.out.println(TAG + " Try to connect '" + url + "'...");
			doc = Jsoup.connect(url)
					.userAgent(USER_AGENT_TOKEN)
					.followRedirects(true)
					.referrer(REFERRER_PAGE)
					.timeout(30000)
					.get();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(TAG + " Connection error");
			return null;
		}
		
		//Get Thumbnail
		Elements imgs = doc.select("#vContent img[src]");
		
		if(!imgs.isEmpty()) {
			for(Element img : imgs) {
				
				//Check Tag
				String tag = img.attr("alt");
				if(!tag.isEmpty() && tag.equals("태그")) {
					System.out.println(TAG + " Find img tag. Finish find thumbnails.");
					break;
				}
				
				//Add thumbnail to list
				list.add(new String[] {IVIagParser.VOLUME_IMG_TAG, img.attr("src")});
				System.out.println(TAG + " Thumbnail parsing success: " + img.attr("src"));
			}
			//Check Tag
			
		}else {
			System.out.println(TAG + " Thumbnail parsing fail (NOT_FOUND)");
		}
		
		Elements magList = doc.select("#vContent a[href^=" + MANGA_PREFIX + "]");
		
		for(Element ele : magList) {
			try {
				String magTitle;
				String magUrl = ele.attr("href");
				
				//Manga title search
				if(ele.ownText().length() > 0) {
					magTitle = ele.ownText();
				}else {
					magTitle = IVIagParser.getOwnText(ele);
					
					if(magTitle == null) {
						magTitle = "undefined";
					}
				}
				
				//Check repetition
				int rpIndex = IVIagParser.hasUrl(list, magUrl);
				if(rpIndex >= 0) {
					System.out.println(TAG + " This link is repetition (index:" + rpIndex + ") <" + magUrl + "> will be remove");
					list.remove(rpIndex);
				}
				
				list.add(new String[] {magTitle, magUrl});
				System.out.println(TAG + " Manga url parsing success: " + magTitle);
				
			}catch(Exception e) {//fail
				e.printStackTrace();
				System.out.println(TAG + " Manga url parsing fail: " + ele.toString());
			}
		}
		/*
		Elements magList = doc.select(".content div");
		
		for(Element ele : magList) {
			try{
				if(ele.hasClass("tag")) {
					System.out.println(TAG + " Tag found. Search finished.");
					break;
				}
				
				String title;
				String link;
				Elements eles;
				
				//String, img check
				if(!(eles = ele.getElementsByTag("img")).isEmpty()) {
					title = VOLUME_IMG_TAG;
					link = eles.get(0).attr("src");
				}else if(!(eles = ele.getElementsByTag("span")).isEmpty()) {
					title = IVIagParser.getOwnText(ele);
					link = eles.get(0).getElementsByTag("a").get(0).attr("href");
				}else {
					continue;
				}
				
				//null check
				if(title == null) {
					title = "undefined";
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
		*/
		return list;
	}
	
	
	
	//만화의 URL (marumaru.in/b/manga/숫자 꼴)로 화 목록을 불러옵니다.
	public static TreeMap<String, String> getMangaList(String mangaUrl){
		if(mangaUrl.startsWith("https")) mangaUrl.replaceFirst("https", "http");
		
		TreeMap<String, String> map = new TreeMap<>();
		
		//Try connect
		try {
			System.out.println(TAG + " Try to connect '" + mangaUrl + "'...");
			Document doc = Jsoup.connect(mangaUrl)
					.userAgent(USER_AGENT_TOKEN)
					.followRedirects(true)
					.referrer(REFERRER_PAGE)
					.timeout(30000)
					.get();
			
			doc.select("#vContent a[href^=" + MANGA_PREFIX + "]").forEach((v) -> {
				if(v.ownText().length() > 0) {
					
				}else {
					
				}
				map.put(v.ownText(), v.attr("href"));
			});
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(TAG + " Connection error");
		}
		
		return map;
	}
	
	
	
	public static ArrayList<String[]> ParseMag(String url) {
		System.out.println(TAG + " ParseMag - Parsing Manga request: " + url);
		
		return IVIagParser.ParseMag(url, false);
	}
	
	
	
	private static ArrayList<String[]> ParseMag(String url, boolean repeat) {
		
		//init
		ArrayList<String[]> list = new ArrayList<>();
		Document doc;
		
		if(url.startsWith("https")) url.replaceFirst("https", "http");
		
		//Try connect
		try {
			System.out.println(TAG + " Try to connect '" + url + "'...");
			doc = Jsoup.connect(url)
					.userAgent(USER_AGENT_TOKEN)
					.followRedirects(true)
					.referrer(REFERRER_PAGE)
					.timeout(30000)
					.cookie(CLOUD_PROXY_COOKIE[0], CLOUD_PROXY_COOKIE[1])
					.get();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(TAG + " Connection error");
			return null;
		}
		
		if(doc.title().equals("You are being redirected...")) {
			
			if(repeat) {
				System.out.println(TAG + " Fail detour CloudProxy.");
				return null;
			}
			
			return IVIagParser.detourCloudProxy(doc, url);
		}
		
		//TODO
		
		return list;
	}
	
	
	
	private static int hasUrl(ArrayList<String[]> list, String url) {
		for(String[] eles : list) {
			for(String ele : eles) {
				if(ele == url) return list.indexOf(eles);
			}
		}
		return -1;
	}
	
	
	
	private static String getOwnText(Element ele) throws Exception {
		return IVIagParser.getOwnText(ele, 0);
	}
	
	
	
	private static String getOwnText(Element ele, int repeat) throws Exception {
		if(repeat > 1024) {
			throw new Exception(TAG + " getOwnText too many recursive function");
		}
		
		if(ele.ownText().length() > 0) {
			return ele.ownText();
		}else if(!ele.children().isEmpty()) {
			Elements eles = ele.children();
			String ownText = null;
			for(int p = 0; p < eles.size(); p++) {
				if((ownText = IVIagParser.getOwnText(eles.get(p), repeat + 1)) != null) {
					break;
				}
			}
			return ownText;
		}else {
			return null;
		}
	}
	
	
	
	@SuppressWarnings("unused")
	private static String getUrl(Element ele, int repeat) {
		return "a";
	}
	
	
	
	@SuppressWarnings("unused")
	private static String stringToCode(String str) {
		String codes = "";
		for(int p = 0; p < str.length(); p++) {
			codes += (int) str.charAt(p) + " ";
		}
		return codes;
	}
	
	
	
	private static  ArrayList<String[]> detourCloudProxy(Document cfDoc, String refreshUrl) {
		System.out.println(TAG + " Try detour CloudProxy...");
		
		try {
			ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
			scriptEngine.eval(
					"var document = {};"
					+ "var location = {};"
					+ "location.reload = function(){"
						+ "org.iviagteam.magparser.IVIagParser.CLOUD_PROXY_COOKIE[0] = document.cookie.toString().split('=')[0];"
						+ "org.iviagteam.magparser.IVIagParser.CLOUD_PROXY_COOKIE[1] = document.cookie.toString().split('=')[1];"
					+ "};" 
					+ cfDoc.getElementsByTag("script").get(0).data() + ";"
					+ "print('[JavaScriptEngine] Cookie: ' + document.cookie);"
					);
		} catch (ScriptException e) {
			e.printStackTrace();
			System.out.println(TAG + " Fail to eval script");
		}
		
		return IVIagParser.ParseMag(refreshUrl, true);
	}
}
