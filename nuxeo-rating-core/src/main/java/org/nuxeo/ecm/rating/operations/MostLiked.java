package org.nuxeo.ecm.rating.operations;

import static org.nuxeo.ecm.activity.ActivityHelper.getActivityId;
import static org.nuxeo.ecm.activity.ActivityHelper.getDocumentId;
import static org.nuxeo.ecm.activity.ActivityHelper.getUsername;
import static org.nuxeo.ecm.automation.server.jaxrs.io.writers.JsonDocumentWriter.writeDocument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityMessageHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
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
    private static final Log log = LogFactory.getLog(MostLiked.class);

    public static final String ID = "Services.MostLiked";

    public static Pattern HTTP_URL_PATTERN = Pattern.compile("\\b(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");

    @Context
    protected CoreSession session;

    @Context
    protected LikeService likeService;

    @Context
    protected ActivityStreamService activityService;

    @Param(name = "contextPath")
    protected String contextPath;

    @Param(name = "limit")
    protected int limit;

    @OperationMethod
    public Blob run() throws Exception {
        ActivitiesList mostLikedDocuments = likeService.getMostLikedActivities(
                session, limit, session.getDocument(new PathRef(contextPath)));

        final List<JSONObject> docsWithRate = new ArrayList<JSONObject>();
        for (Activity activity : mostLikedDocuments) {
            if (ActivityHelper.isDocument(activity.getTarget())) {
                docsWithRate.add(buildFromDocument(activity));
            } else if (ActivityHelper.isActivity(activity.getTarget())) {
                docsWithRate.add(buildFromActivity(activity));
            } else {
                log.info("Unable to check activity type ...");
            }
        }

        Map<String, Object> jsonObj = new HashMap<String, Object>();
        jsonObj.put("items", new JSONArray(docsWithRate));
        return new InputStreamBlob(new ByteArrayInputStream(new JSONObject(
                jsonObj).toString().getBytes("UTF-8")), "application/json");
    }

    protected JSONObject buildFromActivity(Activity activity) {
        Activity miniMessage = activityService.getActivity(Long.valueOf(getActivityId(activity.getTarget())));
        String message = MostLiked.replaceURLsByLinks(miniMessage.getObject());
        Integer rating = Integer.valueOf(activity.getObject());
        Integer hasRated = Integer.valueOf(activity.getContext());

        Map<String, Object> value = new HashMap<String, Object>();
        value.put("type", "minimessage");
        value.put("message", message);
        value.put("rating", rating);
        value.put("actor", getUsername(miniMessage.getActor()));
        value.put("profile", ActivityMessageHelper.getUserProfileLink(
                miniMessage.getActor(), miniMessage.getDisplayActor()));
        value.put("hasUserLiked", hasRated);

        return new JSONObject(value);
    }

    protected JSONObject buildFromDocument(Activity activity) throws Exception {
        DocumentModel doc = session.getDocument(new IdRef(
                getDocumentId(activity.getTarget())));
        Integer rating = Integer.valueOf(activity.getObject());
        Integer hasRated = Integer.valueOf(activity.getContext());

        OutputStream out = new ByteArrayOutputStream();
        writeDocument(out, doc, new String[] { "dublincore", "common" });

        Map<String, Object> value = new HashMap<String, Object>();
        value.put("rating", rating);
        value.put("document", new JSONObject(out.toString()));
        value.put("url", getDocumentUrl(doc.getId()));
        value.put("hasUserLiked", hasRated);
        value.put("type", "document");

        return new JSONObject(value);
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

    protected static String replaceURLsByLinks(String message) {
        String escapedMessage = StringEscapeUtils.escapeHtml(message);
        Matcher m = HTTP_URL_PATTERN.matcher(escapedMessage);
        StringBuffer sb = new StringBuffer(escapedMessage.length());
        while (m.find()) {
            String url = m.group(1);
            m.appendReplacement(sb, computeLinkFor(url));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    protected static String computeLinkFor(String url) {
        return "<a href=\"" + url + "\" target=\"_top\">" + url + "</a>";
    }
}
