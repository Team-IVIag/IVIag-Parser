package org.iviagteam.magparser.callback;

import org.iviagteam.magparser.wrapper.MaruMangaWrapper;

public interface MangaCallback extends ParserCallback{
	void callback(MaruMangaWrapper result, Exception e);
}
