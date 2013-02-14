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

import java.io.ByteArrayInputStream;

import net.sf.json.JSONObject;

import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.rating.api.LikeService;
import org.nuxeo.ecm.rating.api.LikeStatus;

/**
 * Operation to get a {@see LikeStatus} for a specified document or activity
 * object.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@Operation(id = GetLikeStatus.ID, category = Constants.CAT_SERVICES, label = "Get the LikeStatus for a document or an activity", description = "Get the LikeStatus for a document or an activity."
        + "One of the 'document' or 'activityObject' must be set.")
public class GetLikeStatus {

    public static final String ID = "Services.GetLikeStatus";

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
            status = likeService.getLikeStatus(username, doc);
        } else if (activityId != null) {
            String activityObject = ActivityHelper.createActivityObject(activityId);
            status = likeService.getLikeStatus(username, activityObject);
        } else {
            throw new OperationException(
                    "'document' or 'activityId' parameter must be set.");
        }

        JSONObject json = JSONObject.fromObject(status.toMap());
        return new InputStreamBlob(new ByteArrayInputStream(
                json.toString().getBytes("UTF-8")), "application/json");
    }

}
