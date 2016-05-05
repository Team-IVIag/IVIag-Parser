package org.iviagteam.magparser;

import java.util.ArrayList;

import org.iviagteam.magparser.callback.MaruVolumeCallback;
import org.iviagteam.magparser.wrapper.MaruVolumeUrlWrapper;
import org.iviagteam.magparser.wrapper.MaruVolumeWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MaruVolumeParser extends IVIagParser{

	enum Status{IDLE, CONNECTING, IMG_PARSING, VOLUME_PARSING, DONE};
	
	public static final String VOLUME_IMG_TAG = "{%img}";
	
	private Status status = Status.IDLE;
	private String url;
	private MaruVolumeCallback callback;
	
	
	
	public MaruVolumeParser(String url, MaruVolumeCallback callback) {
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
		Elements magList = doc.select("#vContent a[href^=" + VOLUME_PREFIX + "], #vContent span");
		
		for(Element ele : magList) {
			try {
				String magTitle;
				String magUrl = ele.attr("href");
				
				if(ele.tagName().toLowerCase() == "span"){
					if(!ele.hasAttr("cf-patch")) continue;
					
					String patch = ele.attr("cf-patch");
					String lastChar = String.valueOf(patch.charAt(patch.length() - 1));
					
					patch = patch.replace(lastChar, "").replaceFirst("^0x0*", "");
					
					int mangaId = (int) (Integer.parseInt(patch, 16) / Math.pow(2, Integer.parseInt(lastChar, 16) / 2));
					magUrl = CF_PATCH_VOLUME_PREFIX + mangaId;
				}
				
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
				int rpIndex = indexOfUrlList(list.getVolumeList(), magUrl);
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
