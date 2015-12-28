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

package org.nuxeo.ecm.rating.api;

import java.util.Date;

import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Service handling likes / dislikes on documents, or any other activity object.
 * 
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public interface LikeService {

    /**
     * Like the given {@code activityObject} by the {@code username}.
     */
    void like(String username, String activityObject);

    /**
     * Convenient method to like a {@link DocumentModel}.
     * 
     * @see LikeService#like(String, String)
     */
    void like(String username, DocumentModel doc);

    /**
     * Returns {@code true} if the given {@code username} already liked the {@code activityObject}, {@code false}
     * otherwise.
     */
    boolean hasUserLiked(String username, String activityObject);

    /**
     * Returns {@code true} if the given {@code username} already liked the {@code doc}, {@code false} otherwise.
     */
    boolean hasUserLiked(String username, DocumentModel doc);

    /**
     * Returns the likes count for the given {@code activityObject}.
     */
    long getLikesCount(String activityObject);

    /**
     * Convenient method to returns the likes count for a {@link DocumentModel}.
     * 
     * @see LikeService#getLikesCount(String)
     */
    long getLikesCount(DocumentModel doc);

    /**
     * Dislike the given {@code activityObject} by the {@code username}.
     */
    void dislike(String username, String activityObject);

    /**
     * Convenient method to dislike a {@see DocumentModel}.
     * 
     * @see LikeService#dislike(String, String)
     */
    void dislike(String username, DocumentModel doc);

    /**
     * Returns {@code true} if the given {@code username} already disliked the {@code activityObject}, {@code false}
     * otherwise.
     */
    boolean hasUserDisliked(String username, String activityObject);

    /**
     * Returns {@code true} if the given {@code username} already disliked the {@code doc}, {@code false} otherwise.
     */
    boolean hasUserDisliked(String username, DocumentModel doc);

    /**
     * Returns the dislikes count for the given {@code activityObject}.
     */
    long getDislikesCount(String activityObject);

    /**
     * Convenient method to returns the dislikes count for a {@link DocumentModel}.
     * 
     * @see LikeService#getDislikesCount(String)
     */
    long getDislikesCount(DocumentModel doc);

    /**
     * Cancel a like or dislike for the given {@code username}.
     * 
     * @param username the username
     * @param activityObject the activity object on which to cancel the like or dislike.
     */
    void cancel(String username, String activityObject);

    /**
     * Convenient method to cancel a like or dislike on a {@see DocumentModel}.
     * 
     * @see LikeService#cancel(String, String)
     */
    void cancel(String username, DocumentModel doc);

    /**
     * Returns the {@see LikeStatus} for the {@code activityObject}.
     */
    LikeStatus getLikeStatus(String activityObject);

    /**
     * Convenient method to return the {@see LikeStatus} for a {@see DocumentModel}.
     * 
     * @see LikeService#getLikeStatus(String)
     */
    LikeStatus getLikeStatus(DocumentModel doc);

    /**
     * Returns the {@see LikeStatus} for the {@code username} and {@code activityObject}.
     * <p>
     * The returned {@see LikeStatus} will have the information about the like / dislike status of the {@code username}.
     */
    LikeStatus getLikeStatus(String username, String activityObject);

    /**
     * Convenient method to return the {@see LikeStatus} for the {@code username} and a {@see DocumentModel}.
     * 
     * @see LikeService#getLikeStatus(String, String)
     */
    LikeStatus getLikeStatus(String username, DocumentModel doc);

    /**
     * An actitivitesList containing a documentActivity or a minimessageActivity as target, the likes count as object,
     * current user as actor and actor's likes in context.
     * 
     * @param limit maximum documents returned
     * @param source the parent document when child will be reached
     */
    ActivitiesList getMostLikedActivities(CoreSession session, int limit, DocumentModel source);

    /**
     * An actitivitesList containing a documentActivity or a minimessageActivity as target, the likes count as object,
     * current user as actor and actor's likes in context the result will be between two dates
     * 
     * @param limit maximum documents returned
     * @param source the parent document when child will be reached
     */
    ActivitiesList getMostLikedActivities(CoreSession session, int limit, DocumentModel source, Date fromDt, Date toDt);
}
