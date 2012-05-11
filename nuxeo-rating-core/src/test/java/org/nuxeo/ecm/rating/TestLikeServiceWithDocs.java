package org.nuxeo.ecm.rating;

import static org.junit.Assert.assertEquals;
import static org.nuxeo.ecm.activity.ActivityHelper.createDocumentActivityObject;
import static org.nuxeo.ecm.rating.LikeServiceImpl.LIKE_RATING;
import static org.nuxeo.ecm.rating.api.Constants.LIKE_ASPECT;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 * @since 5.6
 */
@RunWith(FeaturesRunner.class)
@Features({ RatingFeature.class })
@RepositoryConfig(repositoryName = "default", cleanup = Granularity.METHOD, init = DefaultRepositoryInit.class)
@LocalDeploy("org.nuxeo.ecm.rating.core:rating-test.xml")
public class TestLikeServiceWithDocs extends AbstractRatingTest {
    @Test
    public void shouldHandleSuperSpaceCount() throws ClientException {
        DocumentModel folder = session.createDocumentModel(
                "/default-domain/workspaces/", "folder", "Folder");
        folder = session.createDocument(folder);
        session.save();

        DocumentModel testDoc = createTestDocument("myDoc",
                "/default-domain/workspaces/folder");
        DocumentModel defaultDomain = session.getDocument(new PathRef(
                "/default-domain"));
        DocumentModel workspaces = session.getDocument(new PathRef(
                "/default-domain/workspaces"));

        likeService.like("robin", testDoc);
        likeService.like("barney", testDoc);
        likeService.dislike("Ted", testDoc);

        assertEquals(3, ratingService.getRatesCount(
                createDocumentActivityObject(testDoc), LIKE_ASPECT));

        // Only on SuperSpace
        assertEquals(
                0,
                ratingService.getRatedChildren(
                        createDocumentActivityObject(folder), LIKE_RATING,
                        LIKE_ASPECT).size());

        assertEquals(
                2,
                ratingService.getRatedChildren(
                        createDocumentActivityObject(defaultDomain),
                        LIKE_RATING, LIKE_ASPECT).size());
        assertEquals(
                2,
                ratingService.getRatedChildren(
                        createDocumentActivityObject(workspaces), LIKE_RATING,
                        LIKE_ASPECT).size());

        // All activities correctly removed
        likeService.cancel("robin", testDoc);
        assertEquals(2, ratingService.getRatesCount(
                createDocumentActivityObject(testDoc), LIKE_ASPECT));
        assertEquals(
                1,
                ratingService.getRatedChildren(
                        createDocumentActivityObject(defaultDomain),
                        LIKE_RATING, LIKE_ASPECT).size());
    }

    @Test
    public void shouldCorrectlyReturnedMostLikedDocumentList()
            throws ClientException {
        DocumentModel workspaces = session.getDocument(new PathRef(
                "/default-domain/workspaces"));

        DocumentModel doc1 = createTestDocument("tmp",
                "/default-domain/workspaces/test");
        DocumentModel doc2 = createTestDocument("tmp1",
                "/default-domain/workspaces/test");
        DocumentModel doc3 = createTestDocument("tmp2",
                "/default-domain/workspaces/test");
        DocumentModel doc4 = createTestDocument("tmp3",
                "/default-domain/workspaces/test");

        likeService.like("robin", doc3); // 3-1
        likeService.like("robin", doc4); // 4-1

        likeService.like("barney", doc1); // 1-1
        likeService.like("barney", doc3); // 3-2
        likeService.like("barney", doc4); // 4-2

        likeService.like("ted", doc2); // 2-1

        likeService.like("marshal", doc3); // 3-3
        likeService.like("marshal", doc4); // 4-3

        likeService.like("lily", doc3); // 3-4

        session.save();

        // order should be: doc3 - doc4 - doc1 - doc2
        ActivitiesList mostLikedDocuments = likeService.getMostLikedDocuments(
                session, 10, workspaces);
        assertEquals(4, mostLikedDocuments.size());

        assertEquals(
                doc3.getId(),
                ActivityHelper.getDocumentId(mostLikedDocuments.get(0).getTarget()));
        assertEquals("4", mostLikedDocuments.get(0).getObject());
        assertEquals(
                doc4.getId(),
                ActivityHelper.getDocumentId(mostLikedDocuments.get(1).getTarget()));
        assertEquals("3", mostLikedDocuments.get(1).getObject());

        // doc1 and doc2 may be inverted
        assertEquals("1", mostLikedDocuments.get(2).getObject());
        assertEquals("1", mostLikedDocuments.get(3).getObject());

        mostLikedDocuments = likeService.getMostLikedDocuments(session, 2,
                workspaces);
        assertEquals(2, mostLikedDocuments.size());
    }

