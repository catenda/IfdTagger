package no.catenda.ifd;

import java.util.Arrays;
import java.util.List;

import no.catenda.buildingsmart.guid.GuidCompressor;

import org.bimserver.models.ifc2x3.Ifc2x3Factory;
import org.bimserver.models.ifc2x3.IfcClassification;
import org.bimserver.models.ifc2x3.IfcClassificationReference;
import org.bimserver.models.ifc2x3.IfcGloballyUniqueId;
import org.bimserver.models.ifc2x3.IfcRelAssociatesClassification;
import org.bimserver.models.ifc2x3.IfcRoot;
import org.bimserver.plugins.serializers.IfcModelInterface;

public class BimServerTagMerger {	
	
	public void merge(Tagger tagger, IfcModelInterface model, String languageCode) {
				
		// Find classification
		
		IfcClassification classification = null;
		for (IfcClassification currentClassification : model.getAllWithSubTypes(IfcClassification.class)) {
			if (currentClassification.getName().equals("IFD Library")) { 
				classification = currentClassification;
			}				
		}

		// Create classification

		if (classification == null) {
			classification = Ifc2x3Factory.eINSTANCE.createIfcClassification();
			classification.setSource("http://ifd-library.org");
			classification.setEdition(languageCode);
			classification.setName("IFD Library");
			model.add(classification);
		}		
		
		for (IfcRoot root : model.getAllWithSubTypes(IfcRoot.class)) {
			List<Tag> tags = tagger.getAllTags(Arrays.asList(root.getGlobalId().getWrappedValue()), languageCode);
			for (Tag tag : tags) {
				
				// Find classification reference
				
				IfcClassificationReference classificationReference = null;
				for (IfcClassificationReference currentClassificationReference : model.getAll(IfcClassificationReference.class)) {
					if (currentClassificationReference.getItemReference().equals(tag.getGuid())) { 
						classificationReference = currentClassificationReference;
						break;
					} 
				}
				
				// Create classification reference
				
				if (classificationReference == null) {
					classificationReference = Ifc2x3Factory.eINSTANCE.createIfcClassificationReference();
					classificationReference.setLocation("http://ifdbrowser.catenda.no/Main.html?ifdguid=" + tag.getGuid());
					classificationReference.setItemReference(tag.getGuid());
					classificationReference.setName(tag.getName());
					classificationReference.setReferencedSource(classification);
					model.add(classificationReference);
				}
				
				IfcRelAssociatesClassification relAssociatesClassification = Ifc2x3Factory.eINSTANCE.createIfcRelAssociatesClassification();
				IfcGloballyUniqueId globalId = Ifc2x3Factory.eINSTANCE.createIfcGloballyUniqueId();
				globalId.setWrappedValue(generateGUID());
				relAssociatesClassification.setGlobalId(globalId);
				relAssociatesClassification.setOwnerHistory(null);
				relAssociatesClassification.setName("IfdRelationship");
				relAssociatesClassification.setRelatingClassification(classificationReference);				
				relAssociatesClassification.getRelatedObjects().add(root);
				model.add(relAssociatesClassification);
			}
		}

	}
	
	private String generateGUID(){
		return GuidCompressor.getNewIFDGloballyUniqueId();
	}
}
