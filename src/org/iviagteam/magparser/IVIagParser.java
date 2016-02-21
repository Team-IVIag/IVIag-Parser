package org.iviagteam.magparser;

import java.util.ArrayList;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public abstract class IVIagParser extends Thread{
	
	public static final String VERSION = "2.1";
	public static final int VERSION_CODE = 3;
	public final String TAG = "[IVIagParser]";
	public final static String DETOUR_TAG = "detour_cloud_proxy";

	public final String USER_AGENT_TOKEN = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.109 Safari/537.36";
	public final String REFERRER_PAGE = "http://www.marumaru.in";
	
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
		
		String code = "var document = {};"
				+ "var location = {reload: function(){"
				+ "org.iviagteam.magparser.IVIagParser.CLOUD_PROXY_COOKIE[0] = document.cookie.toString().split('=')[0];"
				+ "org.iviagteam.magparser.IVIagParser.CLOUD_PROXY_COOKIE[1] = document.cookie.toString().split('=')[1];"
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
		
		/*
		try {
			ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
			scriptEngine.eval(
					
					);
		} catch (ScriptException e) {
			e.printStackTrace();
			System.out.println(TAG + " Fail to eval script");
			return false;
		}
		*/
		
		return true;
	}
	
	
	
	public static String getCookieCache() {
		return IVIagParser.CLOUD_PROXY_COOKIE[0] + "=" + IVIagParser.CLOUD_PROXY_COOKIE[1];
	}
	
	
	
	public static void setCookieCache(String cache) throws Exception {
		String[] cookie = cache.split("=");
		if(cookie.length < 2) {
			throw new Exception("This is not instance of cookie");
		}
		IVIagParser.CLOUD_PROXY_COOKIE[0] = cookie[0];
		IVIagParser.CLOUD_PROXY_COOKIE[1] = cookie[1];
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
