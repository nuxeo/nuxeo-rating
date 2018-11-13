/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Arnaud Kervern
 */
package org.nuxeo.ecm.rating.listener;

import static org.nuxeo.ecm.activity.ActivityHelper.createDocumentActivityObject;
import static org.nuxeo.ecm.core.api.LifeCycleConstants.DELETED_STATE;
import static org.nuxeo.ecm.core.api.LifeCycleConstants.TRANSITION_EVENT;
import static org.nuxeo.ecm.core.api.LifeCycleConstants.TRANSTION_EVENT_OPTION_TO;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_REMOVED;
import static org.nuxeo.ecm.core.api.trash.TrashService.DOCUMENT_TRASHED;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.rating.api.RatingService;
import org.nuxeo.runtime.api.Framework;

/**
 * Remove rates made when a document is removed or his lifecycle state is changing to DELETED_STATE
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
public class RatingListener implements EventListener {
    @Override
    public void handleEvent(Event event) {
        if (event.getContext() instanceof DocumentEventContext) {
            DocumentEventContext ctx = (DocumentEventContext) event.getContext();
            DocumentModel document = ctx.getSourceDocument();

            if (DOCUMENT_REMOVED.equals(event.getName())) {
                cancelRates(document);
            } else if (DOCUMENT_TRASHED.equals(event.getName())) {
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
        RatingService ratingService = Framework.getService(RatingService.class);
        ratingService.cancelRates(createDocumentActivityObject(document), null);
    }
}
