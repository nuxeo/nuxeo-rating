/*
 * (C) Copyright 2012-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.SimpleFeature;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
@Features(CoreFeature.class)
@Deploy({ "org.nuxeo.runtime.datasource", "org.nuxeo.ecm.core.persistence", "org.nuxeo.ecm.activity",
        "org.nuxeo.ecm.rating.api", "org.nuxeo.ecm.rating.core" })
@LocalDeploy("org.nuxeo.ecm.rating.core:rating-test.xml")
public class RatingFeature extends SimpleFeature {

    protected static final String DIRECTORY = "target/test/nxactivities";

    protected static final String PROP_NAME = "ds.nxactivities.home";

    protected File dir;

    @Override
    public void initialize(FeaturesRunner runner) throws Exception {
        dir = new File(DIRECTORY);
        FileUtils.deleteQuietly(dir);
        dir.mkdirs();
        System.setProperty(PROP_NAME, dir.getPath());
        super.initialize(runner);
    }

    @Override
    public void stop(FeaturesRunner runner) throws Exception {
        FileUtils.deleteQuietly(dir);
        dir = null;
        System.clearProperty(PROP_NAME);
        super.stop(runner);
    }

}
