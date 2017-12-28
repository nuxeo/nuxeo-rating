/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Arnaud Kervern
 */
package org.nuxeo.ecm.rating.operations;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.nuxeo.ecm.activity.ActivityHelper.getActivityId;
import static org.nuxeo.ecm.activity.ActivityHelper.getDocumentId;
import static org.nuxeo.ecm.activity.ActivityHelper.getUsername;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.ActivitiesList;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityMessageHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.jaxrs.io.JsonHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.core.io.marshallers.json.document.DocumentModelJsonWriter;
import org.nuxeo.ecm.core.io.registry.MarshallerRegistry;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext.CtxBuilder;
import org.nuxeo.ecm.platform.types.adapter.TypeInfo;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.api.DocumentViewCodecManager;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.ecm.rating.api.LikeService;
import org.nuxeo.runtime.api.Framework;

import com.fasterxml.jackson.core.JsonGenerator;

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

    @Context
    protected OperationContext ctx;

    @Param(name = "contextPath")
    protected String contextPath;

    @Param(name = "limit")
    protected int limit;

    @Param(name = "fromDt", required = false)
    protected Date fromDt;

    @Param(name = "toDt", required = false)
    protected Date toDt;

    @Param(name = "documentLinkBuilder", required = false)
    protected String documentLinkBuilder;

    @OperationMethod
    public Blob run() throws IOException {
        ActivitiesList mostLikedDocuments = likeService.getMostLikedActivities(session, limit,
                session.getDocument(new PathRef(contextPath)), fromDt, toDt);

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
        jsonObj.put("items", JSONArray.fromObject(docsWithRate));
        JSONObject json = JSONObject.fromObject(jsonObj);
        return Blobs.createJSONBlob(json.toString());
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
        value.put("profile",
                ActivityMessageHelper.getUserProfileLink(miniMessage.getActor(), miniMessage.getDisplayActor()));
        value.put("hasUserLiked", hasRated);

        return JSONObject.fromObject(value);
    }

    protected JSONObject buildFromDocument(Activity activity) throws IOException {
        DocumentModel doc = session.getDocument(new IdRef(getDocumentId(activity.getTarget())));
        Integer rating = Integer.valueOf(activity.getObject());
        Integer hasRated = Integer.valueOf(activity.getContext());

        OutputStream out = new ByteArrayOutputStream();
        JsonGenerator jg = JsonHelper.createJsonGenerator(out);

        writeDocument(doc, jg);

        Map<String, Object> value = new HashMap<String, Object>();
        value.put("rating", rating);
        value.put("document", JSONObject.fromObject(out.toString()));
        value.put("url", getDocumentUrl(doc));
        value.put("hasUserLiked", hasRated);
        value.put("type", "document");

        return JSONObject.fromObject(value);
    }

    private static DocumentModelJsonWriter documentModelWriter;

    private static void writeDocument(DocumentModel doc, JsonGenerator jg) throws IOException {
        if (documentModelWriter == null) {
            RenderingContext ctx = CtxBuilder.properties("dublincore", "common").get();
            MarshallerRegistry registry = Framework.getService(MarshallerRegistry.class);
            documentModelWriter = registry.getUniqueInstance(ctx, DocumentModelJsonWriter.class);
        }
        documentModelWriter.write(doc, jg);
        jg.flush();
    }

    protected String getDocumentUrl(DocumentModel doc) {
        if (Framework.isTestModeSet()) {
            return "http://dummyurl.com";
        }

        DocumentViewCodecManager documentViewCodecManager = Framework.getService(DocumentViewCodecManager.class);
        String codecName = isBlank(documentLinkBuilder) ? documentViewCodecManager.getDefaultCodecName()
                : documentLinkBuilder;

        DocumentLocation docLoc = new DocumentLocationImpl(session.getRepositoryName(), doc.getRef());
        DocumentView docView = new DocumentViewImpl(docLoc, doc.getAdapter(TypeInfo.class).getDefaultView());
        return VirtualHostHelper.getContextPathProperty() + "/"
                + documentViewCodecManager.getUrlFromDocumentView(codecName, docView, false, null);
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
