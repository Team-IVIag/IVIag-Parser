package org.iviagteam.magparser.logger;

public class DefaultLogger extends Logger{
	private static DefaultLogger instance = null;
	
	private DefaultLogger(){
		instance = this;
	}
	
	@Override
	public void log(String str, String tag, EnumLogType t) {
		System.out.println(getLogString(tag, str));
	}
	
	public static DefaultLogger getInstance(){
		if(instance == null){
			new DefaultLogger();
		}
		
		return instance;
	}
}
