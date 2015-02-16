/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
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
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.EventServiceAdmin;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.test.RepositorySettings;
import org.nuxeo.ecm.rating.api.LikeService;
import org.nuxeo.ecm.rating.api.RatingService;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public abstract class AbstractRatingTest {

    @Inject
    protected RepositorySettings settings;

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
    public void cleanupDatabase() throws ClientException {
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

    protected DocumentModel createTestDocument(String name) throws ClientException {
        return createTestDocument(name, "/");
    }

    protected DocumentModel createTestDocument(String name, String path) throws ClientException {
        DocumentModel doc = session.createDocumentModel(path, name, "File");
        doc.setProperty("dublincore", "title", name);
        doc = session.createDocument(doc);
        session.saveDocument(doc);
        session.save();
        return doc;
    }

    protected CoreSession openSessionAs(String username) throws ClientException {
        return settings.openSessionAs(username);
    }

}
