package org.iviagteam.magparser;

import java.util.ArrayList;

import org.iviagteam.magparser.callback.MaruVolumeCallback;
import org.iviagteam.magparser.wrapper.MaruVolumeUrlWrapper;
import org.iviagteam.magparser.wrapper.MaruVolumeWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MaruVolumeParser extends VolumeParser{

	public static final String VOLUME_IMG_TAG = "{%img}";
	
	private Status status = Status.IDLE;
	private String url;
	private MaruVolumeCallback callback;
	
	
	
	public MaruVolumeParser(String url, MaruVolumeCallback callback) {
		super(url, callback);
		this.url = url;
		this.callback = callback;
	}
	
	
	
	public Status getStatus() {
		return this.status;
	}
	
	
	
	@Override
	public void run() {
		System.out.println(TAG + " ParseMagVolume - Search volume request: " + this.url);
		
		//init
		MaruVolumeWrapper list = new MaruVolumeWrapper();
		Document doc;
		
		if(this.url.startsWith("https")) this.url.replaceFirst("https", "http");
		
		//Try connect
		try {
			this.status = Status.CONNECTING;
			System.out.println(TAG + " Try to connect '" + this.url + "'...");
			doc = Jsoup.connect(this.url)
					.userAgent(USER_AGENT_TOKEN)
					.followRedirects(true)
					.referrer(REFERRER_PAGE)
					.timeout(30000)
					.get();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(TAG + " Connection error");
			this.status = null;
			this.callback.callback(null, e);
			return;
		}
		//Get Title
		this.status = Status.TITLE_PARSING;
		list.setTitle(doc.select("#bbsview>.viewbox>.subject>h1").get(0).ownText());
		
		//Get Thumbnail
		this.status = Status.IMG_PARSING;
		Elements imgs = doc.select("#vContent img[src]");
		
		if(!imgs.isEmpty()) {
			for(Element img : imgs) {
				
				//Check Tag
				String tag = img.attr("alt");
				if(!tag.isEmpty() && tag.equals("태그") || tag.equals("페이스북 쉐어 버튼")) { //NEED more Exception
					System.out.println(TAG + " Find img tag. Finish find thumbnails.");
					break;
				}
				
				//Add thumbnail to list
				list.addThumb(img.attr("src"));
				System.out.println(TAG + " Thumbnail parsing success: " + img.attr("src"));
			}
			//Check Tag
			
		}else {
			System.out.println(TAG + " Thumbnail parsing fail (NOT_FOUND)");
		}
		
		this.status = Status.VOLUME_PARSING;
		Elements magList = doc.select("#vContent a[href^=" + VOLUME_PREFIX + "]");
		
		for(Element ele : magList) {
			try {
				String magTitle;
				String magUrl = ele.attr("href");
				
				//Manga title search
				if(ele.ownText().length() > 0) {
					magTitle = ele.ownText();
				}else {
					magTitle = getOwnText(ele);
					
					if(magTitle == null) {
						magTitle = "undefined";
					}
				}
				
				//Check repetition
				int rpIndex = indexOfUrlList((ArrayList<MaruVolumeUrlWrapper>) list.getVolumeList(), magUrl);
				if(rpIndex >= 0) {
					System.out.println(TAG + " This link is repetition (index:" + rpIndex + ") <" + magUrl + "> will be remove");
					list.getVolumeList().remove(rpIndex);
				}
				
				list.addVolume(magTitle, magUrl);
				System.out.println(TAG + " Manga url parsing success: " + magTitle);
				
			}catch(Exception e) {//fail
				e.printStackTrace();
				System.out.println(TAG + " Manga url parsing fail: " + ele.toString());
			}
		}
		
		this.status = Status.DONE;
		this.callback.callback(list, null);
	}
	
	
	
	private int indexOfUrlList(ArrayList<MaruVolumeUrlWrapper> list, String url) {
		for(MaruVolumeUrlWrapper ele : list) {
			if(ele.getUrl() == url) return list.indexOf(ele);
		}
		return -1;
	}
}
