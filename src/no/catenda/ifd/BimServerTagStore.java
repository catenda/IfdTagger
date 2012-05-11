package no.catenda.ifd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;

import org.bimserver.ifc.IfcModel;
import org.bimserver.interfaces.objects.SCheckinResult;
import org.bimserver.interfaces.objects.SDownloadResult;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SSerializer;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Factory;
import org.bimserver.models.ifc2x3tc1.IfcClassificationReference;
import org.bimserver.models.ifc2x3tc1.IfcGloballyUniqueId;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcRelAssociatesClassification;
import org.bimserver.models.ifc2x3tc1.IfcRoot;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.deserializers.DeserializeException;
import org.bimserver.plugins.deserializers.DeserializerPlugin;
import org.bimserver.plugins.deserializers.EmfDeserializer;
import org.bimserver.plugins.schema.SchemaDefinition;
import org.bimserver.plugins.serializers.EmfSerializer;
import org.bimserver.plugins.serializers.IfcModelInterface;
import org.bimserver.plugins.serializers.SerializerPlugin;
import org.bimserver.shared.ServiceInterface;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.utils.ByteArrayDataSource;

public class BimServerTagStore implements TagStore {

	private final VolatileTagStore volatileStore;
	private final ServiceInterface serviceInterface;
	private final PluginManager pluginManager;
	private SProject project;
	private IfcModelInterface model;

	public BimServerTagStore(SProject project, ServiceInterface serviceInterface, PluginManager pluginManager)
			throws ServerException, UserException, DeserializeException, IOException, PluginException {
		volatileStore = new VolatileTagStore();
		this.project = project;
		this.serviceInterface = serviceInterface;
		this.pluginManager = pluginManager;
		populateStore();
	}
	
	private void populateStore() throws ServerException, UserException, DeserializeException, IOException, PluginException {
		long revision = project.getLastRevisionId();
		if (revision == -1) {
			model = new IfcModel();
		} else {
			model = readModelFromRevisions(serviceInterface, pluginManager, new HashSet<Long>(Arrays.asList(revision)));
		}
		
		for (IfcRelAssociatesClassification relAssociatesClassification : model.getAllWithSubTypes(IfcRelAssociatesClassification.class)) {
			IfcClassificationReference classificationReference = (IfcClassificationReference) relAssociatesClassification.getRelatingClassification();
			Tag tag = new Tag(
					classificationReference.getItemReference(),
					classificationReference.getName(),
					classificationReference.getReferencedSource().getEdition());
			for (IfcRoot object : relAssociatesClassification.getRelatedObjects()) {
				setTag(object.getGlobalId().getWrappedValue(), tag);
			}
		}			
	}

	@Override
	public void commit(Tagger tagger) throws TagStoreCommitException {
		model.indexGuids();

		for (String guid : getTaggedObjects()) {
			IfcRoot ifcRoot = model.get(guid);
			if (ifcRoot == null) {
				IfcProduct product = Ifc2x3tc1Factory.eINSTANCE.createIfcProduct();
				IfcGloballyUniqueId ifcGloballyUniqueId = Ifc2x3tc1Factory.eINSTANCE.createIfcGloballyUniqueId();
				ifcGloballyUniqueId.setWrappedValue(guid);
				product.setGlobalId(ifcGloballyUniqueId);
				model.add(product);
			}
		}
	
		BimServerTagMerger merger = new BimServerTagMerger();
		merger.merge(tagger, model);

		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			SerializerPlugin serializerPlugin = pluginManager.getFirstSerializerPlugin("application/ifc", true);
			final EmfSerializer serializer = serializerPlugin.createSerializer();
			serializer.init(model, null, pluginManager, null);
			serializer.writeToOutputStream(out);
			
			final Long poid = project.getOid();
			final String comment = "";
			String deserializerName = "IfcStepDeserializer";
			Long fileSize = (long) out.size();
			DataHandler dataHandler = new DataHandler(new ByteArrayDataSource("bytes", out.toByteArray()));
			Boolean merge = false;
			Boolean sync = true;
			Integer checkin = serviceInterface.checkin(poid, comment, deserializerName, fileSize, dataHandler, merge, sync);
			SCheckinResult result = serviceInterface.getCheckinState(checkin);
			switch (result.getStatus()) {
			case CH_FINISHED:
				project = serviceInterface.getProjectByPoid(project.getOid());
				break;
			}
		} catch (Exception e) {
			throw new TagStoreCommitException("Failed to commit: " + e.getMessage());
		}
	}

	@Override
	public void setTag(String guid, Tag tag) {		
		volatileStore.setTag(guid, tag);
	}

	@Override
	public void deleteTag(String guid, String tagGuid) {
		volatileStore.deleteTag(guid, tagGuid);
	}

	@Override
	public List<Tag> getTags(String guid, String languageCode) {
		return volatileStore.getTags(guid, languageCode);
	}

	@Override
	public boolean isTagged(String guid, String languageCode) {
		return volatileStore.isTagged(guid, languageCode);
	}
	
	private static IfcModelInterface readModelFromRevisions(ServiceInterface serviceInterface, PluginManager pluginManager, Set<Long> revisions)
			throws ServerException, UserException, DeserializeException, IOException, PluginException {
		SSerializer serializer = serviceInterface.getSerializerByContentType("application/ifc");
		Integer downloadId = serviceInterface.downloadRevisions(revisions, serializer.getName(), true);
		SDownloadResult downloadData = serviceInterface.getDownloadData(downloadId);
		DataHandler file = downloadData.getFile();
		DeserializerPlugin deserializerPlugin = pluginManager.getFirstDeserializer("ifc", true);
		EmfDeserializer deserializer = deserializerPlugin.createDeserializer();
		SchemaDefinition schema = pluginManager.requireSchemaDefinition();
		deserializer.init(schema);
		IfcModelInterface model = deserializer.read(file.getInputStream(), "", true, 0);
		return model;
	}

	@Override
	public Set<String> getTaggedObjects() {
		return volatileStore.getTaggedObjects();
	}

	@Override
	public Set<String> getLanguageCodes() {
		return volatileStore.getLanguageCodes();
	}
}
