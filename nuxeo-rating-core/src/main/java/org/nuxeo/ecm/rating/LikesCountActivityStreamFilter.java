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

    public static final String ACTORS_PARAMETER = "actors";

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
    public void handleNewActivity(ActivityStreamService activityStreamService,
            Activity activity) {
        // Nothing to do for now
    }

    @Override
    @Deprecated
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            Collection<Serializable> activityIds) {
        // Nothing to do for now
    }

    @Override
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            ActivitiesList activities) {
    }

    @Override
    public ActivitiesList query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, long offset, long limit) {
        QueryType queryType = (QueryType) parameters.get(QUERY_TYPE_PARAMETER);
        if (queryType == null) {
            return new ActivitiesListImpl();
        }

        Query query = null;
        String queryStr = "";
        String innerStr = "";
        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Serializable actor = parameters.get(ACTOR_PARAMETER);

        switch (queryType) {
        case GET_DOCUMENTS_COUNT:
            // InnerQuery indicates if the actor has alredy liked or not
            innerStr = "Select count(activity2) from Activity activity2 where activity2.verb = :verb and activity2.context  = :context and activity2.object = :object AND activity2.actor = :actor AND activity2.target = activity.target";
            queryStr = "Select activity.target, count(activity), ("
                    + innerStr
                    + ") from Activity activity where activity.verb = :verb and activity.context  = :context and activity.object = :object group by activity.target order by count(activity) desc";

            Serializable object = String.valueOf(parameters.get(OBJECT_PARAMETER));
            Serializable context = parameters.get(CONTEXT_PARAMETER);

            query = em.createQuery(queryStr);
            query.setParameter(VERB_PARAMETER, RATING_VERB_PREFIX + LIKE_ASPECT);
            query.setParameter(CONTEXT_PARAMETER, context);
            query.setParameter(OBJECT_PARAMETER, object);
            query.setParameter(ACTOR_PARAMETER, actor);
            break;
        case GET_MINI_MESSAGE_COUNT:
            // InnerQuery indicates if the actor has alredy liked or not
            innerStr = "Select count(likes2) from Activity as likes2 where likes.target = likes2.target and likes2.actor = :actor";
            queryStr = "Select likes.target, count(likes), ("
                    + innerStr
                    + ") from Activity as likes, Activity as minimessage where concat('activity:', minimessage.id) = likes.target and minimessage.verb = :verbMiniMessage and minimessage.context  = :context and likes.verb = :verb group by likes.target order by count(likes) desc";

            query = em.createQuery(queryStr);
            query.setParameter(ACTOR_PARAMETER, actor);
            query.setParameter(CONTEXT_PARAMETER,
                    parameters.get(CONTEXT_PARAMETER));
            query.setParameter(VERB_PARAMETER, RATING_VERB_PREFIX + LIKE_ASPECT);
            query.setParameter(VERB_MINIMESSAGE_PARAMETER, "minimessage");
            break;
        default:
            log.info("Unknown query type: " + queryType);
            return new ActivitiesListImpl();
        }

        if (limit > 0) {
            if (offset != 0) {
                query.setFirstResult((int) offset);
            }
        }

        ActivitiesList likesCount = new ActivitiesListImpl();
        for (Object result : query.getResultList()) {
            Object[] objects = (Object[]) result;
            ActivityBuilder ab = new ActivityBuilder().verb(
                    RATING_VERB_PREFIX + LIKE_ASPECT).actor((String) actor).object(
                    String.valueOf(objects[1])).target((String) objects[0]).context(
                    String.valueOf(objects[2]));
            likesCount.add(ab.build());
        }

        return likesCount;
    }
}
