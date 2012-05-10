package org.nuxeo.ecm.rating.operations;

import static org.nuxeo.ecm.automation.server.jaxrs.io.writers.JsonDocumentWriter.writeDocument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.ecm.rating.api.LikeService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
@Operation(id = MostLiked.ID, category = Constants.CAT_SERVICES, label = "Like a document or an activity object")
public class MostLiked {
    public static final String ID = "Services.MostLiked";

    @Context
    protected CoreSession session;

    @Context
    protected LikeService likeService;

    @Param(name = "contextPath")
    protected String contextPath;

    @Param(name = "limit")
    protected int limit;

    @OperationMethod
    public Blob run() throws Exception {
        Map<DocumentModel, Integer> mostLikedDocuments = likeService.getMostLikedDocuments(
                session, limit, session.getDocument(new PathRef(contextPath)));

        final List<JSONObject> docsWithRate = new ArrayList<JSONObject>();
        for (Map.Entry<DocumentModel, Integer> entry : mostLikedDocuments.entrySet()) {
            DocumentModel doc = entry.getKey();
            Integer rating = entry.getValue();

            OutputStream out = new ByteArrayOutputStream();
            writeDocument(out, doc, new String[] { "dublincore" });

            Map<String, Object> value = new HashMap<String, Object>();
            value.put("rating", rating);
            value.put("document", new JSONObject(out.toString()));
            value.put("url", getDocumentUrl(doc.getId()));
            docsWithRate.add(new JSONObject(value));
        }

        Map<String, Object> jsonObj = new HashMap<String, Object>();
        jsonObj.put("items", new JSONArray(docsWithRate));
        return new InputStreamBlob(new ByteArrayInputStream(
                jsonObj.toString().getBytes("UTF-8")), "application/json");
    }

    protected String getDocumentUrl(String documentId) {
        if (Framework.isTestModeSet()) {
            return "http://dummyurl.com";
        }

        DocumentLocation docLoc = new DocumentLocationImpl(
                session.getRepositoryName(), new IdRef(documentId));
        DocumentView docView = new DocumentViewImpl(docLoc, "view_documents");
        URLPolicyService urlPolicyService = Framework.getLocalService(URLPolicyService.class);
        return VirtualHostHelper.getContextPathProperty() + "/"
                + urlPolicyService.getUrlFromDocumentView("id", docView, null);
    }
}
