package org.nuxeo.ecm.rating;

import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.SimpleFeature;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
@Features(PlatformFeature.class)
@Deploy({ "org.nuxeo.ecm.core.persistence", "org.nuxeo.ecm.activity",
        "org.nuxeo.ecm.rating.api", "org.nuxeo.ecm.rating.core" })
public class RatingFeature extends SimpleFeature {

}
