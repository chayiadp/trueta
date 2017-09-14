package es.trueta.documentacion_repo.actions;

import java.util.HashMap; 
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.trueta.documentacion_repo.bonita.BonitaService;


public class IniciarWorkflowAction extends ActionExecuterAbstractBase {

	private static Log logger = LogFactory.getLog(IniciarWorkflowAction.class);
	public static final String NAME = "iniciar-workflow-action";
	public static final String WORKFLOW_ID = "workflowId";
	public static final String UUID = "uuidAlfresco";
	public static final String WORKFLOW_NAME = "workflowName";

	private BonitaService bonitaService;

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

		try {
			
			String workflowName = (String) action.getParameterValue(WORKFLOW_NAME);
			Map<String, Object> wfParams = new HashMap<>();
			wfParams.put(UUID, actionedUponNodeRef.getId());
			wfParams.put(WORKFLOW_NAME, workflowName);
			Integer caseId = bonitaService.startWorkflow(wfParams);

			logger.info("Workflow iniciado correctamente con id: "+caseId);

		} catch (Exception e) {
			logger.error("Error durante el inicio del workflow: "+e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(WORKFLOW_NAME, DataTypeDefinition.TEXT, true, getParamDisplayLabel(WORKFLOW_NAME)));
	}

	public BonitaService getBonitaService() {
		return bonitaService;
	}

	public void setBonitaService(BonitaService bonitaService) {
		this.bonitaService = bonitaService;
	}

}
