package es.trueta.documentacion_repo.bonita;

import java.io.IOException;
import java.util.Map;

import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.apache.commons.httpclient.HttpException;

import es.trueta.documentacion_repo.exceptions.UnknownWorkflowException;


public interface BonitaService {


	/**
	 * Recupera la cookie de sessión de Bonita con el perfil de alfresco-global.properties
	 * @return devuelve la cookie con la session ID
	 * @throws UnknownAuthorityException si falla el login
	 * @throws IOException
	 * @throws HttpException
	 */
	public String getCookie() throws UnknownAuthorityException, HttpException, IOException;

	/**
	 * Llamada a Bonita para consultar el id de los workflows.
	 * @return devuelve un array de versiones de workflow
	 * @throws UnknownWorkflowException cuando el workflow requerido no existe o no se encuentra
	 * @throws IOException si hay fallos I/O
	 */
	public String getWorkflowId(String workflowName) throws UnknownWorkflowException, IOException;

	/**
	 * Llamada para iniciar un workflow de bonita.
	 * @param parameters mapa de parámetros que se quieren utilizar en las llamadas
	 * @return caseId del workflow
	 * @throws Exception si se produce algun error.
	 */
	public Integer startWorkflow(Map<String, Object> parameters) throws Exception;

}
