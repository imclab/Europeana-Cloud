package eu.europeana.cloud.service.uis.database.dao;

import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

import eu.europeana.cloud.common.model.CloudId;
import eu.europeana.cloud.common.model.IdentifierErrorInfo;
import eu.europeana.cloud.common.model.LocalId;
import eu.europeana.cloud.service.uis.Dao;
import eu.europeana.cloud.service.uis.database.DatabaseService;
import eu.europeana.cloud.service.uis.exception.CloudIdDoesNotExistException;
import eu.europeana.cloud.service.uis.exception.DatabaseConnectionException;
import eu.europeana.cloud.service.uis.status.IdentifierErrorTemplate;

/**
 * Dao providing access to database operations on Cloud id database
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class CloudIdDao implements Dao<CloudId, List<CloudId>> {

	private String host;
	private String keyspaceName;
	private String port;
	private DatabaseService dbService;
	private PreparedStatement insertStatement;
	private PreparedStatement searchStatementNonActive;
	private PreparedStatement deleteStatement;

	/**
	 * The Cloud Id Dao
	 * 
	 * @param dbService
	 *            The service exposing the connection and session
	 */
	public CloudIdDao(DatabaseService dbService) {
		this.dbService = dbService;
		this.host = dbService.getHost();
		this.port = dbService.getPort();
		this.keyspaceName = dbService.getKeyspaceName();
		prepareStatements();
	}

	private void prepareStatements() {
		insertStatement = dbService.getSession().prepare(
				"INSERT INTO Cloud_Id(cloud_id,provider_id,record_id,deleted) VALUES(?,?,?,false)");
		insertStatement.setConsistencyLevel(dbService.getConsistencyLevel());
		searchStatementNonActive = dbService.getSession().prepare("SELECT * FROM Cloud_Id WHERE cloud_id=?");
		searchStatementNonActive.setConsistencyLevel(dbService.getConsistencyLevel());
		deleteStatement = dbService.getSession().prepare(
				"UPDATE Cloud_Id SET deleted=true WHERE cloud_Id=? AND provider_id=? AND record_id=?");
		deleteStatement.setConsistencyLevel(dbService.getConsistencyLevel());
	}

	@Override
	public List<CloudId> searchById(boolean deleted, String... args) throws DatabaseConnectionException,
			CloudIdDoesNotExistException {
		try {
			ResultSet rs = dbService.getSession().execute(searchStatementNonActive.bind(args[0]));
			if (!rs.iterator().hasNext()) {
				throw new CloudIdDoesNotExistException(new IdentifierErrorInfo(
						IdentifierErrorTemplate.CLOUDID_DOES_NOT_EXIST.getHttpCode(),
						IdentifierErrorTemplate.CLOUDID_DOES_NOT_EXIST.getErrorInfo(args[0])));
			}
			List<CloudId> cloudIds = new ArrayList<>();
			for (Row row : rs.all()) {
				if (row.getBool("deleted") == deleted) {
					CloudId cId = new CloudId();
					cId.setId(args[0]);
					LocalId lId = new LocalId();
					lId.setProviderId(row.getString("provider_Id"));
					lId.setRecordId(row.getString("record_Id"));
					cId.setLocalId(lId);
					cloudIds.add(cId);
				}
			}

			return cloudIds;
		} catch (NoHostAvailableException e) {
			throw new DatabaseConnectionException(new IdentifierErrorInfo(
					IdentifierErrorTemplate.DATABASE_CONNECTION_ERROR.getHttpCode(),
					IdentifierErrorTemplate.DATABASE_CONNECTION_ERROR.getErrorInfo(host, port, e.getMessage())));
		}
	}

	@Override
	public List<CloudId> searchActive(String... args) throws DatabaseConnectionException, CloudIdDoesNotExistException {
		return searchById(false, args[0]);
	}

	/**
	 * Search for all the Cloud Identifiers regardless if they are deleted or
	 * not
	 * 
	 * @param args
	 *            The cloudId to search on
	 * @return A list of cloudIds
	 * @throws DatabaseConnectionException
	 */
	public List<CloudId> searchAll(String args) throws DatabaseConnectionException {
		ResultSet rs = dbService.getSession().execute(searchStatementNonActive.bind(args));
		List<Row> results = rs.all();
		List<CloudId> cloudIds = new ArrayList<>();
		for (Row row : results) {
			CloudId cId = new CloudId();
			cId.setId(args);
			LocalId lId = new LocalId();
			lId.setProviderId(row.getString("provider_Id"));
			lId.setRecordId(row.getString("record_Id"));
			cId.setLocalId(lId);
			cloudIds.add(cId);
		}
		return cloudIds;
	}

	@Override
	public List<CloudId> insert(String... args) throws DatabaseConnectionException, CloudIdDoesNotExistException {
		try {
			dbService.getSession().execute(insertStatement.bind(args[0], args[1], args[2]));
		} catch (NoHostAvailableException e) {
			throw new DatabaseConnectionException(new IdentifierErrorInfo(
					IdentifierErrorTemplate.DATABASE_CONNECTION_ERROR.getHttpCode(),
					IdentifierErrorTemplate.DATABASE_CONNECTION_ERROR.getErrorInfo(host, port, e.getMessage())));
		}
		CloudId cId = new CloudId();
		LocalId lId = new LocalId();
		lId.setProviderId(args[1]);
		lId.setRecordId(args[2]);
		cId.setLocalId(lId);
		cId.setId(args[0]);
		List<CloudId> cIds = new ArrayList<CloudId>();
		cIds.add(cId);
		return cIds;
	}

	@Override
	public void delete(String... args) throws DatabaseConnectionException {
		try {
			dbService.getSession().execute(deleteStatement.bind(args[0], args[1], args[2]));
		} catch (NoHostAvailableException e) {
			throw new DatabaseConnectionException(new IdentifierErrorInfo(
					IdentifierErrorTemplate.DATABASE_CONNECTION_ERROR.getHttpCode(),
					IdentifierErrorTemplate.DATABASE_CONNECTION_ERROR.getErrorInfo(host, port, e.getMessage())));
		}
	}

	@Override
	public void update(String... obj) throws DatabaseConnectionException {
		throw new UnsupportedOperationException("This method is not implemented for the Cloud Id");
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public String getKeyspace() {
		return keyspaceName;
	}

	@Override
	public String getPort() {
		return this.port;
	}

}
