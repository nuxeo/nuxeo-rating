/*
 * (C) Copyright 2006-2018 Nuxeo (http://nuxeo.com/) and others.
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

import static org.nuxeo.ecm.core.schema.FacetNames.SUPER_SPACE;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.ACTOR_PARAMETER;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.ASPECT_PARAMETER;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.CONTEXT_PARAMETER;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.QUERY_TYPE_PARAMETER;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.QueryType.GET_ACTOR_RATINGS_FOR_OBJECT;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.QueryType.GET_LATEST_RATED_FOR_OBJECT;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.QueryType.GET_RATED_CHILDREN_FOR_CONTEXT;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.QueryType.GET_RATINGS_FOR_CANCEL;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.QueryType.GET_RATINGS_FOR_OBJECT;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.RATING_PARAMETER;
import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.TARGET_OBJECT_PARAMETER;
import static org.nuxeo.ecm.rating.api.Constants.RATING_VERB_PREFIX;

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
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.rating.api.RatingService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

/**
 * Default implementation of {@see RatingService}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class RatingServiceImpl extends DefaultComponent implements RatingService {

    private static final Log log = LogFactory.getLog(RatingServiceImpl.class);

    @Override
    public void rate(String username, int rating, String activityObject, String aspect) {
        Activity activity = new ActivityBuilder().verb(RATING_VERB_PREFIX + aspect)
                                                 .actor(ActivityHelper.createUserActivityObject(username))
                                                 .target(activityObject)
                                                 .object(String.valueOf(rating))
                                                 .build();
        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        activityStreamService.addActivity(activity);

        addSuperSpaceRate(activity);
    }

    @Override
    public void cancelRate(String username, String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_RATINGS_FOR_CANCEL);
        parameters.put(ACTOR_PARAMETER, ActivityHelper.createUserActivityObject(username));
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(RatingActivityStreamFilter.ID, parameters);
        activityStreamService.removeActivities(activities);
    }

    @Override
    public void cancelRates(String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_RATINGS_FOR_CANCEL);
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(RatingActivityStreamFilter.ID, parameters);
        activityStreamService.removeActivities(activities);
    }

    @Override
    public boolean hasUserRated(String username, String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_ACTOR_RATINGS_FOR_OBJECT);
        parameters.put(ACTOR_PARAMETER, ActivityHelper.createUserActivityObject(username));
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(RatingActivityStreamFilter.ID, parameters);
        return !activities.isEmpty();
    }

    @Override
    public long getRatesCount(String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_RATINGS_FOR_OBJECT);
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(RatingActivityStreamFilter.ID, parameters);
        return activities.size();
    }

    @Override
    public long getRatesCount(String activityObject, int rating, String aspect) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_RATINGS_FOR_OBJECT);
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);
        parameters.put(RATING_PARAMETER, Integer.valueOf(rating));

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(RatingActivityStreamFilter.ID, parameters);
        return activities.size();
    }

    @Override
    public long getRatesCountForUser(String username, String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_ACTOR_RATINGS_FOR_OBJECT);
        parameters.put(ACTOR_PARAMETER, ActivityHelper.createUserActivityObject(username));
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(RatingActivityStreamFilter.ID, parameters);
        return activities.size();
    }

    @Override
    public long getRatesCountForUser(String username, String activityObject, int rating, String aspect) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_ACTOR_RATINGS_FOR_OBJECT);
        parameters.put(ACTOR_PARAMETER, ActivityHelper.createUserActivityObject(username));
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);
        parameters.put(RATING_PARAMETER, Integer.valueOf(rating));

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(RatingActivityStreamFilter.ID, parameters);
        return activities.size();
    }

    @Override
    public double getAverageRating(String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_RATINGS_FOR_OBJECT);
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(RatingActivityStreamFilter.ID, parameters);
        return computeAverage(activities);
    }

    @Override
    public double getAverageRatingForUser(String username, String activityObject, String aspect) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_ACTOR_RATINGS_FOR_OBJECT);
        parameters.put(ACTOR_PARAMETER, ActivityHelper.createUserActivityObject(username));
        parameters.put(TARGET_OBJECT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        ActivitiesList activities = activityStreamService.query(RatingActivityStreamFilter.ID, parameters);
        return computeAverage(activities);
    }

    @Override
    public ActivitiesList getRatedChildren(String activityObject, int rating, String aspect) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_RATED_CHILDREN_FOR_CONTEXT);
        parameters.put(CONTEXT_PARAMETER, activityObject);
        parameters.put(ASPECT_PARAMETER, aspect);
        parameters.put(RATING_PARAMETER, Integer.valueOf(rating));

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        return activityStreamService.query(RatingActivityStreamFilter.ID, parameters);
    }

    @Override
    public ActivitiesList getLastestRatedDocByUser(String username, String aspect, int limit) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(QUERY_TYPE_PARAMETER, GET_LATEST_RATED_FOR_OBJECT);
        parameters.put(ACTOR_PARAMETER, ActivityHelper.createUserActivityObject(username));
        parameters.put(ASPECT_PARAMETER, aspect);

        ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        return activityStreamService.query(RatingActivityStreamFilter.ID, parameters, 0, limit);
    }

    private double computeAverage(ActivitiesList activities) {
        double average = 0;
        for (Activity activity : activities) {
            try {
                average += Integer.valueOf(activity.getObject()).intValue();
            } catch (NumberFormatException e) {
                log.warn(activity.getObject() + " is not a valid rating");
            }
        }
        return average / activities.size();
    }

    protected void addSuperSpaceRate(final Activity fromActivity) {
        final String activityObject = fromActivity.getTarget();
        if (!ActivityHelper.isDocument(activityObject)) {
            return;
        }

        final ActivityStreamService activityStreamService = Framework.getService(ActivityStreamService.class);
        new UnrestrictedSessionRunner(ActivityHelper.getRepositoryName(activityObject)) {
            @Override
            public void run() {
                IdRef docId = new IdRef(ActivityHelper.getDocumentId(activityObject));
                for (DocumentModel parent : session.getParentDocuments(docId)) {
                    if (!parent.hasFacet(SUPER_SPACE)) {
                        continue;
                    }
                    Activity activity = new ActivityBuilder(fromActivity).context(
                            ActivityHelper.createDocumentActivityObject(parent)).build();
                    activityStreamService.addActivity(activity);
                }
            }
        }.runUnrestricted();
    }
}
