package es.trueta.documentacion_repo.officeTransformer;

import java.util.Date;

import org.alfresco.repo.content.transform.ContentTransformerRegistry;
import org.alfresco.repo.content.transform.TransformerConfig;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;

public class TransformerDebug extends org.alfresco.repo.content.transform.TransformerDebug {

	public TransformerDebug(NodeService nodeService, MimetypeService mimetypeService, ContentTransformerRegistry transformerRegistry, TransformerConfig transformerConfig, Log transformerLog,
			Log transformerDebugLog) {
		super(nodeService, mimetypeService, transformerRegistry, transformerConfig, transformerLog, transformerDebugLog);
	}

	@Override
	public String getFileName(TransformationOptions options, boolean firstLevel, long sourceSize) {
		String name = super.getFileName(options, firstLevel, sourceSize);
		if(name == null){
			return null;
		}
		String extension = name.split("\\.")[name.split("\\.").length-1];
		return new Date().getTime()+"."+extension;
	}

}
