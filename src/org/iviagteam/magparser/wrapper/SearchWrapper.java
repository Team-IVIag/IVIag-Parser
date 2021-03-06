package org.iviagteam.magparser.wrapper;

public class SearchWrapper {
	private final String name;
	private final String thumbUrl;
	private final String mangaUrl;
	
	public SearchWrapper(final String name, final String thumbUrl, final String mangaUrl) {
		this.name = name;
		this.thumbUrl = thumbUrl;
		this.mangaUrl = mangaUrl;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getThumbUrl() {
		return this.thumbUrl;
	}
	
	public String getMangaUrl() {
		return this.mangaUrl;
	}
}
