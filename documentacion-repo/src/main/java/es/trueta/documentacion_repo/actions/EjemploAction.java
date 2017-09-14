package es.trueta.documentacion_repo.actions;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/*
 * Ejemplo. Accion que recibe un parametro y lo pinta en consola 
 */

public class EjemploAction extends ActionExecuterAbstractBase {

	private static Log logger = LogFactory.getLog(EjemploAction.class);

	public static final String PARAM_NAME = "param";


	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

		try {
			
			String param = (String) action.getParameterValue(PARAM_NAME);
			
			logger.debug(param);


		} catch (Exception e) {
			logger.error("Error durante el inicio del workflow: "+e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_NAME, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_NAME)));
	}


}
