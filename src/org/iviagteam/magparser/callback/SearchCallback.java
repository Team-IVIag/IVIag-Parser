package org.iviagteam.magparser.callback;

import java.util.ArrayList;

import org.iviagteam.magparser.wrapper.MaruSearchWrapper;

public interface SearchCallback extends ParserCallback{
	void callback(ArrayList<MaruSearchWrapper> result);
}
