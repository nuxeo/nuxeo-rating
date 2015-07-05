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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.rating.operations.CancelLike;
import org.nuxeo.ecm.rating.operations.GetLikeStatus;
import org.nuxeo.ecm.rating.operations.Like;
import org.nuxeo.ecm.rating.operations.MostLiked;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@RunWith(FeaturesRunner.class)
@Features(RatingFeature.class)
@Deploy({ "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.automation.io", "org.nuxeo.ecm.webengine.core",
        "org.nuxeo.ecm.core.io" })
@SuppressWarnings("boxing")
public class TestLikeOperations extends AbstractRatingTest {

    @Inject
    protected AutomationService service;

    @Test
    public void shouldReturnLikeStatusForDocument() throws Exception {
        DocumentModel doc = createTestDocument("file1");

        likeService.like("bender", doc);
        likeService.like("leela", doc);
        likeService.like("fry", doc);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testLikeOperation");
        chain.add(GetLikeStatus.ID).set("document", doc.getRef());
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(3, object.getLong("likesCount"));
        assertEquals(0, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(0, object.getInt("userLikeStatus"));

        likeService.like("Administrator", doc);
        result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        json = result.getString();
        assertNotNull(json);

        object = JSONObject.fromObject(json);
        assertEquals(4, object.getLong("likesCount"));
        assertEquals(0, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(1, object.getInt("userLikeStatus"));
    }

    @Test
    public void shouldReturnLikeStatusForAnActivityObject() throws Exception {
        likeService.like("bender", activityObject);
        likeService.like("leela", activityObject);
        likeService.dislike("fry", activityObject);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testLikeOperation");
        chain.add(GetLikeStatus.ID).set("activityId", String.valueOf(activity.getId()));
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(2, object.getLong("likesCount"));
        assertEquals(1, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(0, object.getInt("userLikeStatus"));

        likeService.dislike("Administrator", activityObject);
        result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        json = result.getString();
        assertNotNull(json);

        object = JSONObject.fromObject(json);
        assertEquals(2, object.getLong("likesCount"));
        assertEquals(2, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(-1, object.getInt("userLikeStatus"));
    }

    @Test
    public void shouldLikeADocument() throws Exception {
        DocumentModel doc = createTestDocument("file1");

        likeService.like("bender", doc);
        likeService.like("leela", doc);
        likeService.like("fry", doc);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testLikeOperation");
        chain.add(GetLikeStatus.ID).set("document", doc.getRef());
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(3, object.getLong("likesCount"));
        assertEquals(0, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(0, object.getInt("userLikeStatus"));

        chain = new OperationChain("testLikeOperation");
        chain.add(Like.ID).set("document", doc.getRef());
        result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        json = result.getString();
        assertNotNull(json);

        object = JSONObject.fromObject(json);
        assertEquals(4, object.getLong("likesCount"));
        assertEquals(0, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(1, object.getInt("userLikeStatus"));

        // Liking the same document should not change the likes count
        result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        json = result.getString();
        assertNotNull(json);

        object = JSONObject.fromObject(json);
        assertEquals(4, object.getLong("likesCount"));
        assertEquals(0, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(1, object.getInt("userLikeStatus"));
    }

    @Test
    public void shouldLikeAnActivityObject() throws Exception {
        likeService.like("bender", activityObject);
        likeService.like("leela", activityObject);
        likeService.dislike("fry", activityObject);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testLikeOperation");
        chain.add(GetLikeStatus.ID).set("activityId", String.valueOf(activity.getId()));
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(2, object.getLong("likesCount"));
        assertEquals(1, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(0, object.getInt("userLikeStatus"));

        chain = new OperationChain("testLikeOperation");
        chain.add(Like.ID).set("activityId", String.valueOf(String.valueOf(activity.getId())));
        result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        json = result.getString();
        assertNotNull(json);

        object = JSONObject.fromObject(json);
        assertEquals(3, object.getLong("likesCount"));
        assertEquals(1, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(1, object.getInt("userLikeStatus"));
    }

    @Test
    public void shouldCancelLikeOnADocument() throws Exception {
        DocumentModel doc = createTestDocument("file1");

        likeService.like("bender", doc);
        likeService.like("leela", doc);
        likeService.like("fry", doc);
        likeService.like("Administrator", doc);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testLikeOperation");
        chain.add(GetLikeStatus.ID).set("document", doc.getRef());
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(4, object.getLong("likesCount"));
        assertEquals(0, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(1, object.getInt("userLikeStatus"));

        chain = new OperationChain("testLikeOperation");
        chain.add(CancelLike.ID).set("document", doc.getRef());
        result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        json = result.getString();
        assertNotNull(json);

        object = JSONObject.fromObject(json);
        assertEquals(3, object.getLong("likesCount"));
        assertEquals(0, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(0, object.getInt("userLikeStatus"));

        // Canceling on the same document should not change the likes count
        // and the userLikeStatus
        result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        json = result.getString();
        assertNotNull(json);

        object = JSONObject.fromObject(json);
        assertEquals(3, object.getLong("likesCount"));
        assertEquals(0, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(0, object.getInt("userLikeStatus"));
    }

    @Test
    public void shouldCancelLikeOnAnActivityObject() throws Exception {
        likeService.like("bender", activityObject);
        likeService.like("leela", activityObject);
        likeService.like("Administrator", activityObject);
        likeService.dislike("fry", activityObject);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testLikeOperation");
        chain.add(GetLikeStatus.ID).set("activityId", String.valueOf(activity.getId()));
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(3, object.getLong("likesCount"));
        assertEquals(1, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(1, object.getInt("userLikeStatus"));

        chain = new OperationChain("testLikeOperation");
        chain.add(CancelLike.ID).set("activityId", String.valueOf(activity.getId()));
        result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        json = result.getString();
        assertNotNull(json);

        object = JSONObject.fromObject(json);
        assertEquals(2, object.getLong("likesCount"));
        assertEquals(1, object.getLong("dislikesCount"));
        assertEquals("Administrator", object.get("username"));
        assertEquals(0, object.getInt("userLikeStatus"));
    }

    @Test
    public void shouldReturnMostLikedDocumentAsJson() throws Exception {
        initWithDefaultRepository();

        DocumentModel myDoc = createTestDocument("test", "/default-domain/workspaces/test");
        likeService.like("Robin", myDoc);
        likeService.like("Barney", myDoc);

        DocumentModel test2 = createTestDocument("test2", "/default-domain/workspaces/test");
        likeService.like("Robin", test2);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testLikeOperation");
        chain.add(MostLiked.ID).set("limit", 5).set("contextPath", "/default-domain/");
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        JSONArray items = object.getJSONArray("items");
        assertEquals(2, items.size());

        JSONObject firstDocRated = items.getJSONObject(0);
        assertEquals(2, firstDocRated.getInt("rating"));
        assertEquals(myDoc.getId(), firstDocRated.getJSONObject("document").getString("uid"));
    }

    protected void initWithDefaultRepository() {
        new DefaultRepositoryInit().populate(session);
        session.save();
    }
}
