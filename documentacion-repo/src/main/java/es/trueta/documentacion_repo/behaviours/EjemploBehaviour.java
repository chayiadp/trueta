package es.trueta.documentacion_repo.behaviours;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.log4j.Logger;

import es.trueta.documentacion_repo.services.OfficeTemplatesService;
/*
 * 
 * Ejemplo. Cuando se sube un documento (cm:content) con el nombre test.docx se ejecuta la accion de generar pdf con plantilla
 * 
 */
public class EjemploBehaviour implements OnCreateNodePolicy{

	private static final Logger logger = Logger.getLogger(EjemploBehaviour.class);

	private PolicyComponent policyComponent;
	private ActionService actionService;
	private NodeService nodeService;
	
	private OfficeTemplatesService officeTemplatesService;
	private SearchService searchService;
	
	public void init() {

		this.policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnCreateNodePolicy.QNAME,
				ContentModel.TYPE_CONTENT, 
				new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
	}
	
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		NodeRef node = childAssocRef.getChildRef();
		String name = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
		
		if(name.equals("testAction.docx")){
			Action action = actionService.createAction("generatePdfFromTemplateAction");
			actionService.executeAction(action, node);
		}
		if(name.equals("testService.docx")){
			try {
				officeTemplatesService.generaDocumentoPlantilla(node, searchPlantilla("calidad"));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
	}
	
	
	private NodeRef searchPlantilla(String tipoDocumento) {
		return searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, 
				"TYPE:\"truplan:plantilla\" AND =truplan:plantilla_tipo:\"" + tipoDocumento + "\"").getNodeRef(0);
	}

	
	

	public PolicyComponent getPolicyComponent() {
		return policyComponent;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public ActionService getActionService() {
		return actionService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public OfficeTemplatesService getOfficeTemplatesService() {
		return officeTemplatesService;
	}

	public void setOfficeTemplatesService(OfficeTemplatesService officeTemplatesService) {
		this.officeTemplatesService = officeTemplatesService;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	

}
