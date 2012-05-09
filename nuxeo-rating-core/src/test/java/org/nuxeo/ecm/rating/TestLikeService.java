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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.activity.ActivityHelper.createDocumentActivityObject;
import static org.nuxeo.ecm.rating.LikeServiceImpl.LIKE_RATING;
import static org.nuxeo.ecm.rating.api.Constants.LIKE_ASPECT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.ActivitiesListImpl;
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.rating.api.LikeStatus;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@RunWith(FeaturesRunner.class)
@Features({ CoreFeature.class })
@RepositoryConfig(repositoryName = "default", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.core.persistence", "org.nuxeo.ecm.activity",
        "org.nuxeo.ecm.rating.api", "org.nuxeo.ecm.rating.core" })
@LocalDeploy("org.nuxeo.ecm.rating.core:rating-test.xml")
public class TestLikeService extends AbstractRatingTest {

    @Test
    public void serviceRegistration() throws IOException {
        assertNotNull(likeService);
    }

    @Test
    public void differentUsersCanLikeADocument() throws ClientException {
        DocumentModel doc = createTestDocument("file1");

        likeService.like("bender", doc);
        likeService.like("leela", doc);
        likeService.like("fry", doc);

        assertTrue(likeService.hasUserLiked("bender", doc));
        assertTrue(likeService.hasUserLiked("leela", doc));
        assertTrue(likeService.hasUserLiked("fry", doc));
        assertFalse(likeService.hasUserDisliked("bender", doc));
        assertFalse(likeService.hasUserDisliked("leela", doc));
        assertFalse(likeService.hasUserDisliked("fry", doc));

        long likesCount = likeService.getLikesCount(doc);
        assertEquals(3, likesCount);

        long dislikesCount = likeService.getDislikesCount(doc);
        assertEquals(0, dislikesCount);
    }

    @Test
    public void differentUsersCanDislikeADocument() throws ClientException {
        DocumentModel doc = createTestDocument("file1");

        likeService.dislike("bender", doc);
        likeService.dislike("leela", doc);
        likeService.dislike("fry", doc);

        assertFalse(likeService.hasUserLiked("bender", doc));
        assertFalse(likeService.hasUserLiked("leela", doc));
        assertFalse(likeService.hasUserLiked("fry", doc));
        assertTrue(likeService.hasUserDisliked("bender", doc));
        assertTrue(likeService.hasUserDisliked("leela", doc));
        assertTrue(likeService.hasUserDisliked("fry", doc));

        long likesCount = likeService.getLikesCount(doc);
        assertEquals(0, likesCount);

        long dislikesCount = likeService.getDislikesCount(doc);
        assertEquals(3, dislikesCount);
    }

    @Test
    public void sameUserCannotLikeAndDislikeAtTheSameTime()
            throws ClientException {
        DocumentModel doc = createTestDocument("file1");

        likeService.like("bender", doc);
        assertTrue(likeService.hasUserLiked("bender", doc));
        assertFalse(likeService.hasUserDisliked("bender", doc));

        likeService.dislike("bender", doc);
        assertFalse(likeService.hasUserLiked("bender", doc));
        assertTrue(likeService.hasUserDisliked("bender", doc));

        long likesCount = likeService.getLikesCount(doc);
        assertEquals(0, likesCount);

        long dislikesCount = likeService.getDislikesCount(doc);
        assertEquals(1, dislikesCount);
    }

    @Test
    public void shouldCancelLikeOrDislike() throws ClientException {
        DocumentModel doc = createTestDocument("file1");

        likeService.like("bender", doc);
        assertTrue(likeService.hasUserLiked("bender", doc));
        assertFalse(likeService.hasUserDisliked("bender", doc));
        long likesCount = likeService.getLikesCount(doc);
        assertEquals(1, likesCount);

        likeService.cancel("bender", doc);
        assertFalse(likeService.hasUserLiked("bender", doc));
        assertFalse(likeService.hasUserDisliked("bender", doc));
        likesCount = likeService.getLikesCount(doc);
        assertEquals(0, likesCount);
    }

    @Test
    public void shouldGetLikeStatusForUser() throws ClientException {
        DocumentModel doc = createTestDocument("file1");

        likeService.like("bender", doc);
        likeService.like("leela", doc);
        likeService.dislike("fry", doc);

        LikeStatus likeStatus = likeService.getLikeStatus("bender", doc);
        assertEquals(2, likeStatus.likesCount);
        assertEquals(1, likeStatus.dislikesCount);
        assertEquals("bender", likeStatus.username);
        assertEquals(LikeStatus.LIKED, likeStatus.userLikeStatus);

        likeStatus = likeService.getLikeStatus("leela", doc);
        assertEquals(2, likeStatus.likesCount);
        assertEquals(1, likeStatus.dislikesCount);
        assertEquals("leela", likeStatus.username);
        assertEquals(LikeStatus.LIKED, likeStatus.userLikeStatus);

        likeStatus = likeService.getLikeStatus("fry", doc);
        assertEquals(2, likeStatus.likesCount);
        assertEquals(1, likeStatus.dislikesCount);
        assertEquals("fry", likeStatus.username);
        assertEquals(LikeStatus.DISLIKED, likeStatus.userLikeStatus);

        likeStatus = likeService.getLikeStatus("zapp", doc);
        assertEquals(2, likeStatus.likesCount);
        assertEquals(1, likeStatus.dislikesCount);
        assertEquals("zapp", likeStatus.username);
        assertEquals(LikeStatus.UNKNOWN, likeStatus.userLikeStatus);
    }

