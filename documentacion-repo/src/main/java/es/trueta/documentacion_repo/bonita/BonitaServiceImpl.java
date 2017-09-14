package es.trueta.documentacion_repo.bonita;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.star.lang.IllegalArgumentException;

import es.trueta.documentacion_repo.actions.IniciarWorkflowAction;
import es.trueta.documentacion_repo.exceptions.UnknownWorkflowException;

public class BonitaServiceImpl implements BonitaService {

	private static final String CONTENT_TYPE = "Content-Type";
	private static final String APPLICATION_JSON = "application/json";
	private static final String CASE_ID = "caseId";
	private static final String ACTIVATIONSTATE_FILTER_ENABLED = "activationState=ENABLED";
	private static final String DISPLAY_NAME_FILTER = "displayName=";
	private static final String FILTER = "f";
	private static final String PAGE = "p";
	private static final String PAGE_0 = "0";
	private static Log logger = LogFactory.getLog(BonitaServiceImpl.class);
	private static final String BONITA_LOGINSERVICE = "/bonita/loginservice";

	private static final String RESPONSE_COOKIE_HEADER_NAME = "Set-Cookie";
	private static final String REQUEST_COOKIE_HEADER_NAME = "Cookie";
	private static final String BONITA_WORKFLOW_ID_PATH = "/bonita/API/bpm/process";
	private static final String ID = "id";
	private static final String BONITA_WORKFLOW_START_PATH_INIT = "/bonita/API/bpm/process/";
	private static final String BONITA_WORKFLOW_START_PATH_END = "/instantiation";

	private static String bonitaHost;
	private static String bonitaPort;
	private static String user;
	private static String password;
	
	

	/**
	 * Llamada que hace login a Bonita.
	 * @param user
	 * @param password
	 * @return devuelve la cookie con la session ID
	 * @throws UnknownAuthorityException si falla el login
	 * @throws IOException
	 * @throws HttpException
	 */
	@Override
	public String getCookie() throws UnknownAuthorityException, HttpException, IOException {

		logger.debug("Recuperando cookie de sessión...");
		String result = null;
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			URI uri = buildURI(BONITA_LOGINSERVICE, null);
			HttpPost request = new HttpPost(uri);

			List<NameValuePair> urlParameters = new ArrayList<>();
			urlParameters.add(new BasicNameValuePair("username", user));
			urlParameters.add(new BasicNameValuePair("password", password));
			request.setEntity(new UrlEncodedFormEntity(urlParameters));
			client = HttpClients.createDefault();

			response = client.execute(request);

			for (Header header : response.getAllHeaders()) {
				if (header.getName().equals(RESPONSE_COOKIE_HEADER_NAME)){
					result = header.getValue();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeResources(client, response);
		}
		return result;
	}

	/**
	 * Constructor de URI para llamadas REST
	 * @param path path de la llamada
	 * @param uriParams parámetros que se puedan añadir
	 * @return la URI montada con host, port y parámetros
	 * @throws URISyntaxException
	 */
	private static URI buildURI(String path, List<NameValuePair> uriParams) throws URISyntaxException {

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http");
		builder.setHost(getBonitaHost());
		builder.setPort(Integer.valueOf(getBonitaPort()));

		if (path != null && !StringUtils.isBlank(path)){
			builder.setPath(path);
		}

		if (uriParams != null && uriParams.size() > 0)
		{
			builder.addParameters(uriParams);
		}
		return builder.build();
	}

	/**
	 * Llamada a Bonita para consultar el id de los workflows.
	 * @return devuelve un array de versiones de workflow
	 * @throws UnknownWorkflowException cuando el workflow requerido no existe o no se encuentra
	 * @throws IOException si hay errores I/O
	 */
	@Override
	public String getWorkflowId(String workflowName) throws UnknownWorkflowException, IOException {

		logger.debug("Recuperando la id del workflow ...");
		if (workflowName == null){
			throw new UnknownWorkflowException("El nombre del workflow no puede ser nulo");
		}


		String result = null;
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair(PAGE, PAGE_0));
			params.add(new BasicNameValuePair(FILTER, DISPLAY_NAME_FILTER+workflowName));
			params.add(new BasicNameValuePair(FILTER,ACTIVATIONSTATE_FILTER_ENABLED));

			URI uri = buildURI(BONITA_WORKFLOW_ID_PATH, params);

			HttpGet request = new HttpGet(uri);
			request.setHeader(REQUEST_COOKIE_HEADER_NAME, getCookie());

			client = HttpClients.createDefault();
			response = client.execute(request);

			if (response.getStatusLine().getStatusCode() == 200){
				String responseString = IOUtils.toString(response.getEntity().getContent());
				JSONArray workflow = new JSONArray(responseString);
				result = workflow.getJSONObject(0).getString(ID);
				if(logger.isDebugEnabled()){
					logger.debug("Id "+result+" del workflow "+workflowName+" recuperada correctamente.");
				}
			}else{
				throw new Exception("Error al recuperar el id del workflow. Code: "+response.getStatusLine().getStatusCode()  +"-"+response.getStatusLine().getReasonPhrase());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}finally{
			closeResources(client, response);
		}
		return result;
	}

