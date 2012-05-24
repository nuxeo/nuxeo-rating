package org.nuxeo.ecm.rating.listener;

import static org.nuxeo.ecm.activity.ActivityHelper.createDocumentActivityObject;
import static org.nuxeo.ecm.core.api.LifeCycleConstants.DELETED_STATE;
import static org.nuxeo.ecm.core.api.LifeCycleConstants.TRANSITION_EVENT;
import static org.nuxeo.ecm.core.api.LifeCycleConstants.TRANSTION_EVENT_OPTION_TO;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_REMOVED;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.rating.api.RatingService;
import org.nuxeo.runtime.api.Framework;

/**
 * Remove rates made when a document is removed or his lifecycle state is
 * changing to DELETED_STATE
 * 
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class RatingListener implements EventListener {
    @Override
    public void handleEvent(Event event) throws ClientException {
        if (event.getContext() instanceof DocumentEventContext) {
            DocumentEventContext ctx = (DocumentEventContext) event.getContext();
            DocumentModel document = ctx.getSourceDocument();

            if (DOCUMENT_REMOVED.equals(event.getName())) {
                cancelRates(document);
            } else if (TRANSITION_EVENT.equals(event.getName())) {
                String destinationState = (String) ctx.getProperty(TRANSTION_EVENT_OPTION_TO);
                if (DELETED_STATE.equals(destinationState)) {
                    cancelRates(document);
                }
            }
        }
    }

    protected void cancelRates(DocumentModel document) {
        RatingService ratingService = Framework.getLocalService(RatingService.class);
        ratingService.cancelRates(createDocumentActivityObject(document), null);
    }
}
