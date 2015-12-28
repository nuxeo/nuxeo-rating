/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.rating;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityImpl;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.EventServiceAdmin;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.rating.api.LikeService;
import org.nuxeo.ecm.rating.api.RatingService;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public abstract class AbstractRatingTest {

    @Inject
    protected ActivityStreamService activityStreamService;

    @Inject
    protected RatingService ratingService;

    @Inject
    protected LikeService likeService;

    @Inject
    protected CoreSession session;

    @Inject
    protected EventService eventService;

    @Inject
    protected EventServiceAdmin eventServiceAdmin;

    protected String activityObject;

    protected Activity activity;

    @Before
    public void disableActivityStreamListener() {
        eventServiceAdmin.setListenerEnabledFlag("activityStreamListener", false);
    }

    @Before
    public void cleanupDatabase() {
        ((ActivityStreamServiceImpl) activityStreamService).getOrCreatePersistenceProvider().run(Boolean.TRUE,
                new PersistenceProvider.RunVoid() {
                    public void runWith(EntityManager em) {
                        Query query = em.createQuery("delete from Activity");
                        query.executeUpdate();
                    }
                });
        activity = activityStreamService.addActivity(new ActivityImpl());
        activityObject = ActivityHelper.createActivityObject(activity);
    }

    protected DocumentModel createTestDocument(String name) {
        return createTestDocument(name, "/");
    }

    protected DocumentModel createTestDocument(String name, String path) {
        DocumentModel doc = session.createDocumentModel(path, name, "File");
        doc.setProperty("dublincore", "title", name);
        doc = session.createDocument(doc);
        session.saveDocument(doc);
        session.save();
        return doc;
    }

}
