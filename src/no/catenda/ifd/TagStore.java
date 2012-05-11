package no.catenda.ifd;

import java.util.List;
import java.util.Set;

public interface TagStore {
	void setTag(String guid, Tag tag);
	void deleteTag(String guid, String tagGuid);
	List<Tag> getTags(String guid, String languageCode);
	boolean isTagged(String guid, String languageCode);
	Set<String> getTaggedObjects();
	void commit(Tagger tagger) throws TagStoreCommitException;
	Set<String> getLanguageCodes();
}
