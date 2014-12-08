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

package org.nuxeo.ecm.rating.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * An object storing the like / dislike status of an activity object.
 * <p>
 * It may also contain the like status for a {@code username}:
 * <ul>
 * <li>LIKED</li>
 * <li>DISLIKED</li>
 * <li>UNKNOWN</li>
 * </ul>
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class LikeStatus {

    /**
     * Constant used when the {@code username} liked the {@code activityObject} .
     */
    public static final int LIKED = 1;

    /**
     * Constant used when the {@code username} disliked the {@code activityObject}.
     */
    public static final int DISLIKED = -1;

    /**
     * Constant used when the {@code username} didn't like nor dislike the {@code activityObject}.
     */
    public static final int UNKNOWN = 0;

    public final String activityObject;

    public final long likesCount;

    public final long dislikesCount;

    public final String username;

    public final int userLikeStatus;

    /**
     * Creates a {@code LikeStatus} with the like status for the specified {@code username}.
     *
     * @param activityObject the activity object for which this {@code LikeStatus} apply
     * @param likesCount the likes count for the activity object
     * @param dislikesCount the dislikes count for the activity object
     * @param username the username on which the {@code userLikeStatus} apply
     * @param userLikeStatus the like status for the {@code username}
     */
    public LikeStatus(String activityObject, long likesCount, long dislikesCount, String username, int userLikeStatus) {
        this.activityObject = activityObject;
        this.likesCount = likesCount;
        this.dislikesCount = dislikesCount;
        this.username = username;
        this.userLikeStatus = userLikeStatus;
    }

    /**
     * Creates a {@code LikeStatus} without the like status for a {@code username}.
     *
     * @param activityObject the activity object for which this {@code LikeStatus} apply
     * @param likesCount the likes count for the activity object
     * @param dislikesCount the dislikes count for the activity object
     */
    public LikeStatus(String activityObject, long likesCount, long dislikesCount) {
        this(activityObject, likesCount, dislikesCount, null, UNKNOWN);
    }

    /**
     * Returns a {@code Map} of attributes for this {@code LikeStatus}.
     */
    @SuppressWarnings("boxing")
    public Map<String, Serializable> toMap() {
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        map.put("activityObject", activityObject);
        map.put("likesCount", likesCount);
        map.put("dislikesCount", dislikesCount);
        map.put("username", username);
        map.put("userLikeStatus", userLikeStatus);
        return map;
    }

}
