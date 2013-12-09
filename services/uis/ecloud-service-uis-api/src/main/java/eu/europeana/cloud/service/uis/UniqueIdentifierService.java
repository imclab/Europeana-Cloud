package eu.europeana.cloud.service.uis;

import java.util.List;

import eu.europeana.cloud.common.model.CloudId;
import eu.europeana.cloud.common.model.LocalId;
import eu.europeana.cloud.exceptions.DatabaseConnectionException;
import eu.europeana.cloud.exceptions.CloudIdDoesNotExistException;
import eu.europeana.cloud.exceptions.IdHasBeenMappedException;
import eu.europeana.cloud.exceptions.ProviderDoesNotExistException;
import eu.europeana.cloud.exceptions.RecordDatasetEmptyException;
import eu.europeana.cloud.exceptions.RecordDoesNotExistException;
import eu.europeana.cloud.exceptions.RecordExistsException;
import eu.europeana.cloud.exceptions.RecordIdDoesNotExistException;

/**
 * Unique Identifier Service Interface This service specifies the available methods for the
 * successful generation and linking of records with a Cloud eCloud Identifier
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * @since Oct 17, 2013
 */
public interface UniqueIdentifierService {
    /**
     * Create a Unique Identifier from the given providerId and recordId
     * 
     * @param recordInfo providerId and optionally recordId
     * @return The unique identifier for this record
     * @throws DatabaseConnectionException
     * @throws RecordExistsException
     */
    CloudId createCloudId(String... recordInfo) throws DatabaseConnectionException,
            RecordExistsException;

    /**
     * Search for a unique identifier based on the providerId and recordId
     * 
     * @param providerId
     * @param recordId
     * @return The unique identifier of the record
     * @throws DatabaseConnectionException
     * @throws RecordDoesNotExistException
     */
    CloudId getCloudId(String providerId, String recordId) throws DatabaseConnectionException,
            RecordDoesNotExistException;

    /**
     * Search all the records that are linked to a unique identifier
     * 
     * @param cloudId
     * @return A list of providerIds with the records that have been linked to the unique identifier
     *         provided
     * @throws DatabaseConnectionException
     * @throws CloudIdDoesNotExistException
     */
    List<LocalId> getLocalIdsByCloudId(String cloudId) throws DatabaseConnectionException,
            CloudIdDoesNotExistException;

    /**
     * Retrieve the recordIds for a given provider, supporting pagination. If no pagination is
     * provided then the recordIds retrieved are 10000 starting from record 0
     * 
     * @param providerId
     * @param start
     * @param end
     * @return A list of recordIds for a provider bound to 10000 results
     * @throws DatabaseConnectionException
     * @throws ProviderDoesNotExistException
     */
    List<LocalId> getLocalIdsByProvider(String providerId, String start, int end)
            throws DatabaseConnectionException, ProviderDoesNotExistException;

    /**
     * Retrieve the cloudIds for a given provider, supporting pagination. If no pagination is
     * provided then the cloudIds retrieved are 10000 starting from record 0
     * 
     * @param providerId
     * @param start
     * @param end
     * @return A list of cloudIds for a provider bound to 10000 results
     * @throws DatabaseConnectionException
     * @throws ProviderDoesNotExistException
     * @throws RecordDatasetEmptyException
     */
    List<CloudId> getCloudIdsByProvider(String providerId, String start, int end)
            throws DatabaseConnectionException, ProviderDoesNotExistException,
            RecordDatasetEmptyException;

    /**
     * Create a mapping between a new providerId and recordId and an existing cloud identifier
     * 
     * @param cloudId
     * @param providerId
     * @param recordId
     * @throws DatabaseConnectionException
     * @throws CloudIdDoesNotExistException
     * @throws IdHasBeenMappedException
     */
    void createIdMapping(String cloudId, String providerId, String recordId)
            throws DatabaseConnectionException, CloudIdDoesNotExistException, IdHasBeenMappedException,ProviderDoesNotExistException;

    /**
     * Remove the mapping between the providerId/recordId and the cloud identifier The mapping is
     * soft-deleted
     * 
     * @param providerId
     * @param recordId
     * @throws DatabaseConnectionException
     * @throws ProviderDoesNotExistException
     * @throws RecordIdDoesNotExistException
     */
    void removeIdMapping(String providerId, String recordId) throws DatabaseConnectionException,
            ProviderDoesNotExistException, RecordIdDoesNotExistException;

    /**
     * Delete a cloud Identifier and all of its relevant mappings. Everything is soft-deleted
     * 
     * @param cloudId
     * @throws DatabaseConnectionException
     * @throws CloudIdDoesNotExistException
     */
    void deleteCloudId(String cloudId) throws DatabaseConnectionException,
            CloudIdDoesNotExistException;
    
    /**
     * Expose information about the database host entry;
     * @return The host IP
     */
    String getHost();
    
    /**
     * Expose information about the keyspaceName
     * @return The keyspace name
     */
    String getKeyspace();
    
    /**
     * Expose the port of the database
     * @return The database port
     */
    String getPort();
    
}
