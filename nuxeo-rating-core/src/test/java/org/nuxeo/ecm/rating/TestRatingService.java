/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.trash.TrashService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@RunWith(FeaturesRunner.class)
@Features({ RatingFeature.class })
@SuppressWarnings("boxing")
public class TestRatingService extends AbstractRatingTest {

    public static final String STARS_ASPECT = "stars";

    public static final String OTHER_ASPECT = "other";

    @Test
    public void serviceRegistration()  {
        assertNotNull(ratingService);
    }

    @Test
    public void differentUsersCanRateADocument() {
        DocumentModel doc = createTestDocument("file1");
        String docActivityObject = ActivityHelper.createDocumentActivityObject(doc);

        ratingService.rate("bender", 5, docActivityObject, STARS_ASPECT);
        assertTrue(ratingService.hasUserRated("bender", docActivityObject, STARS_ASPECT));

        ratingService.rate("fry", 2, docActivityObject, STARS_ASPECT);
        ratingService.rate("leela", 1, docActivityObject, STARS_ASPECT);
        assertTrue(ratingService.hasUserRated("fry", docActivityObject, STARS_ASPECT));
        assertTrue(ratingService.hasUserRated("leela", docActivityObject, STARS_ASPECT));
    }

    @Test
    public void differentUsersCanRateAnActivity() {
        String activityObject = ActivityHelper.createActivityObject(15L);

        ratingService.rate("leela", 5, activityObject, STARS_ASPECT);
        assertTrue(ratingService.hasUserRated("leela", activityObject, STARS_ASPECT));

        ratingService.rate("bender", 2, activityObject, STARS_ASPECT);
        ratingService.rate("fry", 1, activityObject, STARS_ASPECT);
        assertTrue(ratingService.hasUserRated("bender", activityObject, STARS_ASPECT));
        assertTrue(ratingService.hasUserRated("fry", activityObject, STARS_ASPECT));
    }

    @Test
    public void shouldRetrieveRatesCountForAnActivityObject() {
        String activityObject = ActivityHelper.createActivityObject(15L);

        ratingService.rate("bender", 5, activityObject, STARS_ASPECT);
        ratingService.rate("fry", 2, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 3, activityObject, STARS_ASPECT);
        assertTrue(ratingService.hasUserRated("bender", activityObject, STARS_ASPECT));
        assertTrue(ratingService.hasUserRated("fry", activityObject, STARS_ASPECT));
        assertTrue(ratingService.hasUserRated("leela", activityObject, STARS_ASPECT));

        long ratesCount = ratingService.getRatesCount(activityObject, STARS_ASPECT);
        assertEquals(3, ratesCount);
    }

    @Test
    public void shouldRetrieveRatesCountOfAGivenRatingForAnActivityObject() {
        String activityObject = ActivityHelper.createActivityObject(15L);

        ratingService.rate("bender", 5, activityObject, STARS_ASPECT);
        ratingService.rate("fry", 2, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 5, activityObject, STARS_ASPECT);
        assertTrue(ratingService.hasUserRated("bender", activityObject, STARS_ASPECT));
        assertTrue(ratingService.hasUserRated("fry", activityObject, STARS_ASPECT));
        assertTrue(ratingService.hasUserRated("leela", activityObject, STARS_ASPECT));

        long ratesCount = ratingService.getRatesCount(activityObject, 5, STARS_ASPECT);
        assertEquals(2, ratesCount);
    }

    @Test
    public void shouldRetrieveUserRatesCountForAnActivityObject() {
        String activityObject = ActivityHelper.createActivityObject(15L);

        ratingService.rate("bender", 3, activityObject, STARS_ASPECT);
        ratingService.rate("bender", 5, activityObject, STARS_ASPECT);
        ratingService.rate("bender", 3, activityObject, STARS_ASPECT);
        ratingService.rate("bender", 4, activityObject, STARS_ASPECT);
        ratingService.rate("bender", 3, activityObject, STARS_ASPECT);
        ratingService.rate("fry", 2, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 3, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 5, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 5, activityObject, STARS_ASPECT);

        long ratesCount = ratingService.getRatesCountForUser("bender", activityObject, 3, STARS_ASPECT);
        assertEquals(3, ratesCount);
        ratesCount = ratingService.getRatesCountForUser("leela", activityObject, 5, STARS_ASPECT);
        assertEquals(2, ratesCount);
        ratesCount = ratingService.getRatesCountForUser("fry", activityObject, 5, STARS_ASPECT);
        assertEquals(0, ratesCount);
    }

    @Test
    public void shouldRetrieveUserRatesCountOfAGivenRatingForAnActivityObject() {
        String activityObject = ActivityHelper.createActivityObject(15L);

        ratingService.rate("bender", 5, activityObject, STARS_ASPECT);
        ratingService.rate("bender", 2, activityObject, STARS_ASPECT);
        ratingService.rate("bender", 3, activityObject, STARS_ASPECT);
        ratingService.rate("fry", 2, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 3, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 5, activityObject, STARS_ASPECT);

        long ratesCount = ratingService.getRatesCountForUser("bender", activityObject, STARS_ASPECT);
        assertEquals(3, ratesCount);
        ratesCount = ratingService.getRatesCountForUser("fry", activityObject, STARS_ASPECT);
        assertEquals(1, ratesCount);
        ratesCount = ratingService.getRatesCountForUser("leela", activityObject, STARS_ASPECT);
        assertEquals(2, ratesCount);
    }

