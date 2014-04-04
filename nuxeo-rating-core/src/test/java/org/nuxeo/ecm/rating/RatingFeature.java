package org.nuxeo.ecm.rating;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.runners.model.FrameworkMethod;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.storage.sql.ra.PoolingRepositoryFactory;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.TransactionalFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.core.test.annotations.TransactionalConfig;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.SimpleFeature;

/**
 * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
 */
@Features({ TransactionalFeature.class, CoreFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD, repositoryFactoryClass = PoolingRepositoryFactory.class)
@Deploy({ "org.nuxeo.runtime.datasource", "org.nuxeo.ecm.core.persistence",
        "org.nuxeo.ecm.activity", "org.nuxeo.ecm.rating.api",
        "org.nuxeo.ecm.rating.core" })
public class RatingFeature extends SimpleFeature {

    protected static final String DIRECTORY = "target/test/nxactivities";

    protected static final String PROP_NAME = "ds.nxactivities.home";

    protected File dir;

    @Override
    public void initialize(FeaturesRunner runner) throws Exception {
        dir = new File(DIRECTORY);
        FileUtils.deleteTree(dir);
        dir.mkdirs();
        System.setProperty(PROP_NAME, dir.getPath());
        super.initialize(runner);
    }

    @Override
    public void stop(FeaturesRunner runner) throws Exception {
        FileUtils.deleteTree(dir);
        dir = null;
        super.stop(runner);
    }

}
