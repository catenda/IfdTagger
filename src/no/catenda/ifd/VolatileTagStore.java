package no.catenda.ifd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VolatileTagStore implements TagStore {

	private final Map<String, Map<String, List<Tag>>> tags;
	private final Set<String> languageCodes;
	
	public VolatileTagStore() {
		tags = new HashMap<String, Map<String, List<Tag>>>();
		languageCodes = new HashSet<String>();
	}
	
	@Override
	public void commit(Tagger tagger) {
	}

	@Override
	public void setTag(String guid, Tag tag) {
		if (!tags.containsKey(guid)) {
			tags.put(guid, new HashMap<String, List<Tag>>());
		}
		if (!tags.get(guid).containsKey(tag.getGuid())) {
			tags.get(guid).put(tag.getGuid(), new ArrayList<Tag>());
		}
		List<Tag> list = tags.get(guid).get(tag.getGuid());
		for (int i = 0; i < list.size(); i++) {
			Tag existingTag = list.get(i);
			if (existingTag.getLanguageCode().equals(tag.getLanguageCode())) {
				list.set(i, tag);
				return;
			}
		}
		list.add(tag);
		languageCodes.add(tag.getLanguageCode());
	}

	@Override
	public void deleteTag(String guid, String tagGuid) {
		if (!tags.containsKey(guid)) {
			return;
		}
		if (!tags.get(guid).containsKey(tagGuid)) {
			return;
		}
		tags.get(guid).remove(tagGuid);
		if (tags.get(guid).isEmpty()) {
			tags.remove(guid);
		}
	}

	@Override
	public List<Tag> getTags(String guid, String languageCode) {
		List<Tag> result = new ArrayList<Tag>();
		if (!tags.containsKey(guid)) {
			return result;
		}
		Set<String> tagGuids = tags.get(guid).keySet();
		for (String tagGuid : tagGuids) {
			List<Tag> list = tags.get(guid).get(tagGuid);
			if (languageCode == null) {
				result.addAll(list);
			} else {
				for (int i = 0; i < list.size(); i++) {
					Tag existingTag = list.get(i);
					if (existingTag.getLanguageCode().equals(languageCode)) {
						result.add(existingTag);
						break;
					}
				}
			}
		}
		return result;
	}

	@Override
	public boolean isTagged(String guid, String languageCode) {
		if (!tags.containsKey(guid)) {
			return false;
		}
		Set<String> tagGuids = tags.get(guid).keySet();
		for (String tagGuid : tagGuids) {
			List<Tag> list = tags.get(guid).get(tagGuid);
			if (languageCode == null) {
				return list.size() > 0;
			} else {
				for (int i = 0; i < list.size(); i++) {
					Tag existingTag = list.get(i);
					if (existingTag.getLanguageCode().equals(tagGuid)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public Set<String> getTaggedObjects() {
		return tags.keySet();
	}

	@Override
	public Set<String> getLanguageCodes() {
		return languageCodes;
	}

}
