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

package org.nuxeo.ecm.rating.operations;

import net.sf.json.JSONObject;

import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.rating.api.LikeService;
import org.nuxeo.ecm.rating.api.LikeStatus;

/**
 * Operation to like a document or activity object.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@Operation(id = Like.ID, category = Constants.CAT_SERVICES, label = "Like a document or an activity object", description = "Like a document or an activity object."
        + "One of the 'document' or 'activityObject' must be set."
        + "Returns the related LikeStatus once the action is done.")
public class Like {

    public static final String ID = "Services.Like";

    @Context
    protected CoreSession session;

    @Context
    protected LikeService likeService;

    @Param(name = "document", required = false)
    protected DocumentModel doc;

    @Param(name = "activityId", required = false)
    protected String activityId;

    @OperationMethod
    public Blob run() throws Exception {
        String username = session.getPrincipal().getName();
        LikeStatus status;
        if (doc != null) {
            likeService.like(username, doc);
            status = likeService.getLikeStatus(username, doc);
        } else if (activityId != null) {
            String activityObject = ActivityHelper.createActivityObject(activityId);
            likeService.like(username, activityObject);
            status = likeService.getLikeStatus(username, activityObject);
        } else {
            throw new OperationException("'document' or 'activityId' parameter must be set.");
        }

        JSONObject json = JSONObject.fromObject(status.toMap());
        return Blobs.createBlob(json.toString(), "application/json");
    }

}
