package org.iviagteam.magparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.LogFactory;
import org.iviagteam.magparser.logger.DefaultLogger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 * IVIagParser JAVA
 * 
 * @author SemteulGaram, Khinenw
 * @since 2016.02.06~2016.02.16
 * @version 4.0
 */

public abstract class IVIagParser{
	
	public static final String VERSION = "4.0";
	public static final int VERSION_CODE = 7;
	public static final String TAG = "IVIagParser";
	public static final String DETOUR_TAG = "IVIagParser::DetourProxy";
	public static final String USER_AGENT_TOKEN = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.109 Safari/537.36";
	public static final String REFERRER_PAGE = "http://marumaru.in";
	public static final int TIME_OUT = 30000; //Need more? :[
	
	public static String passcode = "qndxkr";
	
	public static final String MAG_TITLE_TAG = "{%title}";
	public static final String ILLEGAL_CHARS = "\\/:?\"*<>|";
	
	private static HashMap<String, String> CLOUD_PROXY_COOKIE = new HashMap<>();
	
	@SuppressWarnings("rawtypes")
	public Enum getStatus() {
		return null;
	}
	
	
	
	protected static String getOwnText(Element ele) throws Exception {
		return IVIagParser.getOwnText(ele, 0);
	}
	
	
	
	protected static String getOwnText(Element ele, int repeat) throws Exception {
		if(repeat > 128) {
			throw new Exception("getOwnText too many recursive function");
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
	
	
	
	protected static String stringToCode(String str) {
		String codes = "";
		for(int p = 0; p < str.length(); p++) {
			codes += (int) str.charAt(p) + " ";
		}
		return codes;
	}
	
	
	
	protected static boolean detourCloudProxy(Document doc) {
		System.out.println(TAG + " Try detour CloudProxy...");
		
		String code = "var document = {};"
				+ "var location = {reload: function(){"
				+ "var cookie = document.cookie.toString().split('=');"
				+ "org.iviagteam.magparser.IVIagParser.getCookies().put(cookie[0], cookie[1]);"
			+ "}};"
			+ doc.getElementsByTag("script").get(0).data() + ";"
			+ "java.lang.System.out.println('[RhinoEngine] Cookie: ' + document.cookie);";
		
		try {
			org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter();
			ctx.setOptimizationLevel(-1);
	        ctx.setLanguageVersion(org.mozilla.javascript.Context.VERSION_1_7);
			Script script = ctx.compileString(code, IVIagParser.DETOUR_TAG, 0, null);
			Scriptable scope = ctx.initStandardObjects();
			script.exec(ctx, scope);
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	public static void disableLogging(){
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	    Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
	    Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
	    DefaultLogger.getInstance().setLog(false);
	}
	
	
	public static HashMap<String, String> getCookies() {
		return IVIagParser.CLOUD_PROXY_COOKIE;
	}
	
	
	
	public static void setCookies(final HashMap<String, String> map) throws Exception {
		IVIagParser.CLOUD_PROXY_COOKIE = map;
	}
	
	
	
	public static String cookieStringify(HashMap<String, String> map) {
		String result = "";
		final Set<String> set = map.keySet();
		String key;
		for(final Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
			key = iterator.next();
			if(!result.isEmpty()) {
				result += ";";
			}
			result += key + "=" + map.get(key);
		}
		return result;
	}
	
	
	
	public static HashMap<String, String> cookieParsing(String cookieStr) {
		final HashMap<String, String> result = new HashMap<>();
		final String[] cookies = cookieStr.split(";");
		for(String cookie : cookies) {
			String[] set = cookie.split("=");
			result.put(set[0], set[1]);
		}
		return result;
	}
	
	
	
	protected static ArrayList<Integer> illegalCharIndex(String str) {
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
