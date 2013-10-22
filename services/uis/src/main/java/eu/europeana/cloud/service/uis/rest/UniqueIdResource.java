package eu.europeana.cloud.service.uis.rest;

import javax.ws.rs.core.Response;

/**
 * UniqueId REST API implementation
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * @since Oct 17, 2013
 */
public interface UniqueIdResource {
    /**
     * Invoke the unique identifier creation REST call HTTP call: GET
     * 
     * @param providerId
     * @param recordId
     * @return JSON/XML response with the unique Identifier or Error Message
     */
    Response createGlobalId(String providerId, String recordId);

    /**
     * Invoke the unique Identifier search according to providerId/recordId combo REST call HTTP
     * call: GET
     * 
     * @param providerId
     * @param recordId
     * @return JSON/XML response with the unique Identifier or Error Message
     */
    Response getGlobalId(String providerId, String recordId);

    /**
     * Invoke the retrieval of providerId/recordId REST call HTTP call: GET
     * 
     * @param globalId
     * @return JSON/XML response with the list of local ids organized by provider or Error Message
     */
    Response getLocalIds(String globalId);

    /**
     * Invoke the retrieval of recordId based on a providerId REST call HTTP call: GET
     * 
     * @param providerId
     * @param start
     * @param to
     * @return JSON/XML response with the list of local ids of that provider or Error Message
     */
    Response getLocalIdsByProvider(String providerId, int start, int to);

    /**
     * Invoke the retrieval of all unique identifiers for a giver provider HTTP call: GET
     * 
     * @param providerId
     * @param start
     * @param to
     * @return JSON/XML response with the list of unique ids of that provider or Error Message
     */
    Response getGlobalIdsByProvider(String providerId, int start, int to);

    /**
     * Create the mapping between an existing unique identifier and a providerId/recordId combo HTTP
     * call: GET
     * 
     * @param globalId
     * @param providerId
     * @param recordId
     * @return JSON/XML acknowledgement or Error Message
     */
    Response createIdMapping(String globalId, String providerId, String recordId);

    /**
     * Remove the mapping between a providerId/recordId and a unique identifier HTTP call: DELETE
     * 
     * @param providerId
     * @param recordId
     * @return JSON/XML acknowledgement or Error Message
     */
    Response removeIdMapping(String providerId, String recordId);

    /**
     * Remove the unique identifier and all of its mappings HTTP call: DELETE
     * 
     * @param globalId
     * @return JSON/XML acknowledgement or Error Message
     */
    Response deleteGlobalId(String globalId);
}
