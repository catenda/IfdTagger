package no.catenda.ifd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tagger {
	
	private final TagStore store;

	public Tagger(TagStore store) {
		this.store = store;
	}

	public void setTag(List<String> taggedObjectGuids, Tag tag) throws TagStoreCommitException {
		for (String guid : taggedObjectGuids) {
			store.setTag(guid, tag);
		}
		store.commit(this);
	}

	public void deleteTag(List<String> taggedObjectGuids, String tagGuid) throws TagStoreCommitException {
		for (String guid : taggedObjectGuids) {
			store.deleteTag(guid, tagGuid);
		}
		store.commit(this);		
	}

	public List<Tag> getAllTags(List<String> taggedObjectGuids, String languageCode) {
		List<Tag> tags = new ArrayList<Tag>();
		for (String guid : taggedObjectGuids) {
			tags.addAll(store.getTags(guid, languageCode));
		}
		return tags;
	}
	
	public List<Tag> getCommonTags(List<String> taggedObjectGuids, String languageCode) {		
		Map<String, Tag> commonTags = null;
		for (String taggedObjectGuid : taggedObjectGuids) {
			List<Tag> tags = getAllTags(Arrays.asList(new String[] { taggedObjectGuid }), languageCode);
			if (commonTags == null) {
				commonTags = new HashMap<String, Tag>();
				for (Tag tag : tags) {
					commonTags.put(tag.getGuid(), tag);
				}
			} else {
				for (Tag tag : tags) {
					if (!commonTags.containsKey(tag.getGuid())) {
						commonTags.remove(tag.getGuid());
					}
				}
			}
		}
		return new ArrayList<Tag>(commonTags.values());
	}
	
    public boolean[] isTagged(List<String> taggedObjectGuids, String languageCode) {
        boolean[] isTagged = new boolean[taggedObjectGuids.size()];
        for (int i = 0; i < taggedObjectGuids.size(); i++) {
        	isTagged[i] = store.isTagged(taggedObjectGuids.get(i), languageCode);
        }
        return isTagged;
    }

	public Set<String> getLanguageCodes() {
		return store.getLanguageCodes();
	}
}
