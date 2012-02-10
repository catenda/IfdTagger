package no.catenda.ifd;

import java.util.List;

public interface Tagger {
	public void setTag(List<String> taggedObjectGuids, Tag tag);
	public void deleteTag(List<String> taggedObjectGuids, String tagGuid);
	public List<Tag> getAllTags(List<String> taggedObjectGuids, String languageCode);
	public List<Tag> getCommonTags(List<String> taggedObjectGuids, String languageCode);
}
