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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityImpl;
import org.nuxeo.ecm.activity.ActivityReply;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.rating.api.LikeStatus;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@RunWith(FeaturesRunner.class)
@Features({ RatingFeature.class })
public class TestLikeService extends AbstractRatingTest {

    @Test
    public void serviceRegistration() throws IOException {
        assertNotNull(likeService);
    }

    @Test
    public void differentUsersCanLikeADocument() {
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
    public void differentUsersCanDislikeADocument() {
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
    public void sameUserCannotLikeAndDislikeAtTheSameTime() {
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
    public void shouldCancelLikeOrDislike() {
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
    public void shouldGetLikeStatusForUser() {
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
    public void shouldGetGlobalLikeStatus() {
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
    public void shouldRemoveLikeOnRemovedActivity() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activity = activityStreamService.addActivity(activity);

        String activityObject = ActivityHelper.createActivityObject(activity);
        likeService.like("bender", activityObject);

        LikeStatus likeStatus = likeService.getLikeStatus(activityObject);
        assertEquals(1, likeStatus.likesCount);

        activityStreamService.removeActivities(Collections.singleton(activity));
        likeStatus = likeService.getLikeStatus(activityObject);
        assertEquals(0, likeStatus.likesCount);
    }

    @Test
    public void shouldRemoveLikeOnRemovedActivityAndReplies() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activity = activityStreamService.addActivity(activity);

        String activityObject = ActivityHelper.createActivityObject(activity);
        likeService.like("bender", activityObject);

        LikeStatus likeStatus = likeService.getLikeStatus(activityObject);
        assertEquals(1, likeStatus.likesCount);

        long replyPublishedDate = new Date().getTime();
        ActivityReply reply = new ActivityReply("bender", "Bender", "First reply", replyPublishedDate);
        reply = activityStreamService.addActivityReply(activity.getId(), reply);
        assertEquals(activity.getId() + "-reply-1", reply.getId());

        String activityReplyObject = ActivityHelper.createActivityObject(reply.getId());
        likeService.like("bender", activityReplyObject);

        likeStatus = likeService.getLikeStatus(activityReplyObject);
        assertEquals(1, likeStatus.likesCount);

        activity = activityStreamService.getActivity(activity.getId());
        activityStreamService.removeActivities(Collections.singleton(activity));
        likeStatus = likeService.getLikeStatus(activityObject);
        assertEquals(0, likeStatus.likesCount);

        likeStatus = likeService.getLikeStatus(activityReplyObject);
        assertEquals(0, likeStatus.likesCount);
    }

    @Test
    public void shouldRemoveLikeOnRemovedActivityReply() {
        Activity activity = new ActivityImpl();
        activity.setActor("Administrator");
        activity.setVerb("test");
        activity.setObject("yo");
        activity.setPublishedDate(new Date());
        activity = activityStreamService.addActivity(activity);

        String activityObject = ActivityHelper.createActivityObject(activity);
        likeService.like("bender", activityObject);

        LikeStatus likeStatus = likeService.getLikeStatus(activityObject);
        assertEquals(1, likeStatus.likesCount);

        long replyPublishedDate = new Date().getTime();
        ActivityReply reply = new ActivityReply("bender", "Bender", "First reply", replyPublishedDate);
        reply = activityStreamService.addActivityReply(activity.getId(), reply);
        assertEquals(activity.getId() + "-reply-1", reply.getId());

        String activityReplyObject = ActivityHelper.createActivityObject(reply.getId());
        likeService.like("bender", activityReplyObject);

        likeStatus = likeService.getLikeStatus(activityReplyObject);
        assertEquals(1, likeStatus.likesCount);

        activityStreamService.removeActivityReply(activity.getId(), reply.getId());
        likeStatus = likeService.getLikeStatus(activityObject);
        assertEquals(1, likeStatus.likesCount);

        likeStatus = likeService.getLikeStatus(activityReplyObject);
        assertEquals(0, likeStatus.likesCount);
    }
}
