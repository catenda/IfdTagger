package no.catenda.ifd;

import java.util.Arrays;
import java.util.List;

import no.catenda.buildingsmart.guid.GuidCompressor;

import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Factory;
import org.bimserver.models.ifc2x3tc1.IfcClassification;
import org.bimserver.models.ifc2x3tc1.IfcClassificationReference;
import org.bimserver.models.ifc2x3tc1.IfcGloballyUniqueId;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcRelAssociatesClassification;
import org.bimserver.plugins.serializers.IfcModelInterface;

public class BimServerTagMerger {	
	
	protected void merge(Tagger tagger, IfcModelInterface model) {
		for (String languageCode : tagger.getLanguageCodes()) {
			merge(tagger, model, languageCode);
		}
	}

	public void merge(Tagger tagger, IfcModelInterface model, String languageCode) {
				
		// Find classification
		
		IfcClassification classification = null;
		for (IfcClassification currentClassification : model.getAll(IfcClassification.class)) {
			if (currentClassification.getName().equals("IFD Library")) { 
				classification = currentClassification;
			}				
		}

		// Create classification

		if (classification == null) {
			classification = Ifc2x3tc1Factory.eINSTANCE.createIfcClassification();
			classification.setSource("http://ifd-library.org");
			classification.setEdition(languageCode);
			classification.setName("IFD Library");
			model.add(classification);
		}
		
		for (IfcProduct product : model.getAllWithSubTypes(IfcProduct.class)) {
			List<Tag> tags = tagger.getAllTags(Arrays.asList(product.getGlobalId().getWrappedValue()), languageCode);
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
					classificationReference = Ifc2x3tc1Factory.eINSTANCE.createIfcClassificationReference();
					classificationReference.setLocation("http://ifdbrowser.catenda.no/Main.html?ifdguid=" + tag.getGuid());
					classificationReference.setItemReference(tag.getGuid());
					classificationReference.setName(tag.getName());
					classificationReference.setReferencedSource(classification);
					model.add(classificationReference);
				}
				
				IfcRelAssociatesClassification relAssociatesClassification = Ifc2x3tc1Factory.eINSTANCE.createIfcRelAssociatesClassification();
				IfcGloballyUniqueId globalId = Ifc2x3tc1Factory.eINSTANCE.createIfcGloballyUniqueId();
				globalId.setWrappedValue(generateGUID());
				relAssociatesClassification.setGlobalId(globalId);
				relAssociatesClassification.setOwnerHistory(null);
				relAssociatesClassification.setName("IfdRelationship");
				relAssociatesClassification.setRelatingClassification(classificationReference);				
				relAssociatesClassification.getRelatedObjects().add(product);
				model.add(relAssociatesClassification);
			}
		}

	}
	
	private String generateGUID(){
		return GuidCompressor.getNewIFDGloballyUniqueId();
	}
}
