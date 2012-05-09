package org.nuxeo.ecm.rating.api;

import org.nuxeo.ecm.activity.ActivitiesList;

/**
 * Service handling rating on activity objects.
 * <p>
 * The same activity object can handle multiple ratings by using different
 * {@code aspect}s.
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
     * Returns {@code true} if the {@code username} already rated the
     * {@code activityObject} on the given {@code aspect}, {@code false}
     * otherwise.
     */
    boolean hasUserRated(String username, String activityObject, String aspect);

    /**
     * Returns the rates count for the {@code activityObject} on the given
     * {@code aspect}.
     */
    long getRatesCount(String activityObject, String aspect);

    /**
     * Returns the rates count for the {@code activityObject} on the given
     * {@code aspect} where the rating is equals to {@code rating}.
     */
    long getRatesCount(String activityObject, int rating, String aspect);

    /**
     * Returns the rates count by {@code username} for the
     * {@code activityObject} on the given {@code aspect}.
     */
    long getRatesCountForUser(String username, String activityObject,
            String aspect);

    /**
     * Returns the rates count by {@code username} for the
     * {@code activityObject} on the given {@code aspect} where the rating is
     * equals to {@code rating}.
     */
    long getRatesCountForUser(String username, String activityObject,
            int rating, String aspect);

    /**
     * Returns the average rating for the {@code activityObject} on the given
     * {@code aspect}.
     */
    double getAverageRating(String activityObject, String aspect);

    /**
     * Returns the average rating by {@code username} for the
     * {@code activityObject} on the given {@code aspect}.
     */
    double getAverageRatingForUser(String username, String activityObject,
            String aspect);

    /**
     * Returns the list of their rated children
     * {@code activityObject} on the given {@code aspect} with {@code rating}.
     *
     * @return a List of activityObject
     */
    ActivitiesList getRatedChildren(String activityObject, int rating, String aspect);
}
