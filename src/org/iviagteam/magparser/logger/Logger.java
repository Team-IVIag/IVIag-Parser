package org.iviagteam.magparser.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Logger{
	public final SimpleDateFormat format;
	public boolean logEnabled = true;
	public boolean ignoreDebug = true;
	
	public Logger(){
		format = new SimpleDateFormat("[yyyy-MM-dd hh:mm:ss]");
	}
	
	public String getLogString(String str, String tag){
		return format.format(new Date()) + tag + str;
	}
	
	public String getStackTrace(Throwable e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
	public void log(String str, EnumLogType t){
		log(str, makeTag(getDefaultTag()), t);
	}
	
	public void critical(String s, String tag){
		log(s, tag, EnumLogType.CRITICAL);
	}
	
	public void critical(String s){
		critical(s, getDefaultCriticalTag() + " " + makeTag(getDefaultTag()));
	}
	
	public void critical(Throwable t, String tag){
		critical(getStackTrace(t), getDefaultCriticalTag() + " " + makeTag(tag));
	}
	
	public void critical(Throwable t){
		critical(t, getDefaultTag());
	}
	
	public void error(String s, String tag){
		log(s, tag, EnumLogType.ERROR);
	}
	
	public void error(String s){
		error(s, getDefaultErrorTag() + " " + makeTag(getDefaultTag()));
	}
	
	public void error(Throwable t, String tag){
		error(getStackTrace(t), getDefaultErrorTag() + " " + makeTag(tag));
	}
	
	public void error(Throwable t){
		error(t, getDefaultTag());
	}
	
	public void warning(String str, String tag){
		log(str, getDefaultWarningTag() + " " + makeTag(tag), EnumLogType.WARNING);
	}
	
	public void warning(String str){
		info(str, getDefaultTag());
	}
	
	public void info(String str, String tag){
		log(str, getDefaultInfoTag() + " " + makeTag(tag), EnumLogType.INFO);
	}
	
	public void info(String str){
		info(str, getDefaultTag());
	}
	
	public void debug(String str, String tag){
		if(!ignoreDebug)
			log(str, getDefaultDebugTag() + " " + makeTag(tag), EnumLogType.DEBUG);
	}
	
	public void debug(String str){
		debug(str, getDefaultTag());
	}
	
	public String getDefaultDebugTag(){
		return makeTag("DEBUG");
	}
	
	public String getDefaultInfoTag(){
		return makeTag("INFO");
	}
	
	public String getDefaultWarningTag(){
		return makeTag("WARNING");
	}
	
	public String getDefaultErrorTag(){
		return makeTag("ERROR");
	}
	
	public String getDefaultCriticalTag(){
		return makeTag("CRITICAL");
	}
	
	public String getDefaultTag(){
		return "IVIagParser";
	}
	
	public String makeTag(String str){
		return "[" + str + "]";
	}
	
	public void setLog(boolean b){
		logEnabled = b;
	}
	
	public void log(String str, String tag, EnumLogType t){
		if(logEnabled) procLog(str, tag, t);
	}
	
	public abstract void procLog(String str, String tag, EnumLogType t);
}

enum EnumLogType{
	DEBUG(0), INFO(1), WARNING(2), ERROR(3), CRITICAL(4);
	
	private final int level;
	
	private EnumLogType(int level){
		this.level = level;
	}
	
	public int getLevel(){
		return this.level;
	}
}
