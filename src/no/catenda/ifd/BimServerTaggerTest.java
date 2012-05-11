package no.catenda.ifd;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.ConnectionException;
import org.bimserver.client.factories.UsernamePasswordAuthenticationInfo;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.deserializers.DeserializeException;
import org.bimserver.shared.ServiceInterface;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BimServerTaggerTest {

	private Tagger tagger;
	private ServiceInterface serviceInterface;
	private SProject project;
	
	@Before
	public void setUp() throws ConnectionException, ServerException, UserException, DeserializeException, IOException, PluginException {
		UsernamePasswordAuthenticationInfo authenticationInfo = new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin");
		PluginManager pluginManager = new PluginManager();
		pluginManager.loadPluginsFromCurrentClassloader();
		BimServerClient client = new BimServerClient(pluginManager);
		client.setAuthentication(authenticationInfo);
		client.connectProtocolBuffers("localhost", 8020);

		serviceInterface = client.getServiceInterface();
		project = serviceInterface.addProject(UUID.randomUUID().toString());

		tagger = new Tagger(new BimServerTagStore(project, serviceInterface, pluginManager));
	}
	
	@After
	public void tearDown() throws ServerException, UserException {
		serviceInterface.deleteProject(project.getOid());
	}
	
	@Test
	public void testSetTag() throws TagStoreCommitException {
		Tag tag = new Tag("1Co3yK0000A34oE3WnEJ8o", "Example name", "en");
		tagger.setTag(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), tag);
		List<Tag> tags = tagger.getAllTags(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), "en");
		assertEquals(1, tags.size());
		assertEquals("1Co3yK0000A34oE3WnEJ8o", tags.get(0).getGuid());
		assertEquals("Example name", tags.get(0).getName());
		assertEquals("en", tags.get(0).getLanguageCode());
	}
	
	@Test
	public void testSetTagUpdate() throws TagStoreCommitException {
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
	public void testDeleteTag() throws TagStoreCommitException {
		Tag tag = new Tag("1Co3yK0000A34oE3WnEJ8o", "Example name", "en");
		tagger.setTag(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), tag);
		List<Tag> tags = tagger.getAllTags(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), "en");
		assertEquals(1, tags.size());
		
		tagger.deleteTag(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), "1Co3yK0000A34oE3WnEJ8o");
		tags = tagger.getAllTags(Arrays.asList(new String[] { "3vHRQ8oT0Hsm00051Mm008" }), "en");
		assertEquals(0, tags.size());		
	}

}
