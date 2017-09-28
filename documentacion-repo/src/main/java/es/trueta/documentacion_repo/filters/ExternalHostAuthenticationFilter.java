package es.trueta.documentacion_repo.filters;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.webdav.auth.BaseAuthenticationFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.springframework.extensions.webscripts.servlet.DependencyInjectedFilter;


public class ExternalHostAuthenticationFilter extends BaseAuthenticationFilter implements DependencyInjectedFilter	{

	private static Log logger = LogFactory.getLog(ExternalHostAuthenticationFilter.class);

	private final static String EXTERNAL_HEADER = "external.authentication.proxyHeader";
	private final static String PERMITED_HOSTS = "external.authentication.permitedHosts";

	private Properties globalProperties;

	@Override
	protected Log getLogger() {
		return logger;
	}
	
	

	public ExternalHostAuthenticationFilter() {
		super();
	}


	@Override
	public void doFilter(ServletContext context, ServletRequest request, ServletResponse response, javax.servlet.FilterChain chain) throws IOException, ServletException {

		String externalHeader = globalProperties.getProperty(EXTERNAL_HEADER);
		String host = request.getRemoteHost();

		HttpServletRequestWrapper req = new HttpServletRequestWrapper((HttpServletRequest) request);
		HttpServletResponse res = (HttpServletResponse) response;


		if(req.getHeader(externalHeader)!= null && !isPermitedHost(host)){

			res.setStatus(HttpStatus.SC_FORBIDDEN);

			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.write("Forbidden header '" + externalHeader + "' from host '" + host + "'"); 

		}else{
			chain.doFilter(request, response);
		}		
	}

	

	

	private boolean isPermitedHost(String host) {
		String permitedHosts = globalProperties.getProperty(PERMITED_HOSTS);
		if(permitedHosts != null){
			for (String permitedHost : permitedHosts.split(",")) {
				if(permitedHost.equals(host) || permitedHost.equals("*")){
					return true;
				}
			}
		}
		return false;
	}

	public Properties getGlobalProperties() {
		return globalProperties;
	}

	public void setGlobalProperties(Properties globalProperties) {
		this.globalProperties = globalProperties;
	}



	






}
