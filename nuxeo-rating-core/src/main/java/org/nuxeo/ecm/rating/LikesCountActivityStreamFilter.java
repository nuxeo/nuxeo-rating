package org.nuxeo.ecm.rating;

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

    public static final String ID = "LikesCountActivityStreamFilter";

    public static final String CONTEXT_PARAMETER = "context";

    public static final String ASPECT_PARAMETER = "aspect";

    public static final String OBJECT_PARAMETER = "object";

    public static final String ACTOR_PARAMETER = "actor";

    protected static final String VERB_PARAMETER = "verb";

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
    public void handleRemovedActivities(
            ActivityStreamService activityStreamService,
            Collection<Serializable> activityIds) {
        // Nothing to do for now
    }

    @Override
    public ActivitiesList query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, long offset, long limit) {

        String innerStr = "Select count(activity2) from Activity activity2 where activity2.verb = :verb and activity2.context  = :context and activity2.object = :object AND activity2.actor = :actor AND activity2.target = activity.target";
        String queryStr = "Select activity.target, count(activity), ("
                + innerStr
                + ") from Activity activity where activity.verb = :verb and activity.context  = :context and activity.object = :object group by activity.target order by count(activity) desc";

        Serializable object = String.valueOf(parameters.get(OBJECT_PARAMETER));
        Serializable context = parameters.get(CONTEXT_PARAMETER);
        Serializable actor = parameters.get(ACTOR_PARAMETER);

        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        Query query = em.createQuery(queryStr);
        query.setParameter(VERB_PARAMETER, RATING_VERB_PREFIX + LIKE_ASPECT);
        query.setParameter(CONTEXT_PARAMETER, context);
        query.setParameter(OBJECT_PARAMETER, object);
        query.setParameter(ACTOR_PARAMETER, actor);

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
