package no.catenda.ifd;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.ConnectionException;
import org.bimserver.client.factories.UsernamePasswordAuthenticationInfo;
import org.bimserver.plugins.PluginManager;
import org.junit.Before;
import org.junit.Test;

public class BimServerTaggerTest {

	private Tagger tagger;
	
	@Before
	public void setUp() throws ConnectionException {
		UsernamePasswordAuthenticationInfo authenticationInfo = new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin");
		PluginManager pluginManager = new PluginManager();
		pluginManager.loadPluginsFromCurrentClassloader();
		BimServerClient bimServerClient = new BimServerClient(pluginManager);
		bimServerClient.setAuthentication(authenticationInfo);
		bimServerClient.connectProtocolBuffers("localhost", 8020);
		tagger = new BimServerTagger(bimServerClient);
	}
	
	@Test
	public void testSetTag() {
		Tag tag = new Tag("1Co3yK0000A34oE3WnEJ8o", "Example name", "en");
		tagger.setTag(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), tag);
		List<Tag> tags = tagger.getAllTags(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), "en");
		assertEquals(1, tags.size());
		assertEquals("1Co3yK0000A34oE3WnEJ8o", tags.get(0).getGuid());
		assertEquals("Example name", tags.get(0).getName());
		assertEquals("en", tags.get(0).getLanguageCode());
	}
	
	@Test
	public void testSetTagUpdate() {
		Tag originalTag = new Tag("1Co3yK0000A34oE3WnEJ8o", "Original name", "en");
		tagger.setTag(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), originalTag);
		List<Tag> originalTags = tagger.getAllTags(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), "en");
		assertEquals(1, originalTags.size());
		assertEquals("1Co3yK0000A34oE3WnEJ8o", originalTags.get(0).getGuid());
		assertEquals("Original name", originalTags.get(0).getName());
		assertEquals("en", originalTags.get(0).getLanguageCode());

		Tag newTag = new Tag("1Co3yK0000A34oE3WnEJ8o", "New name", "en");
		tagger.setTag(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), newTag);
		List<Tag> newTags = tagger.getAllTags(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), "en");
		assertEquals(1, newTags.size());
		assertEquals("1Co3yK0000A34oE3WnEJ8o", newTags.get(0).getGuid());
		assertEquals("New name", newTags.get(0).getName());
		assertEquals("en", newTags.get(0).getLanguageCode());
	}
	
	@Test
	public void testDeleteTag() {
		Tag tag = new Tag("1Co3yK0000A34oE3WnEJ8o", "Example name", "en");
		tagger.setTag(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), tag);
		List<Tag> tags = tagger.getAllTags(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), "en");
		assertEquals(1, tags.size());
		
		tagger.deleteTag(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), "1Co3yK0000A34oE3WnEJ8o");
		tags = tagger.getAllTags(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), "en");
		assertEquals(0, tags.size());		
	}

}
