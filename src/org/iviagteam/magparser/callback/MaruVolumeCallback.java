package org.iviagteam.magparser.callback;

import org.iviagteam.magparser.wrapper.MaruVolumeWrapper;

public interface MaruVolumeCallback {
	void callback(MaruVolumeWrapper result, Exception whenError);
}
