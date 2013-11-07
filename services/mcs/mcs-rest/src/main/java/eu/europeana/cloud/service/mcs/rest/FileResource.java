package eu.europeana.cloud.service.mcs.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.europeana.cloud.common.model.File;
import eu.europeana.cloud.common.model.Representation;
import eu.europeana.cloud.service.mcs.exception.FileNotExistsException;
import eu.europeana.cloud.service.mcs.exception.RecordNotExistsException;
import eu.europeana.cloud.service.mcs.exception.RepresentationNotExistsException;
import eu.europeana.cloud.service.mcs.exception.VersionNotExistsException;
import eu.europeana.cloud.service.mcs.RecordService;
import eu.europeana.cloud.service.mcs.exception.WrongContentRangeException;
import static eu.europeana.cloud.service.mcs.rest.ParamConstants.*;

/**
 * FilesResource
 */
@Path("/records/{" + P_GID + "}/representations/{" + P_SCHEMA + "}/versions/{" + P_VER + "}/files/{" + P_FILE + "}")
@Component
public class FileResource {

    @Autowired
    private RecordService recordService;

    @Context
    private UriInfo uriInfo;

    @Context
    private Request request;

    @PathParam(P_GID)
    private String globalId;

    @PathParam(P_SCHEMA)
    private String schema;

    @PathParam(P_VER)
    private String version;

    @PathParam(P_FILE)
    private String fileName;

    static final String HEADER_RANGE = "Range";


    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response sendFile(
            @FormDataParam(F_FILE_MIME) String mimeType,
            @FormDataParam(F_FILE_DATA) InputStream data)
            throws IOException, RecordNotExistsException, RepresentationNotExistsException, VersionNotExistsException {
        ParamUtil.require(F_FILE_DATA, data);

        File f = new File();
        f.setMimeType(mimeType);
        f.setFileName(fileName);

        boolean isCreateOperation = recordService.putContent(globalId, schema, version, f, data);
        EnrichUriUtil.enrich(uriInfo, globalId, schema, version, f);

        Response.Status operationStatus = isCreateOperation ? Response.Status.CREATED : Response.Status.NO_CONTENT;
        return Response.status(operationStatus).location(f.getContentUri()).tag(f.getMd5()).build();
    }


    /**
     * Use this method to retrieve content of a file which is a part of a specified record representation. 
     * Basic support for retrieving only a part of content is implemented using HTTP Range header:
     * <ul>
     * <li><b>Range: bytes=10-15</b> - retrieve bytes from 10 to 15 of content
     * <li><b>Range: bytes=10-</b> - skip 10 first bytes of content
     * </ul>
     * 
     * @param range
     * @return
     * @throws RecordNotExistsException
     * @throws RepresentationNotExistsException
     * @throws VersionNotExistsException
     * @throws FileNotExistsException 
     */
    @GET
    public Response getFile(
            @HeaderParam(HEADER_RANGE) String range)
            throws RecordNotExistsException, RepresentationNotExistsException, VersionNotExistsException, FileNotExistsException {
        // extract range
        final ContentRange contentRange;
        if (range == null) {
            contentRange = new ContentRange(-1L, -1L);
        } else {
            contentRange = ContentRange.parse(range);
        }

        // TODO: this is some kind of logic here, we do not want this
        String md5 = null;
        Response.Status status;
        if (contentRange.isSpecified()) {
            status = Response.Status.PARTIAL_CONTENT;
        } else {
            status = Response.Status.OK;
            final Representation rep = recordService.getRepresentation(globalId, schema, version);
            final File requestedFile = getByName(fileName, rep);

            if (requestedFile == null) {
                throw new FileNotExistsException();
            }
            md5 = requestedFile.getMd5();
        }

        StreamingOutput output = new StreamingOutput() {

            @Override
            public void write(OutputStream output)
                    throws IOException, WebApplicationException {
                recordService.getContent(globalId, schema, version, fileName, contentRange.start, contentRange.end, output);
            }
        };

        return Response.status(status).entity(output).tag(md5).build();
    }


    private File getByName(String fileName, Representation rep) {
        for (File f : rep.getFiles()) {
            if (f.getFileName().equals(fileName)) {
                return f;
            }
        }
        return null;
    }


    @DELETE
    public Response deleteFile() {
        recordService.deleteContent(globalId, schema, version, fileName);
        return Response.noContent().build();
    }

    static class ContentRange {

        long start, end;

        private static final Pattern bytesPattern = Pattern.compile("bytes=(?<start>\\d+)[-](?<end>\\d*)");


        ContentRange(long start, long end) {
            this.start = start;
            this.end = end;
        }


        boolean isSpecified() {
            return start != -1 || end != -1;
        }


        static ContentRange parse(String range) {
            long start, end;
            if (range == null) {
                throw new IllegalArgumentException("Range should not be null");
            }
            Matcher rangeMatcher = bytesPattern.matcher(range);
            if (rangeMatcher.matches()) {
                try {
                    start = Long.parseLong(rangeMatcher.group("start"));
                    String endString = rangeMatcher.group("end");
                    end = endString.isEmpty() ? -1 : Long.parseLong(endString);
                } catch (NumberFormatException ex) {
                    throw new WrongContentRangeException("Cannot parse range: " + ex.getMessage());
                }
            } else {
                throw new WrongContentRangeException("Expected range header format is: " + bytesPattern.pattern());
            }
            
            if (end != -1 && end < start) {
                throw new WrongContentRangeException("Range end must not be smaller than range start");
            }

            return new ContentRange(start, end);
        }
    }
}