    @Test
    public void shouldRetrieveAverageRatingForAnActivityObject() {
        String activityObject = ActivityHelper.createActivityObject(15L);

        ratingService.rate("bender", 5, activityObject, STARS_ASPECT);
        ratingService.rate("fry", 2, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 3, activityObject, STARS_ASPECT);
        assertTrue(ratingService.hasUserRated("bender", activityObject, STARS_ASPECT));
        assertTrue(ratingService.hasUserRated("fry", activityObject, STARS_ASPECT));
        assertTrue(ratingService.hasUserRated("leela", activityObject, STARS_ASPECT));

        double average = ratingService.getAverageRating(activityObject, STARS_ASPECT);
        assertEquals(3.3, average, 0.1);
    }

    @Test
    public void shouldRetrieveUserAverageRatingForAnActivityObject() {
        String activityObject = ActivityHelper.createActivityObject(15L);

        ratingService.rate("bender", 5, activityObject, STARS_ASPECT);
        ratingService.rate("bender", 1, activityObject, STARS_ASPECT);
        ratingService.rate("bender", 4, activityObject, STARS_ASPECT);
        ratingService.rate("fry", 2, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 3, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 5, activityObject, STARS_ASPECT);

        double average = ratingService.getAverageRatingForUser("bender", activityObject, STARS_ASPECT);
        assertEquals(3.3, average, 0.1);
        average = ratingService.getAverageRatingForUser("leela", activityObject, STARS_ASPECT);
        assertEquals(4, average, 0.1);
    }

    @Test
    public void shouldHandleRatingOnDifferentAspects() {
        String activityObject = ActivityHelper.createActivityObject(15L);

        ratingService.rate("bender", 5, activityObject, STARS_ASPECT);
        ratingService.rate("bender", 2, activityObject, OTHER_ASPECT);
        ratingService.rate("fry", 2, activityObject, STARS_ASPECT);
        ratingService.rate("fry", 1, activityObject, OTHER_ASPECT);
        ratingService.rate("leela", 2, activityObject, STARS_ASPECT);
        ratingService.rate("leela", 3, activityObject, OTHER_ASPECT);

        long ratesCount = ratingService.getRatesCount(activityObject, STARS_ASPECT);
        assertEquals(3, ratesCount);

        ratesCount = ratingService.getRatesCount(activityObject, OTHER_ASPECT);
        assertEquals(3, ratesCount);

        double average = ratingService.getAverageRating(activityObject, STARS_ASPECT);
        assertEquals(3, average, 0.1);
        average = ratingService.getAverageRating(activityObject, OTHER_ASPECT);
        assertEquals(2, average, 0.1);
    }

    @Test
    public void shouldCancelExistingRating() {
        String activityObject = ActivityHelper.createActivityObject(15L);

        ratingService.rate("bender", 5, activityObject, STARS_ASPECT);
        ratingService.rate("fry", 2, activityObject, STARS_ASPECT);
        assertTrue(ratingService.hasUserRated("bender", activityObject, STARS_ASPECT));
        assertTrue(ratingService.hasUserRated("fry", activityObject, STARS_ASPECT));

        ratingService.cancelRate("bender", activityObject, STARS_ASPECT);
        assertFalse(ratingService.hasUserRated("bender", activityObject, STARS_ASPECT));
        assertTrue(ratingService.hasUserRated("fry", activityObject, STARS_ASPECT));

        ratingService.cancelRate("fry", activityObject, STARS_ASPECT);
        assertFalse(ratingService.hasUserRated("bender", activityObject, STARS_ASPECT));
        assertFalse(ratingService.hasUserRated("fry", activityObject, STARS_ASPECT));
    }

    @Test
    public void shouldNotFindRatingWhenDocIsRemoved() {
        DocumentModel doc = createTestDocument("doc1");
        String docActivity = ActivityHelper.createDocumentActivityObject(doc);

        ratingService.rate("Leah", 5, docActivity, STARS_ASPECT);
        assertTrue(ratingService.hasUserRated("Leah", docActivity, STARS_ASPECT));

        DocumentRef docRef = doc.getRef();
        session.removeDocument(docRef);
        session.save();

        assertEquals(0, ratingService.getRatesCount(docActivity, STARS_ASPECT));
    }

    @Test
    public void shouldNotFindRatingWhenDocIsTrashed() {

        DocumentModel doc = createTestDocument("doc1");
        String docActivity = ActivityHelper.createDocumentActivityObject(doc);

        ratingService.rate("Leah", 5, docActivity, STARS_ASPECT);
        assertEquals(1, ratingService.getRatesCount(docActivity, STARS_ASPECT));

        Framework.getService(TrashService.class).trashDocument(doc);

        assertEquals(0, ratingService.getRatesCount(docActivity, STARS_ASPECT));
    }

    @Test
    public void shouldGetLatestRateForUser() {
        String activity1 = ActivityHelper.createDocumentActivityObject(createTestDocument("doc1"));
        String activity2 = ActivityHelper.createDocumentActivityObject(createTestDocument("doc2"));
        String activity3 = ActivityHelper.createDocumentActivityObject(createTestDocument("doc3"));
        String activity4 = ActivityHelper.createDocumentActivityObject(createTestDocument("doc4"));

        String username = "Lise";

        ratingService.rate(username, 5, activity1, STARS_ASPECT);
        ratingService.rate(username, 2, activity2, STARS_ASPECT);
        ratingService.rate(username, 3, activity3, STARS_ASPECT);
        ratingService.rate(username, 5, activity4, STARS_ASPECT);

        ActivitiesList lastestRatedDocByUser = ratingService.getLastestRatedDocByUser(username, STARS_ASPECT, 3);
        assertEquals(3, lastestRatedDocByUser.size());
        assertEquals(activity4, lastestRatedDocByUser.get(0).getTarget());
        assertEquals(activity3, lastestRatedDocByUser.get(1).getTarget());
        assertEquals(activity2, lastestRatedDocByUser.get(2).getTarget());
    }
}
