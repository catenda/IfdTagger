package no.catenda.ifd;

public class Tag {

	private final String guid;
	private final String name;
	private final String languageCode;

	public Tag(String guid, String name, String languageCode) {
		this.guid = guid;
		this.name = name;
		this.languageCode = languageCode;
	}

	public String getGuid() {
		return guid;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLanguageCode() {
		return languageCode;
	}
	
}
