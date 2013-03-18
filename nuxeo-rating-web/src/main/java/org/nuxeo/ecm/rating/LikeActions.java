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
        LikeService likeService = Framework.getLocalService(LikeService.class);
        return likeService.hasUserLiked(principal.getName(), doc);
    }

    public void like(Principal principal, DocumentModel doc) {
        LikeService likeService = Framework.getLocalService(LikeService.class);
        likeService.like(principal.getName(), doc);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "label.document.liked");
    }

    public void cancelLike(Principal principal, DocumentModel doc) {
        LikeService likeService = Framework.getLocalService(LikeService.class);
        likeService.cancel(principal.getName(), doc);
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO,
                "label.document.unliked");
    }

    public long getLikesCount(DocumentModel doc) {
        LikeService likeService = Framework.getLocalService(LikeService.class);
        return likeService.getLikesCount(doc);
    }

}
