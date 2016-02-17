package org.iviagteam.magparser.callback;

import org.iviagteam.magparser.wrapper.MaruVolumeWrapper;

public interface VolumeCallback extends ParserCallback{
	void callback(MaruVolumeWrapper result, Exception e);
}
