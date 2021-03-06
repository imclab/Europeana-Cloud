package eu.europeana.cloud.service.dls.listeners;

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
 * {@link eu.europeana.cloud.common.model.Representation representation} version
 * from a certain {@link eu.europeana.cloud.common.model.Record record}.
 *
 * It receives messages with
 * <code>records.representations.versions.deleteVersion</code> routing key.
 * Message text should be a representation version (directly sent value).
 * Representation version id uniquely identifies
 * {@link Representation representation} in the whole system, so it is know from
 * which {@link eu.europeana.cloud.common.model.Record record} to remove it.
 *
 * After receiving properly formed message, listener calls
 * {@link eu.europeana.cloud.service.dls.solr.SolrDAO#removeRepresentationVersion(String)}
 * so that Solr index is updated
 * ({@link eu.europeana.cloud.common.model.Record record} will not hold this
 * representation version in updated index).
 *
 * If message is malformed, information about error is logged and no call to
 * {@link eu.europeana.cloud.service.dls.solr.SolrDAO} is performed. If call to
 * {@link eu.europeana.cloud.service.dls.solr.SolrDAO} fails, an information is
 * also logged.
 *
 * Messages for this listener are produced by
 * <code>eu.europeana.cloud.service.mcs.persistent.SolrRepresentationIndexer.removeRepresentationVersion(String)}</code>
 * method in MCS.
 */
@Component
public class RepresentationVersionRemovedListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationVersionRemovedListener.class);

    @Autowired
    SolrDAO solrDao;

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

        String versionId = messageText;

        if (StringUtils.isBlank(versionId)) {
            LOGGER.error("Required parameter version id is empty.");
            return;
        }

        try {
            solrDao.removeRepresentationVersion(versionId);
        } catch (SolrServerException | IOException ex) {
            LOGGER.error("Cannot remove representation from solr.", ex);
        }

    }
}
