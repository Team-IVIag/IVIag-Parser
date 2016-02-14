package org.iviagteam.magparser.callback;

import java.util.ArrayList;

import org.iviagteam.magparser.wrapper.MaruSearchWrapper;

public interface MaruSearchCallback {
	void callback(ArrayList<MaruSearchWrapper> result, Exception whenError);
}
