/*
 * (C) Copyright 2012-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Arnaud Kervern, Thomas Roger
 *
 */
package org.nuxeo.ecm.rating;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RunnerFeature;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
@Features(CoreFeature.class)
@Deploy("org.nuxeo.runtime.datasource")
@Deploy("org.nuxeo.ecm.core.persistence")
@Deploy("org.nuxeo.ecm.activity")
@Deploy("org.nuxeo.ecm.rating.api")
@Deploy("org.nuxeo.ecm.rating.core")
@Deploy("org.nuxeo.ecm.rating.core:rating-test.xml")
public class RatingFeature implements RunnerFeature {

    protected static final String DIRECTORY = "target/test/nxactivities";

    protected static final String PROP_NAME = "ds.nxactivities.home";

    protected File dir;

    @Override
    public void initialize(FeaturesRunner runner) {
        dir = new File(DIRECTORY);
        FileUtils.deleteQuietly(dir);
        dir.mkdirs();
        System.setProperty(PROP_NAME, dir.getPath());
    }

    @Override
    public void stop(FeaturesRunner runner) {
        FileUtils.deleteQuietly(dir);
        dir = null;
        System.clearProperty(PROP_NAME);
    }

}