	private void closeResources(Object... args) throws IOException {
		for (Object object : args) {
			if (object instanceof Closeable){
				((Closeable) object).close();
			}
		}
	}

	/**
	 * Llamada para iniciar un workflow de bonita.
	 * @param parameters mapa de parámetros que se quieren utilizar en las llamadas
	 * @return caseId del workflow
	 * @throws Exception si se produce algun error.
	 */
	@Override
	public Integer startWorkflow(Map<String, Object> parameters) throws Exception {

		logger.debug("Iniciando workflow en bonita...");

		if (parameters == null){
			throw new IllegalArgumentException("El campo parameters es obligatorio");
		}
		if (!parameters.containsKey(IniciarWorkflowAction.UUID)){
			throw new IllegalArgumentException("Hay que informar el uuidAlfresco del documento para el que se inicia el workflow");
		}
		if (!parameters.containsKey(IniciarWorkflowAction.WORKFLOW_NAME)){
			throw new IllegalArgumentException("El nombre del workflow es un parámetro obligatorio");
		}


		Integer result = null;
		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;
		try {
			String workflowName = (String) parameters.get(IniciarWorkflowAction.WORKFLOW_NAME);
			String path = BONITA_WORKFLOW_START_PATH_INIT+getWorkflowId(workflowName)+BONITA_WORKFLOW_START_PATH_END;
			URI uri = buildURI(path, null);

			HttpPost request = new HttpPost(uri);
			request.addHeader(REQUEST_COOKIE_HEADER_NAME, getCookie());
			request.addHeader(CONTENT_TYPE, APPLICATION_JSON);

			JSONObject object = new JSONObject();
			object.put(IniciarWorkflowAction.UUID, parameters.get(IniciarWorkflowAction.UUID));
			StringEntity e = new StringEntity(object.toString(), "UTF-8");
			request.setEntity(e);

			client = HttpClients.createDefault();
			response = client.execute(request);

			if (response.getStatusLine().getStatusCode() == 200){
				String responseString = IOUtils.toString(response.getEntity().getContent());
				JSONObject obj = new JSONObject(responseString);
				result = obj.getInt(CASE_ID);
				if(logger.isDebugEnabled()){
					logger.debug("Workflow "+workflowName+" iniciado correctamente. CaseId:"+result);
				}
			}else{
				throw new Exception("Error al instanciar el workflow. Code: "+response.getStatusLine().getStatusCode()+" - "+response.getStatusLine().getReasonPhrase());
			}
		}
		catch(Exception e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}finally{
			closeResources(client, response);
		}

		return result;
	}

	public static String getBonitaHost() {
		return bonitaHost;
	}

	public static void setBonitaHost(String bonitaHost) {
		BonitaServiceImpl.bonitaHost = bonitaHost;
	}

	public static String getBonitaPort() {
		return bonitaPort;
	}

	public static void setBonitaPort(String bonitaPort) {
		BonitaServiceImpl.bonitaPort = bonitaPort;
	}

	public static String getUser() {
		return user;
	}

	public static void setUser(String user) {
		BonitaServiceImpl.user = user;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		BonitaServiceImpl.password = password;
	}

}
