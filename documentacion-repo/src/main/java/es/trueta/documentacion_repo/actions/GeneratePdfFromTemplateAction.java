package es.trueta.documentacion_repo.actions;


import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.trueta.documentacion_repo.services.OfficeTemplatesService;


public class GeneratePdfFromTemplateAction extends ActionExecuterAbstractBase {

	private static Log logger = LogFactory.getLog(GeneratePdfFromTemplateAction.class);

	public static final String UUID = "uuid";
	public static final String TIPO = "tipo";

	private OfficeTemplatesService officeTemplatesService;
	private NodeService nodeService;
	private SearchService searchService;

	@Override
	protected void executeImpl(Action action, NodeRef node) {

		try {
			
			String tipo = (String) action.getParameterValue(TIPO);
		
			Map<QName, Serializable> properties = nodeService.getProperties(node);
			//Propiedades del nodo para hacer la busqueda
			//La propiedad me la invento pero lo hago para tener un ejemplo
			String tipoDocumento = (String) properties.get(QName.createQName("trueta:tipo"));
			
			//pongo un valor "correcto" al tipo
			tipoDocumento = "calidad";
			
			//buscamos la plantilla
			ResultSet query = searchPlantilla(tipoDocumento);
			
			if(query != null && query.length()>0){
				NodeRef documentoGenerado = officeTemplatesService.generaDocumentoPlantilla(node, query.getNodeRef(0));
				//Podriamos renombrar o cambiar cualquier metadato
				//nodeService.setProperty(documentoGenerado, ContentModel.PROP_NAME, "nuevoNombre.pdf");
				
				//Podriamos mover el documento
//				String name = (String) nodeService.getProperty(documentoGenerado, ContentModel.PROP_NAME);
//				nodeService.moveNode(documentoGenerado, newParentRef, ContentModel.ASSOC_CONTAINS, 
//						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)))
				
			}
			

		} catch (Exception e) {
			logger.error("Error al generar pdf: "+e.getMessage());
			logger.error(e, e);
			throw new RuntimeException(e);
		}
	}

	private ResultSet searchPlantilla(String tipoDocumento) {
		return searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, 
				"TYPE:\"truplan:plantilla\" AND =truplan:plantilla_tipo:\"" + tipoDocumento + "\"");
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(TIPO, DataTypeDefinition.TEXT, false, "Tipo"));
		
	}
	

	public OfficeTemplatesService getOfficeTemplatesService() {
		return officeTemplatesService;
	}

	public void setOfficeTemplatesService(OfficeTemplatesService officeTemplatesService) {
		this.officeTemplatesService = officeTemplatesService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	

}
