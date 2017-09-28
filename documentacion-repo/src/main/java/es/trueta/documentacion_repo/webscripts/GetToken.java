package es.trueta.documentacion_repo.webscripts;

import java.io.IOException;


import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GetToken extends AbstractWebScript {

	AuthenticationService authenticationService;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		try {
			String ticket = authenticationService.getNewTicket();

			JSONObject obj = new JSONObject();
			obj.put("token", ticket);
			String jsonString = obj.toString();

			res.addHeader("Access-Control-Allow-Origin", "*");
			res.addHeader("Access-Control-Allow-Headers","*");


			res.getWriter().write(jsonString);
		} catch (JSONException e) {
			res.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
}
