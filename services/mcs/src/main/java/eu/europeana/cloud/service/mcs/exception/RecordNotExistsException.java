package eu.europeana.cloud.service.mcs.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * RecordNotExistsException
 */
public class RecordNotExistsException extends RuntimeException {

    public RecordNotExistsException() {
    }


    public RecordNotExistsException(String recordId) {
        super("There is no record with provided global id: " + recordId);
    }
}