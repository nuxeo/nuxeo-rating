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

import org.junit.Test;
import org.junit.runner.RunWith;
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
    }
}
