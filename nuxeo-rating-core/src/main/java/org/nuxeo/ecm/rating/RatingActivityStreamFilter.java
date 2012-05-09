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

import static org.nuxeo.ecm.rating.api.Constants.RATING_VERB_SUFFIX;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.ActivitiesListImpl;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityStreamFilter;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;

/**
 * Activity Stream filter handling rating activities.
 * <p>
 * The different queries this filter can handle are defined in the
 * {@link QueryType} enum.
 * 
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
public class RatingActivityStreamFilter implements ActivityStreamFilter {

    public static final String ID = "RatingActivityStreamFilter";

    public enum QueryType {
        GET_ACTOR_RATINGS_FOR_OBJECT, GET_RATINGS_FOR_OBJECT, GET_RATED_CHILDREN_FOR_CONTEXT
    }

    public static final String QUERY_TYPE_PARAMETER = "queryTypeParameter";

    public static final String TARGET_OBJECT_PARAMETER = "targetObject";

    public static final String ASPECT_PARAMETER = "aspect";

    public static final String ACTOR_PARAMETER = "actor";

    public static final String RATING_PARAMETER = "rating";

    public static final String CONTEXT_PARAMETER = "context";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isInterestedIn(Activity activity) {
        String verb = activity.getVerb();
        return verb != null && verb.startsWith(RATING_VERB_SUFFIX);
    }

    @Override
    public void handleNewActivity(ActivityStreamService activityStreamService,
            Activity activity) {
        // nothing to do for now
    }

    @Override
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            Collection<Serializable> activityIds) {
        // nothing to do for now
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActivitiesList query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, long offset, long limit) {
        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        QueryType queryType = (QueryType) parameters.get(QUERY_TYPE_PARAMETER);
        if (queryType == null) {
            return new ActivitiesListImpl();
        }

        Query query = null;
        StringBuilder sb;
        String targetObject = (String) parameters.get(TARGET_OBJECT_PARAMETER);
        String aspect = (String) parameters.get(ASPECT_PARAMETER);
        String actor = (String) parameters.get(ACTOR_PARAMETER);
        Integer rating = (Integer) parameters.get(RATING_PARAMETER);
        String context = (String) parameters.get(CONTEXT_PARAMETER);
        switch (queryType) {
        case GET_ACTOR_RATINGS_FOR_OBJECT:
            sb = new StringBuilder(
                    "select activity from Activity activity where activity.verb = :verb and activity.actor = :actor and activity.target = :targetObject and activity.context is null");
            if (rating != null) {
                sb.append(" and activity.object = :rating");
            }
            query = em.createQuery(sb.toString());
            query.setParameter("verb", RATING_VERB_SUFFIX + aspect);
            query.setParameter(TARGET_OBJECT_PARAMETER, targetObject);
            query.setParameter(ACTOR_PARAMETER, actor);
            if (rating != null) {
                query.setParameter(RATING_PARAMETER, String.valueOf(rating));
            }
            break;
        case GET_RATINGS_FOR_OBJECT:
            sb = new StringBuilder(
                    "select activity from Activity activity where activity.verb = :verb and activity.target = :targetObject and activity.context is null");
            if (rating != null) {
                sb.append(" and activity.object = :rating");
            }
            query = em.createQuery(sb.toString());
            query.setParameter("verb", RATING_VERB_SUFFIX + aspect);
            query.setParameter(TARGET_OBJECT_PARAMETER, targetObject);
            if (rating != null) {
                query.setParameter(RATING_PARAMETER, String.valueOf(rating));
            }
            break;
        case GET_RATED_CHILDREN_FOR_CONTEXT:
            sb = new StringBuilder(
                    "select activity from Activity activity where activity.verb = :verb and activity.context  = :context");
            if (rating != null) {
                sb.append(" and activity.object = :rating");
            }
            query = em.createQuery(sb.toString());
            query.setParameter("verb", RATING_VERB_SUFFIX + aspect);
            query.setParameter(CONTEXT_PARAMETER, context);
            if (rating != null) {
                query.setParameter(RATING_PARAMETER, String.valueOf(rating));
            }
            break;
        }

        if (limit > 0) {
            query.setMaxResults((int) limit);
            if (offset > 0) {
                query.setFirstResult((int) offset);
            }
        }
        return new ActivitiesListImpl(query.getResultList());
    }
}
