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
 *     Thomas Roger
 */
package org.nuxeo.ecm.rating.api;

import org.nuxeo.ecm.activity.ActivitiesList;

/**
 * Service handling rating on activity objects.
 * <p>
 * The same activity object can handle multiple ratings by using different {@code aspect}s.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public interface RatingService {

    /**
     * Rates the {@code activityObject} by the {@code username}.
     *
     * @param rating the rating
     * @param aspect the rating aspect
     */
    void rate(String username, int rating, String activityObject, String aspect);

    /**
     * Cancels a rate by the {@code username}.
     *
     * @param activityObject the activity object on which cancelling the rate
     * @param aspect the rating aspect
     */
    void cancelRate(String username, String activityObject, String aspect);

    /**
     * Cancels all rates.
     *
     * @param activityObject the activity object on which cancelling the rate
     * @param aspect the rating aspect may be null.
     */
    void cancelRates(String activityObject, String aspect);

    /**
     * Returns {@code true} if the {@code username} already rated the {@code activityObject} on the given {@code aspect}
     * , {@code false} otherwise.
     */
    boolean hasUserRated(String username, String activityObject, String aspect);

    /**
     * Returns the rates count for the {@code activityObject} on the given {@code aspect}.
     */
    long getRatesCount(String activityObject, String aspect);

    /**
     * Returns the rates count for the {@code activityObject} on the given {@code aspect} where the rating is equals to
     * {@code rating}.
     */
    long getRatesCount(String activityObject, int rating, String aspect);

    /**
     * Returns the rates count by {@code username} for the {@code activityObject} on the given {@code aspect}.
     */
    long getRatesCountForUser(String username, String activityObject, String aspect);

    /**
     * Returns the rates count by {@code username} for the {@code activityObject} on the given {@code aspect} where the
     * rating is equals to {@code rating}.
     */
    long getRatesCountForUser(String username, String activityObject, int rating, String aspect);

    /**
     * Returns the average rating for the {@code activityObject} on the given {@code aspect}.
     */
    double getAverageRating(String activityObject, String aspect);

    /**
     * Returns the average rating by {@code username} for the {@code activityObject} on the given {@code aspect}.
     */
    double getAverageRatingForUser(String username, String activityObject, String aspect);

    /**
     * Returns the list of their rated children {@code activityObject} on the given {@code aspect} with {@code rating}.
     *
     * @return a List of activityObject
     */
    ActivitiesList getRatedChildren(String activityObject, int rating, String aspect);

    /**
     * Get the latest docs activities rated by a specific user
     *
     * @param username the desired user
     * @param limit
     * @return a List of activityObject
     */
    ActivitiesList getLastestRatedDocByUser(String username, String aspect, int limit);
}
