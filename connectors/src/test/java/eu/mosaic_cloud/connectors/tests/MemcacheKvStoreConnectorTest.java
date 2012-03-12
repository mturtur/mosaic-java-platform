/*
 * #%L
 * mosaic-connectors
 * %%
 * Copyright (C) 2010 - 2012 Institute e-Austria Timisoara (Romania)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package eu.mosaic_cloud.connectors.tests;

import java.util.Arrays;
import java.util.Map;

import eu.mosaic_cloud.connectors.kvstore.memcache.MemcacheKvStoreConnector;
import eu.mosaic_cloud.connectors.tools.ConnectorConfiguration;
import eu.mosaic_cloud.drivers.interop.kvstore.memcached.MemcachedStub;
import eu.mosaic_cloud.platform.core.configuration.IConfiguration;
import eu.mosaic_cloud.platform.core.configuration.PropertyTypeConfiguration;
import eu.mosaic_cloud.platform.core.utils.PojoDataEncoder;
import eu.mosaic_cloud.platform.interop.specs.kvstore.KeyValueSession;
import eu.mosaic_cloud.platform.interop.specs.kvstore.MemcachedSession;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MemcacheKvStoreConnectorTest extends
        BaseKvStoreConnectorTest<MemcacheKvStoreConnector<String>> {

    private static final String MOSAIC_MEMCACHED_PORT = "mosaic.tests.resources.memcached.port";
    private static final String MOSAIC_MEMCACHED_HOST = "mosaic.tests.resources.memcached.host";
    private static Scenario scenario_;

    @BeforeClass
    public static void setUpBeforeClass() {
        final IConfiguration configuration = PropertyTypeConfiguration.create();

        // configuration.addParameter("interop.channel.address", "inproc://");
        configuration.addParameter("interop.channel.address", "tcp://127.0.0.1:31029");
        configuration.addParameter("interop.driver.identifier", "driver.memcached.1");

        final String host = System.getProperty(MemcacheKvStoreConnectorTest.MOSAIC_MEMCACHED_HOST,
                "127.0.0.1");
        configuration.addParameter("memcached.host_1", host);
        final int port = Integer.parseInt(System.getProperty(
                MemcacheKvStoreConnectorTest.MOSAIC_MEMCACHED_PORT, "8091"));
        configuration.addParameter("memcached.port_1", port);
        configuration.addParameter("kvstore.driver_name", "MEMCACHED");
        configuration.addParameter("kvstore.driver_threads", 2);
        configuration.addParameter("kvstore.bucket", "test");
        configuration.addParameter("kvstore.user", "test");
        configuration.addParameter("kvstore.passwd", "test");

        BaseConnectorTest.setUpScenario(MemcacheKvStoreConnectorTest.class);
        final Scenario scenario = new Scenario(MemcacheKvStoreConnectorTest.class, configuration);

        scenario.registerDriverRole(KeyValueSession.DRIVER);
        scenario.registerDriverRole(MemcachedSession.DRIVER);
        BaseConnectorTest.driverStub = MemcachedStub.createDetached(configuration,
                scenario.getDriverChannel(), scenario.getThreading());
        MemcacheKvStoreConnectorTest.scenario_ = scenario;
    }

    @AfterClass
    public static void tearDownAfterClass() {
        BaseConnectorTest.tearDownScenario(MemcacheKvStoreConnectorTest.scenario_);
    }

    @Override
    public void setUp() {
        this.scenario = MemcacheKvStoreConnectorTest.scenario_;
        final ConnectorConfiguration configuration = ConnectorConfiguration.create(
                this.scenario.getConfiguration(), this.scenario.getEnvironment());
        this.connector = MemcacheKvStoreConnector.create(configuration,
                new PojoDataEncoder<String>(String.class));
    }

    @Override
    @Test
    public void test() {
        this.testConnector();
        this.testSet();
        this.testGet();
        this.testGetBulk();
        this.testAdd();
        this.testReplace();
        this.testCas();
        this.testList();
        this.testDelete();
    }

    protected void testAdd() {
        final String k1 = this.scenario.keyPrefix + "_key_fantastic";
        final String k2 = this.scenario.keyPrefix + "_key_fabulous";
        Assert.assertFalse(this.awaitBooleanOutcome(this.connector.add(k1, 30, "wrong")));
        Assert.assertTrue(this.awaitBooleanOutcome(this.connector.add(k2, 30, "fabulous")));
    }

    protected void testAppend() {
        final String k1 = this.scenario.keyPrefix + "_key_fabulous";
        Assert.assertTrue(this.awaitBooleanOutcome(this.connector.append(k1, " and miraculous")));
        Assert.assertEquals("fantabulous and miraculous", this.awaitOutcome(this.connector.get(k1)));
    }

    protected void testCas() {
        final String k1 = this.scenario.keyPrefix + "_key_fabulous";
        Assert.assertTrue(this.awaitBooleanOutcome(this.connector.cas(k1, "replaced by dummy")));
        Assert.assertEquals("replaced by dummy", this.awaitOutcome(this.connector.get(k1)));
    }

    protected void testGetBulk() {
        final String k1 = this.scenario.keyPrefix + "_key_fantastic";
        final String k2 = this.scenario.keyPrefix + "_key_famous";
        final Map<String, String> values = this.awaitOutcome(this.connector.getBulk(Arrays.asList(
                k1, k2)));
        Assert.assertNotNull(values);
        Assert.assertEquals("fantastic", values.get(k1));
        Assert.assertEquals("famous", values.get(k2));
    }

    @Override
    protected void testList() {
        Assert.assertNull(this.awaitOutcome(this.connector.list()));
    }

    protected void testPrepend() {
        final String k1 = this.scenario.keyPrefix + "_key_fabulous";
        Assert.assertTrue(this.awaitBooleanOutcome(this.connector.prepend(k1, "it is ")));
        Assert.assertEquals("it is fantabulous and miraculous",
                this.awaitOutcome(this.connector.get(k1)));
    }

    protected void testReplace() {
        final String k1 = this.scenario.keyPrefix + "_key_fabulous";
        Assert.assertTrue(this.awaitBooleanOutcome(this.connector.replace(k1, 30, "fantabulous")));
        Assert.assertEquals("fantabulous", this.awaitOutcome(this.connector.get(k1)));
    }

    @Override
    protected void testSet() {
        final String k1 = this.scenario.keyPrefix + "_key_fantastic";
        final String k2 = this.scenario.keyPrefix + "_key_famous";
        Assert.assertTrue(this.awaitBooleanOutcome(this.connector.set(k1, 30, "fantastic")));
        Assert.assertTrue(this.awaitBooleanOutcome(this.connector.set(k2, 30, "famous")));
    }
}
