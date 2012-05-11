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

import static org.nuxeo.ecm.rating.api.Constants.LIKE_ASPECT;
import static org.nuxeo.ecm.rating.api.LikeStatus.DISLIKED;
import static org.nuxeo.ecm.rating.api.LikeStatus.LIKED;
import static org.nuxeo.ecm.rating.api.LikeStatus.UNKNOWN;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.ActivitiesListImpl;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.rating.api.LikeService;
import org.nuxeo.ecm.rating.api.LikeStatus;
import org.nuxeo.ecm.rating.api.RatingService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Default implementation of {@see LikeService}.
 * 
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class LikeServiceImpl extends DefaultComponent implements LikeService {

    private static final Log log = LogFactory.getLog(LikeServiceImpl.class);

    public static final int LIKE_RATING = 1;

    public static final int DISLIKE_RATING = -1;

    @Override
    public void like(String username, String activityObject) {
        RatingService ratingService = Framework.getLocalService(RatingService.class);
        ratingService.cancelRate(username, activityObject, LIKE_ASPECT);
        ratingService.rate(username, LIKE_RATING, activityObject, LIKE_ASPECT);
    }

    @Override
    public void like(String username, DocumentModel doc) {
        like(username, ActivityHelper.createDocumentActivityObject(doc));
    }

    @Override
    public boolean hasUserLiked(String username, String activityObject) {
        RatingService ratingService = Framework.getLocalService(RatingService.class);
        double average = ratingService.getAverageRatingForUser(username,
                activityObject, LIKE_ASPECT);
        return average > 0;
    }

    @Override
    public boolean hasUserLiked(String username, DocumentModel doc) {
        return hasUserLiked(username,
                ActivityHelper.createDocumentActivityObject(doc));
    }

    @Override
    public long getLikesCount(String activityObject) {
        RatingService ratingService = Framework.getLocalService(RatingService.class);
        return ratingService.getRatesCount(activityObject, LIKE_RATING,
                LIKE_ASPECT);
    }

    @Override
    public long getLikesCount(DocumentModel doc) {
        return getLikesCount(ActivityHelper.createDocumentActivityObject(doc));
    }

    @Override
    public void dislike(String username, String activityObject) {
        RatingService ratingService = Framework.getLocalService(RatingService.class);
        ratingService.cancelRate(username, activityObject, LIKE_ASPECT);
        ratingService.rate(username, DISLIKE_RATING, activityObject,
                LIKE_ASPECT);
    }

    @Override
    public void dislike(String username, DocumentModel doc) {
        dislike(username, ActivityHelper.createDocumentActivityObject(doc));
    }

    @Override
    public boolean hasUserDisliked(String username, String activityObject) {
        RatingService ratingService = Framework.getLocalService(RatingService.class);
        double average = ratingService.getAverageRatingForUser(username,
                activityObject, LIKE_ASPECT);
        return average < 0;
    }

    @Override
    public boolean hasUserDisliked(String username, DocumentModel doc) {
        return hasUserDisliked(username,
                ActivityHelper.createDocumentActivityObject(doc));
    }

    @Override
    public long getDislikesCount(String activityObject) {
        RatingService ratingService = Framework.getLocalService(RatingService.class);
        return ratingService.getRatesCount(activityObject, DISLIKE_RATING,
                LIKE_ASPECT);
    }

    @Override
    public long getDislikesCount(DocumentModel doc) {
        return getDislikesCount(ActivityHelper.createDocumentActivityObject(doc));
    }

    @Override
    public void cancel(String username, String activityObject) {
        RatingService ratingService = Framework.getLocalService(RatingService.class);
        ratingService.cancelRate(username, activityObject, LIKE_ASPECT);
    }

    @Override
    public void cancel(String username, DocumentModel doc) {
        cancel(username, ActivityHelper.createDocumentActivityObject(doc));
    }

    @Override
    public LikeStatus getLikeStatus(String activityObject) {
        long likesCount = getLikesCount(activityObject);
        long dislikesCount = getDislikesCount(activityObject);
        return new LikeStatus(activityObject, likesCount, dislikesCount);
    }

    @Override
    public LikeStatus getLikeStatus(DocumentModel doc) {
        return getLikeStatus(ActivityHelper.createDocumentActivityObject(doc));
    }

    @Override
    public LikeStatus getLikeStatus(String username, String activityObject) {
        long likesCount = getLikesCount(activityObject);
        long dislikesCount = getDislikesCount(activityObject);
        int userLikeStatus = hasUserLiked(username, activityObject) ? LIKED
                : hasUserDisliked(username, activityObject) ? DISLIKED
                        : UNKNOWN;
        return new LikeStatus(activityObject, likesCount, dislikesCount,
                username, userLikeStatus);
    }

    @Override
    public LikeStatus getLikeStatus(String username, DocumentModel doc) {
        return getLikeStatus(username,
                ActivityHelper.createDocumentActivityObject(doc));
    }

    @Override
    public ActivitiesList getMostLikedDocuments(CoreSession session, int limit,
            DocumentModel source) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(LikesCountActivityStreamFilter.CONTEXT_PARAMETER,
                ActivityHelper.createDocumentActivityObject(source));
        parameters.put(LikesCountActivityStreamFilter.OBJECT_PARAMETER,
                LIKE_RATING);
        parameters.put(
                LikesCountActivityStreamFilter.ACTOR_PARAMETER,
                ActivityHelper.createUserActivityObject(session.getPrincipal().getName()));

        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        ActivitiesList activitiesList = activityStreamService.query(
                LikesCountActivityStreamFilter.ID, parameters);
        ActivitiesList filtered = activitiesList.filterActivities(session);
        if (filtered.size() > limit) {
            return new ActivitiesListImpl(filtered.subList(0, limit));
        }
        return filtered;
    }
}
