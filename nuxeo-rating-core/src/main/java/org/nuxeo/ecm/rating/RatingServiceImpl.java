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

import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.ACTOR_PARAMETER;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.ASPECT_PARAMETER;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.QUERY_TYPE_PARAMETER;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.QueryType.GET_ACTOR_RATINGS_FOR_OBJECT;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.QueryType.GET_RATINGS_FOR_OBJECT;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.RATING_PARAMETER;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.TARGET_OBJECT_PARAMETER;
import static org.nuxeo.ecm.rating.api.Constants.RATING_VERB_SUFFIX;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.rating.api.RatingService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Default implementation of {@see RatingService}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class RatingServiceImpl extends DefaultComponent implements
        RatingService {

    private static final Log log = LogFactory.getLog(RatingServiceImpl.class);

    @Override
    public void rate(String username, int rating, String activityObject,
            String aspect) {
        Activity activity = new ActivityBuilder().verb(
                RATING_VERB_SUFFIX + aspect).actor(
                ActivityHelper.createUserActivityObject(username)).target(
                activityObject).object(String.valueOf(rating)).build();
        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        activityStreamService.addActivity(activity);
    }

    @Override
    public void cancelRate(String username, String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_ACTOR_RATINGS_FOR_OBJECT);
        parameters.put(ACTOR_PARAMETER,
                ActivityHelper.createUserActivityObject(username));
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(
                RatingActivityStreamFilter.ID, parameters);
        activityStreamService.removeActivities(activities.toActivityIds());
    }

    @Override
    public boolean hasUserRated(String username, String activityObject,
            String aspect) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_ACTOR_RATINGS_FOR_OBJECT);
        parameters.put(ACTOR_PARAMETER,
                ActivityHelper.createUserActivityObject(username));
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(
                RatingActivityStreamFilter.ID, parameters);
        return !activities.isEmpty();
    }

    @Override
    public long getRatesCount(String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_RATINGS_FOR_OBJECT);
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(
                RatingActivityStreamFilter.ID, parameters);
        return activities.size();
    }

    @Override
    public long getRatesCount(String activityObject, int rating, String aspect) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_RATINGS_FOR_OBJECT);
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);
        parameters.put(RATING_PARAMETER, rating);

        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(
                RatingActivityStreamFilter.ID, parameters);
        return activities.size();
    }

    @Override
    public long getRatesCountForUser(String username, String activityObject,
            String aspect) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_ACTOR_RATINGS_FOR_OBJECT);
        parameters.put(ACTOR_PARAMETER,
                ActivityHelper.createUserActivityObject(username));
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(
                RatingActivityStreamFilter.ID, parameters);
        return activities.size();
    }

    @Override
    public long getRatesCountForUser(String username, String activityObject,
            int rating, String aspect) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_ACTOR_RATINGS_FOR_OBJECT);
        parameters.put(ACTOR_PARAMETER,
                ActivityHelper.createUserActivityObject(username));
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);
        parameters.put(RATING_PARAMETER, rating);

        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(
                RatingActivityStreamFilter.ID, parameters);
        return activities.size();
    }

    @Override
    public double getAverageRating(String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_RATINGS_FOR_OBJECT);
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(
                RatingActivityStreamFilter.ID, parameters);
        return computeAverage(activities);
    }

    @Override
    public double getAverageRatingForUser(String username,
            String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_ACTOR_RATINGS_FOR_OBJECT);
        parameters.put(ACTOR_PARAMETER,
                ActivityHelper.createUserActivityObject(username));
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(
                RatingActivityStreamFilter.ID, parameters);
        return computeAverage(activities);
    }

    private double computeAverage(ActivitiesList activities) {
        double average = 0;
        for (Activity activity : activities) {
            try {
                average += Integer.valueOf(activity.getObject());
            } catch (NumberFormatException e) {
                log.warn(activity.getObject() + " is not a valid rating");
            }
        }
        return average / activities.size();
    }
}
