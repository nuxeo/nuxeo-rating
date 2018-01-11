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

package org.nuxeo.ecm.rating.operations;

import java.io.IOException;

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
 * Operation to get a {@see LikeStatus} for a specified document or activity object.
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
    public Blob run() throws OperationException, IOException {
        String username = session.getPrincipal().getName();
        LikeStatus status;
        if (doc != null) {
            status = likeService.getLikeStatus(username, doc);
        } else if (activityId != null) {
            String activityObject = ActivityHelper.createActivityObject(activityId);
            status = likeService.getLikeStatus(username, activityObject);
        } else {
            throw new OperationException("'document' or 'activityId' parameter must be set.");
        }

        return Blobs.createJSONBlobFromValue(status.toMap());
    }

}
