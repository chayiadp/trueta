package es.trueta.documentacion_repo.actions;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

public class Ejemplo2 extends ActionExecuterAbstractBase {

	private NodeService nodeService;
	
	
	
	
	public Ejemplo2() {
		super();
		System.out.println("Nueva instancia");
	}

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		
		getNodeService().setProperty(actionedUponNodeRef, ContentModel.PROP_DESCRIPTION, "22");
		
		
		
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		// TODO Auto-generated method stub
		
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		
		System.out.println("Spring esta seteando el nodeService");
		
		this.nodeService = nodeService;
	}


	
	
}