    @Test
    public void shouldCorrectlyCountWithRatingCountFilter()
            throws ClientException {
        DocumentModel test = session.getDocument(new PathRef(
                "/default-domain/workspaces/test"));

        DocumentModel doc1 = createTestDocument("tmp",
                "/default-domain/workspaces/test");
        DocumentModel doc2 = createTestDocument("tmp1",
                "/default-domain/workspaces/test");

        likeService.like("ted", doc1);
        likeService.like("Robin", doc1);
        likeService.like("Lily", doc1);

        likeService.like("ted", doc2);
        likeService.like("Barney", doc2);
        likeService.like("Marshal", doc2);
        likeService.like("Ranjit", doc2);

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(LikesCountActivityStreamFilter.CONTEXT_PARAMETER,
                ActivityHelper.createDocumentActivityObject(test));
        parameters.put(LikesCountActivityStreamFilter.ASPECT_PARAMETER, "like");
        parameters.put(LikesCountActivityStreamFilter.OBJECT_PARAMETER,
                LIKE_RATING);
        parameters.put(LikesCountActivityStreamFilter.ACTOR_PARAMETER,
                ActivityHelper.createUserActivityObject("Robin"));

        ActivitiesList activitiesList = activityStreamService.query(
                LikesCountActivityStreamFilter.ID, parameters);
        assertEquals(2, activitiesList.size());
        assertEquals("4", activitiesList.get(0).getObject());
        assertEquals("3", activitiesList.get(1).getObject());

        assertEquals("0", activitiesList.get(0).getContext());
        assertEquals("1", activitiesList.get(1).getContext());
    }

    protected void addDummyActivities(ActivitiesList activities, String docId,
            int nb) {
        for (int i = 0; i < nb; i++) {
            ActivityBuilder ab = new ActivityBuilder().target(
                    ActivityHelper.createDocumentActivityObject("default",
                            docId)).object("1");
            activities.add(ab.build());
        }
    }

    @Test
    public void shouldEnsureOrderIsRight() throws ClientException {
        int expected = 75;
        int limit = 30;

        for (int i = 0; i < expected; i++) {
            createAndLikeDocs(i);
        }
        PathRef testRef = new PathRef("/default-domain/workspaces/test");
        assertEquals(expected, session.getChildren(testRef).size());

        ActivitiesList docs = likeService.getMostLikedDocuments(session, limit,
                session.getDocument(testRef));
        assertEquals(limit, docs.size());

        for (int i = 0; i < limit; i++) {
            int id = expected - (i + 1);
            Activity activity = docs.get(i);
            IdRef docRef = new IdRef(
                    ActivityHelper.getDocumentId(activity.getTarget()));
            assertEquals("doc" + id, session.getDocument(docRef).getName());
            assertEquals(String.valueOf(id), activity.getObject());
        }
    }

    protected void createAndLikeDocs(int nb) throws ClientException {
        DocumentModel doc = createTestDocument("doc" + nb,
                "/default-domain/workspaces/test");
        for (int i = 0; i < nb; i++) {
            likeService.like("user" + i, doc);
        }
    }
}
