package eu.europeana.cloud.service.dls.listeners;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.europeana.cloud.common.web.ParamConstants;
import eu.europeana.cloud.service.dls.solr.SolrDAO;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ listener that processes messages about removing a
 * {@link eu.europeana.cloud.common.model.Representation representation} of
 * certain representation name in all versions from a
 * {@link eu.europeana.cloud.common.model.Record record} of certain cloudId.
 *
 * It receives messages with <code>records.representations.delete</code> routing
 * key. Message text should be Json including a property containing
 * representation name and a property containing cloudId of the
 * {@link eu.europeana.cloud.common.model.Record record}.
 *
 * After receiving properly formed message, listener calls
 * {@link eu.europeana.cloud.service.dls.solr.SolrDAO#removeRepresentation(String, String)}
 * so that Solr index is updated
 * ({@link eu.europeana.cloud.common.model.Record record} will hold no versions
 * of this {@link eu.europeana.cloud.common.model.Representation representation}
 * in updated index).
 *
 * If message is malformed, information about error is logged and no call to
 * {@link eu.europeana.cloud.service.dls.solr.SolrDAO} is performed. If call to
 * {@link eu.europeana.cloud.service.dls.solr.SolrDAO} fails, an information is
 * also logged.
 *
 * Messages for this listener are produced by
 * <code>eu.europeana.cloud.service.mcs.persistent.SolrRepresentationIndexer.removeRepresentation(String, String)}</code>
 * method in MCS.
 */
@Component
public class RepresentationRemovedListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationRemovedListener.class);

    @Autowired
    SolrDAO solrDao;

    private final Gson gson = new Gson();

    @Override
    public void onMessage(Message message) {

        byte[] messageBytes = message.getBody();
        if (messageBytes == null) {
            LOGGER.error("Message has null body.");
            return;
        }

        String messageText = new String(message.getBody());
        if (messageText.isEmpty()) {
            LOGGER.error("Message has empty body.");
            return;
        }

        JsonObject jo = gson.fromJson(messageText, JsonElement.class).getAsJsonObject();

        String cloudId = null;
        JsonElement cloudIdJson = jo.get(ParamConstants.P_CLOUDID);
        if (cloudIdJson != null && !cloudIdJson.isJsonNull()) {
            cloudId = cloudIdJson.getAsString();
        }
        if (StringUtils.isBlank(cloudId)) {
            LOGGER.error("Required parameter version is empty.");
            return;
        }

        String representationName = null;
        JsonElement representationNameJson = jo.get(ParamConstants.P_REPRESENTATIONNAME);
        if (representationNameJson != null && !representationNameJson.isJsonNull()) {
            representationName = representationNameJson.getAsString();
        }
        if (StringUtils.isBlank(representationName)) {
            LOGGER.error("Required parameter representationName is empty.");
            return;
        }

        try {
            solrDao.removeRepresentation(cloudId, representationName);
        } catch (SolrServerException | IOException ex) {
            LOGGER.error("Cannot remove representation from solr.", ex);
        }
    }
}
