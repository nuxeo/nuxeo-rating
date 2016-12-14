/*
 * (C) Copyright 2012-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Arnaud Kervern
 */
package org.nuxeo.ecm.rating;

import static org.nuxeo.ecm.rating.RatingActivityStreamFilter.QUERY_TYPE_PARAMETER;
import static org.nuxeo.ecm.rating.api.Constants.LIKE_ASPECT;
import static org.nuxeo.ecm.rating.api.Constants.RATING_VERB_PREFIX;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.ActivitiesListImpl;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityReply;
import org.nuxeo.ecm.activity.ActivityStreamFilter;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;

/**
 * An activity stream filter to handle likes count
 *
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.6
 */
public class LikesCountActivityStreamFilter implements ActivityStreamFilter {

    public enum QueryType {
        GET_DOCUMENTS_COUNT, GET_MINI_MESSAGE_COUNT
    }

    public static final String ID = "LikesCountActivityStreamFilter";

    public static final String CONTEXT_PARAMETER = "context";

    public static final String ASPECT_PARAMETER = "aspect";

    public static final String OBJECT_PARAMETER = "object";

    public static final String ACTOR_PARAMETER = "actor";

    public static final String FROMDT_PARAMETER = "fromDt";

    public static final String TODT_PARAMETER = "toDt";

    protected static final String VERB_PARAMETER = "verb";

    protected static final String VERB_MINIMESSAGE_PARAMETER = "verbMiniMessage";

    private static final Log log = LogFactory.getLog(LikesCountActivityStreamFilter.class);

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isInterestedIn(Activity activity) {
        return false;
    }

    @Override
    public void handleNewActivity(ActivityStreamService activityStreamService, Activity activity) {
        // Nothing to do for now
    }

    @Override
    public void handleRemovedActivities(ActivityStreamService activityStreamService, ActivitiesList activities) {
    }

    @Override
    public void handleRemovedActivityReply(ActivityStreamService activityStreamService, Activity activity,
            ActivityReply activityReply) {
    }

    @Override
    public ActivitiesList query(ActivityStreamService activityStreamService, Map<String, Serializable> parameters,
            long offset, long limit) {
        QueryType queryType = (QueryType) parameters.get(QUERY_TYPE_PARAMETER);
        if (queryType == null) {
            return new ActivitiesListImpl();
        }

        Query query;
        String queryStr;
        String innerStr;
        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Serializable actor = parameters.get(ACTOR_PARAMETER);

        switch (queryType) {
        case GET_DOCUMENTS_COUNT:
            // InnerQuery indicates if the actor has alredy liked or not
            innerStr = "SELECT COUNT(activity2) FROM Activity activity2 WHERE activity2.verb = :verb";
            innerStr += " AND activity2.context = :context AND activity2.object = :object";
            innerStr += " AND activity2.actor = :actor AND activity2.target = activity.target";
            if (parameters.containsKey(FROMDT_PARAMETER)) {
                innerStr += " AND activity2.publishedDate BETWEEN :fromDt AND :toDt";
            }
            queryStr = "SELECT activity.target, count(activity), (" + innerStr + ") FROM Activity activity";
            queryStr += " WHERE activity.verb = :verb and activity.context  = :context";
            queryStr += " AND activity.object = :object";
            if (parameters.containsKey(FROMDT_PARAMETER)) {
                queryStr += " AND activity.publishedDate BETWEEN :fromDt AND :toDt";
            }
            queryStr += " GROUP BY activity.target ORDER BY COUNT(activity) DESC";

            Serializable object = String.valueOf(parameters.get(OBJECT_PARAMETER));
            query = em.createQuery(queryStr);
            query.setParameter(OBJECT_PARAMETER, object);

            break;
        case GET_MINI_MESSAGE_COUNT:
            // InnerQuery indicates if the actor has alredy liked or not
            innerStr = "Select count(likes2) from Activity as likes2 ";
            innerStr += " where likes.target = likes2.target and likes2.actor = :actor";
            if (parameters.containsKey(FROMDT_PARAMETER)) {
                innerStr += " AND likes2.publishedDate BETWEEN :fromDt AND :toDt";
            }
            queryStr = "Select likes.target, count(likes), (" + innerStr
                    + ") from Activity as likes, Activity as minimessage";
            queryStr += " where concat('activity:', cast(minimessage.id as string)) = likes.target";
            queryStr += " and minimessage.verb = :verbMiniMessage and minimessage.context  = :context";
            queryStr += " and likes.verb = :verb";
            if (parameters.containsKey(FROMDT_PARAMETER)) {
                queryStr += " AND likes.publishedDate BETWEEN :fromDt AND :toDt";
            }
            queryStr += " group by likes.target order by count(likes) desc";

            query = em.createQuery(queryStr);
            query.setParameter(VERB_MINIMESSAGE_PARAMETER, "minimessage");

            break;
        default:
            log.info("Unknown query type: " + queryType);
            return new ActivitiesListImpl();
        }

        // Default parameters
        query.setParameter(CONTEXT_PARAMETER, parameters.get(CONTEXT_PARAMETER));
        query.setParameter(VERB_PARAMETER, RATING_VERB_PREFIX + LIKE_ASPECT);
        query.setParameter(ACTOR_PARAMETER, actor);
        if (parameters.containsKey(FROMDT_PARAMETER)) {
            query.setParameter(FROMDT_PARAMETER, parameters.get(FROMDT_PARAMETER));
            query.setParameter(TODT_PARAMETER, parameters.get(TODT_PARAMETER));
        }

        if (limit > 0) {
            query.setMaxResults((int) limit);
        }
        if (offset > 0) {
            query.setFirstResult((int) offset);
        }

        ActivitiesList likesCount = new ActivitiesListImpl();
        for (Object result : query.getResultList()) {
            Object[] objects = (Object[]) result;
            ActivityBuilder ab = new ActivityBuilder().verb(RATING_VERB_PREFIX + LIKE_ASPECT)
                                                      .actor((String) actor)
                                                      .object(String.valueOf(objects[1]))
                                                      .target((String) objects[0])
                                                      .context(String.valueOf(objects[2]));
            likesCount.add(ab.build());
        }

        return likesCount;
    }
}
