package es.trueta.documentacion_repo.authentication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.acegisecurity.Authentication;

public class TokenDBAuthenticationComponentImpl extends AbstractAuthenticationComponent {
	private static final Log LOG = LogFactory.getLog(TokenDBAuthenticationComponentImpl.class);

	private DataSource dataSource;

	private String table;
	private String columnUser;
	private String columnToken;


	@Override
	public void authenticateImpl(String userName, char[] password) throws AuthenticationException {
		String pass = new String(password);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Authenticating: " + userName + "/" + pass);
		}

		try (Connection connection = dataSource.getConnection()){
			
			String sqlSelect = "select "+ columnToken +" from "+ table + " where "+ columnUser +"=? and "+ columnToken +"=?";
			String sqlDelete = "delete from "+ table +" where "+ columnUser +"=? and "+ columnToken +"=?";

			PreparedStatement prepareStatement = connection.prepareStatement(sqlSelect);

			prepareStatement.setString(1, userName);
			prepareStatement.setString(2, pass);
			ResultSet result = prepareStatement.executeQuery();

			if(result.next()){
				PreparedStatement prepareStatementDelete = connection.prepareStatement(sqlDelete);
				prepareStatementDelete.setString(1, userName);
				prepareStatementDelete.setString(2, pass);
				prepareStatementDelete.executeUpdate();
				connection.commit();
				setCurrentUser(userName);

			}else{
				throw new AuthenticationException("Token DB Authentication fail");
			}
			

		} catch (SQLException e) {
			throw new AuthenticationException(e.getMessage(), e);
		}

	}

	public Authentication authenticate(Authentication token) throws AuthenticationException {
		throw new AlfrescoRuntimeException("Authentication via token not supported");
	}

	/**
	 * This authentication component implementation allows guest login
	 * @return
	 */
	@Override
	protected boolean implementationAllowsGuestLogin() {
		return true;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getColumnUser() {
		return columnUser;
	}

	public void setColumnUser(String columnUser) {
		this.columnUser = columnUser;
	}

	public String getColumnToken() {
		return columnToken;
	}

	public void setColumnToken(String columnToken) {
		this.columnToken = columnToken;
	}



}