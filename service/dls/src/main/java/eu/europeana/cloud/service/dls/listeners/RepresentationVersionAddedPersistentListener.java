package eu.europeana.cloud.service.dls.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import eu.europeana.cloud.common.model.Representation;
import eu.europeana.cloud.common.web.ParamConstants;
import eu.europeana.cloud.common.model.CompoundDataSetId;
import eu.europeana.cloud.service.dls.solr.SolrDAO;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ listener that processes messages about adding a new persistent
 * {@link eu.europeana.cloud.common.model.Representation representation} version
 * to a certain {@link eu.europeana.cloud.common.model.Record record}.
 *
 * It receives messages with
 * <code>records.representations.versions.addPersistent</code> routing key.
 * Message text should be a map serialised to Json, holding
 * {@link Representation} object, and a list of {@link CompoundDataSetId}
 * objects. The list of ids is needed, if there are some
 * {@link eu.europeana.cloud.common.model.DataSet data sets} that have
 * representation name assigned <i>as a whole</i>. In such case
 * {@link eu.europeana.cloud.common.model.DataSet data set} holds a pointer to
 * latest persistent version. So if a new version is persisted, the pointer
 * might need to be updated (not necessarily, because <i>latest</i>
 * property is attributed based on
 * {@link eu.europeana.cloud.common.model.Representation representation}
 * creation time). The list of ids can be empty, but cannot be <code>null</code>.
 *
 * After receiving properly formed message, listener calls
 * {@link eu.europeana.cloud.service.dls.solr.SolrDAO#insertRepresentation(Representation, Collection) SolrDAO.insertRepresentation(Representation, Collection&lt;CompoundDataSetId&gt;)}
 * so that Solr index is updated (the
 * {@link eu.europeana.cloud.common.model.Representation representation} is
 * marked as persistent and
 * {@link eu.europeana.cloud.common.model.DataSet data sets} point to this
 * {@link eu.europeana.cloud.common.model.Representation representation} if
 * appropriate).
 *
 * If message is malformed, information about error is logged and no call to
 * {@link eu.europeana.cloud.service.dls.solr.SolrDAO} is performed. If call to
 * {@link eu.europeana.cloud.service.dls.solr.SolrDAO} fails, an information is
 * also logged.
 *
 * Messages for this listener are produced by
 * <code>eu.europeana.cloud.service.mcs.persistent.SolrRepresentationIndexer.insertRepresentation(Representation, Collection&lt;CompoundDataSetId&gt;)</code>
 * method in MCS.
 */
@Component
public class RepresentationVersionAddedPersistentListener implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationVersionAddedPersistentListener.class);
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
            .create();

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

        JsonElement jsonElem = gson.fromJson(messageText, JsonElement.class);
        if (jsonElem == null || jsonElem.isJsonNull()) {
            LOGGER.error("Received message with null parameters map.");
            return;
        }
        JsonObject jsonObject = jsonElem.getAsJsonObject();

        JsonElement jsonRepresentation = jsonObject.get(ParamConstants.F_REPRESENTATION);
        Representation representation = gson.fromJson(jsonRepresentation, Representation.class);
        if (representation == null) {
            LOGGER.error("Received representation is null.");
            return;
        }

        Type dataSetIdsType = new TypeToken<Collection<CompoundDataSetId>>() {
        }.getType();
        JsonElement jsonDataSetIds = jsonObject.get(ParamConstants.F_DATASETS);
        Collection<CompoundDataSetId> dataSetIds = gson.fromJson(jsonDataSetIds, dataSetIdsType);
        if (dataSetIds == null) {
            LOGGER.error("Received data set ids list is null.");
            return;
        }

        try {
            solrDao.insertRepresentation(representation, dataSetIds);
        } catch (IOException | SolrServerException ex) {
            LOGGER.error("Cannot insert persistent representation into solr.", ex);
        }
    }
}
