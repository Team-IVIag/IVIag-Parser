package org.iviagteam.magparser;

import java.util.ArrayList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class IVIagParser extends Thread{
	
	public static final String VERSION = "2.1";
	public static final int VERSION_CODE = 3;
	public final String TAG = "[IVIagParser]";

	public final String USER_AGENT_TOKEN = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36";
	public final String REFERRER_PAGE = "http://www.google.com";
	
	public final String VOLUME_PREFIX = "http://www";
	public final String MAG_TITLE_TAG = "{%title}";
	public final static String ILLEGAL_CHARS = "\\/:?\"*<>|";
	
	public static String[] CLOUD_PROXY_COOKIE = new String[] {"sucuri_cloudproxy_uuid_000000000", "00000000000000000000000000000000"};
	
	@SuppressWarnings("rawtypes")
	public Enum getStatus() {
		return null;
	}
	
	
	
	protected String getOwnText(Element ele) throws Exception {
		return this.getOwnText(ele, 0);
	}
	
	
	
	protected String getOwnText(Element ele, int repeat) throws Exception {
		if(repeat > 128) {
			throw new Exception(TAG + " getOwnText too many recursive function");
		}
		
		if(ele.ownText().length() > 0) {
			return ele.ownText();
		}else if(!ele.children().isEmpty()) {
			Elements eles = ele.children();
			String ownText = null;
			for(int p = 0; p < eles.size(); p++) {
				if((ownText = this.getOwnText(eles.get(p), repeat + 1)) != null) {
					break;
				}
			}
			return ownText;
		}else {
			return null;
		}
	}
	
	
	
	protected String getUrl(Element ele, int repeat) {
		return "a";
	}
	
	
	
	protected String stringToCode(String str) {
		String codes = "";
		for(int p = 0; p < str.length(); p++) {
			codes += (int) str.charAt(p) + " ";
		}
		return codes;
	}
	
	
	
	protected boolean detourCloudProxy(Document doc) {
		System.out.println(TAG + " Try detour CloudProxy...");
		
		try {
			ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
			scriptEngine.eval(
					"var document = {};"
					+ "var location = {reload: function(){"
						+ "org.iviagteam.magparser.IVIagParser.CLOUD_PROXY_COOKIE[0] = document.cookie.toString().split('=')[0];"
						+ "org.iviagteam.magparser.IVIagParser.CLOUD_PROXY_COOKIE[1] = document.cookie.toString().split('=')[1];"
					+ "}};"
					+ doc.getElementsByTag("script").get(0).data() + ";"
					+ "print('[JavaScriptEngine] Cookie: ' + document.cookie);"
					);
		} catch (ScriptException e) {
			e.printStackTrace();
			System.out.println(TAG + " Fail to eval script");
			return false;
		}
		
		return true;
	}
	
	
	
	protected ArrayList<Integer> illegalCharIndex(String str) {
		ArrayList<Integer> index =  new ArrayList<>();
		for(int p = 0; p < str.length(); p++) {
			if(ILLEGAL_CHARS.indexOf(str.charAt(p)) >= 0) {
				index.add(p);
			}
		}
		return index;
	}
	
	
	
	public static String illegalCharFixer(String str) {
		char chr;
		String result = "";
		for(int p = 0; p < str.length(); p++) {
			if(IVIagParser.ILLEGAL_CHARS.indexOf((chr = str.charAt(p))) < 0) {
				result += chr;
			}
		}
		return result;
	}
	
	
	
	public static String urlToFilename(String pageUrl) {
		//Parse filename
		String[] tmpSptUrl = pageUrl.split("/");
		String pageTitle = (tmpSptUrl[tmpSptUrl.length - 1]).split("\\?")[0];
		
		//Check file name is valid
		return IVIagParser.illegalCharFixer(pageTitle);
	}
}
