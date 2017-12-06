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

import static org.jboss.seam.ScopeType.STATELESS;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;
import java.security.Principal;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.rating.api.LikeService;
import org.nuxeo.runtime.api.Framework;

/**
 * Handles Like related web actions.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.6
 */
@Name("likeActions")
@Scope(STATELESS)
@Install(precedence = FRAMEWORK)
public class LikeActions implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    public boolean hasUserLiked(Principal principal, DocumentModel doc) {
        LikeService likeService = Framework.getService(LikeService.class);
        return likeService.hasUserLiked(principal.getName(), doc);
    }

    public void like(Principal principal, DocumentModel doc) {
        LikeService likeService = Framework.getService(LikeService.class);
        likeService.like(principal.getName(), doc);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "label.document.liked");
    }

    public void cancelLike(Principal principal, DocumentModel doc) {
        LikeService likeService = Framework.getService(LikeService.class);
        likeService.cancel(principal.getName(), doc);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "label.document.unliked");
    }

    public long getLikesCount(DocumentModel doc) {
        LikeService likeService = Framework.getService(LikeService.class);
        return likeService.getLikesCount(doc);
    }

}