    @Test
    public void shouldGetGlobalLikeStatus() throws ClientException {
        DocumentModel doc = createTestDocument("file1");

        likeService.like("bender", doc);
        likeService.like("leela", doc);
        likeService.dislike("fry", doc);

        LikeStatus likeStatus = likeService.getLikeStatus(doc);
        assertEquals(2, likeStatus.likesCount);
        assertEquals(1, likeStatus.dislikesCount);
        assertNull(likeStatus.username);
        assertEquals(LikeStatus.UNKNOWN, likeStatus.userLikeStatus);
    }

    @Test
    public void shouldHandleSuperSpaceCount() throws ClientException {
        initWithDefaultRepository();

        DocumentModel folder = session.createDocumentModel(
                "/default-domain/workspaces/", "folder", "Folder");
        folder = session.createDocument(folder);
        session.save();

        DocumentModel testDoc = createTestDocument("myDoc",
                "/default-domain/workspaces/folder");
        DocumentModel defaultDomain = session.getDocument(new PathRef(
                "/default-domain"));
        DocumentModel workspaces = session.getDocument(new PathRef(
                "/default-domain/workspaces"));

        likeService.like("robin", testDoc);
        likeService.like("barney", testDoc);
        likeService.dislike("Ted", testDoc);

        assertEquals(3, ratingService.getRatesCount(
                createDocumentActivityObject(testDoc), LIKE_ASPECT));

        // Only on SuperSpace
        assertEquals(
                0,
                ratingService.getRatedChildren(
                        createDocumentActivityObject(folder), LIKE_RATING,
                        LIKE_ASPECT).size());

        assertEquals(
                2,
                ratingService.getRatedChildren(
                        createDocumentActivityObject(defaultDomain),
                        LIKE_RATING, LIKE_ASPECT).size());
        assertEquals(
                2,
                ratingService.getRatedChildren(
                        createDocumentActivityObject(workspaces), LIKE_RATING,
                        LIKE_ASPECT).size());

        // All activities correctly removed
        likeService.cancel("robin", testDoc);
        assertEquals(2, ratingService.getRatesCount(
                createDocumentActivityObject(testDoc), LIKE_ASPECT));
        assertEquals(
                1,
                ratingService.getRatedChildren(
                        createDocumentActivityObject(defaultDomain),
                        LIKE_RATING, LIKE_ASPECT).size());
    }

    @Test
    public void shouldCorrectlyReturnedMostLikedDocumentList()
            throws ClientException {
        initWithDefaultRepository();

        DocumentModel workspaces = session.getDocument(new PathRef(
                "/default-domain/workspaces"));

        DocumentModel doc1 = createTestDocument("tmp",
                "/default-domain/workspaces/test");
        DocumentModel doc2 = createTestDocument("tmp1",
                "/default-domain/workspaces/test");
        DocumentModel doc3 = createTestDocument("tmp2",
                "/default-domain/workspaces/test");
        DocumentModel doc4 = createTestDocument("tmp3",
                "/default-domain/workspaces/test");

        likeService.like("robin", doc3); // 3-1
        likeService.like("robin", doc4); // 4-1

        likeService.like("barney", doc1); // 1-1
        likeService.like("barney", doc3); // 3-2
        likeService.like("barney", doc4); // 4-2

        likeService.like("ted", doc2); // 2-1

        likeService.like("marshal", doc3); // 3-3
        likeService.like("marshal", doc4); // 4-3

        likeService.like("lily", doc3); // 3-4

        session.save();

        // order should be: doc3 - doc4 - doc1 - doc2
        Map<DocumentModel, Integer> moreLikedDocuments = likeService.getMostLikedDocuments(
                session, 10, workspaces);
        assertEquals(4, moreLikedDocuments.size());

        List<DocumentModel> docs = new ArrayList<DocumentModel>(
                moreLikedDocuments.keySet());
        assertEquals(doc3.getName(), docs.get(0).getName());
        assertEquals(new Integer(4), moreLikedDocuments.get(docs.get(0)));
        assertEquals(doc4.getName(), docs.get(1).getName());
        assertEquals(new Integer(3), moreLikedDocuments.get(docs.get(1)));

        // doc1 and doc2 may be inverted
        assertEquals(new Integer(1), moreLikedDocuments.get(docs.get(2)));
        assertEquals(new Integer(1), moreLikedDocuments.get(docs.get(3)));

        moreLikedDocuments = likeService.getMostLikedDocuments(session, 2,
                workspaces);
        assertEquals(2, moreLikedDocuments.size());
    }

    @Test
    public void shouldHaveACorrectSortingOrder() {
        ActivitiesList activities = new ActivitiesListImpl();
        addDummyActivities(activities, "doc1", 10);
        addDummyActivities(activities, "doc2", 5);
        addDummyActivities(activities, "doc3", 11);
        addDummyActivities(activities, "doc4", 1);
        addDummyActivities(activities, "doc5", 20);

        LikeServiceImpl.DocIdWithRate[] rated = ((LikeServiceImpl) likeService).getSortedDocIdRated(
                activities).toArray(new LikeServiceImpl.DocIdWithRate[5]);
        // Check ordering is correct
        assertEquals("doc5", rated[0].docId);
        assertEquals(20, rated[0].rate);
        assertEquals("doc3", rated[1].docId);
        assertEquals("doc1", rated[2].docId);
        assertEquals(10, rated[2].rate);
        assertEquals("doc2", rated[3].docId);
        assertEquals("doc4", rated[4].docId);
    }

    protected void addDummyActivities(ActivitiesList activities, String docId,
            int nb) {
        for (int i = 0; i < nb; i++) {
            ActivityBuilder ab = new ActivityBuilder().target(
                    ActivityHelper.createDocumentActivityObject("default",
                            docId)).object("1");
            activities.add(ab.build());
        }
    }
}
