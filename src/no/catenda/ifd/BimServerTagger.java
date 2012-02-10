package no.catenda.ifd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bimserver.client.BimServerClient;
import org.bimserver.interfaces.objects.SIfdTag;

public class BimServerTagger implements Tagger {

	private final BimServerClient bimServerClient;

	public BimServerTagger(BimServerClient bimServerClient) {
		this.bimServerClient = bimServerClient;		
	}
	
	@Override
	public void setTag(List<String> taggedObjectGuids, Tag tag) {
		for (String taggedObjectGuid : taggedObjectGuids) {
			try {
				bimServerClient.getServiceInterface().setTag(taggedObjectGuid, tag.getGuid(), tag.getName(), tag.getLanguageCode());
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void deleteTag(List<String> taggedObjectGuids, String tagGuid) {
		for (String taggedObjectGuid : taggedObjectGuids) {
			try {
				bimServerClient.getServiceInterface().deleteTag(taggedObjectGuid, tagGuid);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public List<Tag> getAllTags(List<String> taggedObjectGuids,
			String languageCode) {
		List<Tag> tags = new ArrayList<Tag>();
		for (String taggedObjectGuid : taggedObjectGuids) {
			try {
				for (SIfdTag tag : bimServerClient.getServiceInterface().getTags(taggedObjectGuid, languageCode)) {
					tags.add(new Tag(tag.getGuid(), tag.getName(), tag.getLanguageCode()));
				}
			} catch (Exception e) {
			}
		}
		return tags;
	}

	@Override
	public List<Tag> getCommonTags(List<String> taggedObjectGuids,
			String languageCode) {
		Set<Tag> commonTags = null;
		for (String taggedObjectGuid : taggedObjectGuids) {
			List<Tag> tags = getAllTags(Arrays.asList(new String[] { taggedObjectGuid }), languageCode);
			if (commonTags == null) {
				commonTags = new HashSet<Tag>(tags);
			} else {
				commonTags.retainAll(tags);
			}
		}
		return new ArrayList<Tag>(commonTags);
	}

}
