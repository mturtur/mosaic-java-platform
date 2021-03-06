/*
 * #%L
 * mosaic-examples-simple-cloudlets
 * %%
 * Copyright (C) 2010 - 2013 Institute e-Austria Timisoara (Romania)
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

package eu.mosaic_cloud.examples.cloudlets.v2.simple.tests;


import java.util.UUID;

import eu.mosaic_cloud.components.core.ComponentIdentifier;
import eu.mosaic_cloud.components.core.ComponentResourceDescriptor;
import eu.mosaic_cloud.components.core.ComponentResourceSpecification;
import eu.mosaic_cloud.drivers.interop.AbstractDriverStub;
import eu.mosaic_cloud.drivers.kvstore.riak.interop.KeyValueStub;
import eu.mosaic_cloud.drivers.queue.amqp.interop.AmqpStub;
import eu.mosaic_cloud.interoperability.core.Channel;
import eu.mosaic_cloud.interoperability.core.ChannelFactory;
import eu.mosaic_cloud.interoperability.core.ChannelResolver;
import eu.mosaic_cloud.interoperability.core.ResolverCallbacks;
import eu.mosaic_cloud.interoperability.implementations.zeromq.ZeroMqChannel;
import eu.mosaic_cloud.platform.implementation.v2.cloudlets.core.Cloudlet;
import eu.mosaic_cloud.platform.implementation.v2.configuration.PropertyTypeConfiguration;
import eu.mosaic_cloud.platform.interop.specs.amqp.AmqpSession;
import eu.mosaic_cloud.platform.interop.specs.kvstore.KeyValueSession;
import eu.mosaic_cloud.platform.v2.cloudlets.core.CloudletCallback;
import eu.mosaic_cloud.platform.v2.connectors.component.ComponentConnector;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;
import eu.mosaic_cloud.tools.threading.tools.Threading;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Preconditions;


public abstract class BaseCloudletTest
			extends eu.mosaic_cloud.platform.implementation.v2.cloudlets.tests.BaseCloudletTest<BaseCloudletTest.Scenario<?>>
{
	@Override
	@After
	public void tearDown () {
		if (!this.doRun) {
			return;
		}
		if (this.scenario.amqpDriverStub != null) {
			this.scenario.amqpDriverStub.destroy ();
		}
		if (this.scenario.kvDriverStub != null) {
			this.scenario.kvDriverStub.destroy ();
		}
		if (this.scenario.driversChannel != null) {
			this.scenario.driversChannel.terminate ();
		}
		if (this.scenario.connectorsChannel != null) {
			this.scenario.connectorsChannel.terminate ();
		}
		if (this.cloudlet != null) {
			this.awaitSuccess (this.cloudlet.destroy ());
		}
		eu.mosaic_cloud.platform.implementation.v2.cloudlets.tests.BaseCloudletTest.tearDownScenario (this.scenario);
		this.cloudlet = null;
		this.scenario = null;
	}
	
	@Override
	@Test
	public void test () {
		if (!this.doRun) {
			return;
		}
		if (this.runDelay != 0) {
			Threading.sleep (this.runDelay);
		}
		this.awaitSuccess (this.cloudlet.initialize ());
		Assert.assertTrue (this.cloudlet.await (this.scenario.poolTimeout));
		this.cloudlet = null;
	}
	
	protected <Context> void setUp (final Class<? extends CloudletCallback<Context>> callbacksClass, final Class<Context> contextClass, final String configuration) {
		if (!this.doRun) {
			return;
		}
		final Scenario<Context> scenario = new Scenario<Context> ();
		this.scenario = scenario;
		final ChannelFactory connectorsChannelFactory = new ChannelFactory () {
			@Override
			public Channel create () {
				Preconditions.checkState (scenario.connectorsChannel != null);
				Preconditions.checkState (scenario.driversChannel != null);
				return (scenario.connectorsChannel);
			}
		};
		final ComponentConnector componentConnector = new ComponentConnector () {
			@Override
			public CallbackCompletion<ComponentResourceDescriptor> acquire (final ComponentResourceSpecification resource) {
				throw (new UnsupportedOperationException ());
			}
			
			@Override
			public <TInputs, TOutputs> CallbackCompletion<TOutputs> call (final ComponentIdentifier component, final String operation, final TInputs inputs, final Class<TOutputs> outputs) {
				throw (new UnsupportedOperationException ());
			}
			
			@Override
			public <TInputs> CallbackCompletion<Void> cast (final ComponentIdentifier component, final String operation, final TInputs inputs) {
				throw (new UnsupportedOperationException ());
			}
			
			@Override
			public CallbackCompletion<Void> destroy () {
				throw (new UnsupportedOperationException ());
			}
			
			@Override
			public CallbackCompletion<Void> initialize () {
				throw (new UnsupportedOperationException ());
			}
		};
		final ChannelResolver connectorsChannelResolver = new ChannelResolver () {
			@Override
			public void resolve (final String target, final ResolverCallbacks callbacks) {
				Preconditions.checkNotNull (target);
				Preconditions.checkNotNull (callbacks);
				Preconditions.checkState (scenario.connectorsChannel != null);
				Preconditions.checkState (scenario.driversChannel != null);
				if ("a5e40f0b2c041bc694ace68ace08420d40f9cbc0".equals (target)) {
					callbacks.resolved (this, target, scenario.driversIdentity, scenario.driversEndpoint);
				} else if ("a3e40f0b2c041bc694ace68ace08420d40f9cbc0".equals (target)) {
					callbacks.resolved (this, target, scenario.driversIdentity, scenario.driversEndpoint);
				} else {
					throw (new IllegalArgumentException ());
				}
			}
		};
		eu.mosaic_cloud.platform.implementation.v2.cloudlets.tests.BaseCloudletTest.setUpScenario (this.getClass (), scenario, configuration, callbacksClass, contextClass, componentConnector, connectorsChannelFactory, connectorsChannelResolver);
		{
			scenario.connectorsIdentity = UUID.randomUUID ().toString ();
			scenario.connectorsChannel = ZeroMqChannel.create (scenario.connectorsIdentity, scenario.threading, scenario.exceptions);
		}
		{
			scenario.driversIdentity = UUID.randomUUID ().toString ();
			scenario.driversEndpoint = "inproc://" + scenario.driversIdentity;
			scenario.driversChannel = ZeroMqChannel.create (scenario.driversIdentity, scenario.threading, scenario.exceptions);
			scenario.driversChannel.accept (scenario.driversEndpoint);
		}
		{
			final String host = System.getProperty (BaseCloudletTest.MOSAIC_AMQP_HOST, BaseCloudletTest.MOSAIC_AMQP_HOST_DEFAULT);
			final Integer port = Integer.valueOf (System.getProperty (BaseCloudletTest.MOSAIC_AMQP_PORT, BaseCloudletTest.MOSAIC_AMQP_PORT_DEFAULT));
			final PropertyTypeConfiguration driverConfiguration = PropertyTypeConfiguration.create ();
			driverConfiguration.addParameter ("interop.driver.endpoint", scenario.driversEndpoint);
			driverConfiguration.addParameter ("interop.driver.identity", scenario.driversIdentity);
			driverConfiguration.addParameter ("amqp.host", host);
			driverConfiguration.addParameter ("amqp.port", port);
			driverConfiguration.addParameter ("amqp.driver_threads", Integer.valueOf (1));
			driverConfiguration.addParameter ("consumer.amqp.queue", "tests.queue");
			driverConfiguration.addParameter ("consumer.amqp.consumer_id", "tests.consumer");
			driverConfiguration.addParameter ("consumer.amqp.auto_ack", Boolean.FALSE);
			driverConfiguration.addParameter ("consumer.amqp.exclusive", Boolean.FALSE);
			driverConfiguration.addParameter ("publisher.amqp.exchange", "tests.exchange");
			driverConfiguration.addParameter ("publisher.amqp.routing_key", "tests.routing-key");
			driverConfiguration.addParameter ("publisher.amqp.manadatory", Boolean.TRUE);
			driverConfiguration.addParameter ("publisher.amqp.immediate", Boolean.FALSE);
			driverConfiguration.addParameter ("publisher.amqp.durable", Boolean.FALSE);
			scenario.driversChannel.register (AmqpSession.DRIVER);
			scenario.amqpDriverStub = AmqpStub.createDetached (driverConfiguration, scenario.driversChannel, scenario.threading);
		}
		{
			final String host = System.getProperty (BaseCloudletTest.MOSAIC_RIAK_HOST, BaseCloudletTest.MOSAIC_RIAK_HOST_DEFAULT);
			final Integer port = Integer.valueOf (System.getProperty (BaseCloudletTest.MOSAIC_RIAK_PORT, BaseCloudletTest.MOSAIC_RIAK_PORT_DEFAULT));
			final PropertyTypeConfiguration driverConfiguration = PropertyTypeConfiguration.create ();
			driverConfiguration.addParameter ("interop.driver.endpoint", scenario.driversEndpoint);
			driverConfiguration.addParameter ("interop.driver.identity", scenario.driversIdentity);
			driverConfiguration.addParameter ("kvstore.host", host);
			driverConfiguration.addParameter ("kvstore.port", port);
			driverConfiguration.addParameter ("kvstore.driver_name", "RIAKREST");
			driverConfiguration.addParameter ("kvstore.driver_threads", Integer.valueOf (1));
			driverConfiguration.addParameter ("kvstore.bucket", "tests");
			scenario.driversChannel.register (KeyValueSession.DRIVER);
			scenario.kvDriverStub = KeyValueStub.createDetached (driverConfiguration, scenario.threading, scenario.driversChannel);
		}
		this.cloudlet = Cloudlet.create (this.scenario.environment);
	}
	
	protected boolean doRun = true;
	protected long runDelay = 0;
	private static final String MOSAIC_AMQP_HOST = "mosaic.tests.resources.amqp.host";
	private static final String MOSAIC_AMQP_HOST_DEFAULT = "127.0.0.1";
	private static final String MOSAIC_AMQP_PORT = "mosaic.tests.resources.amqp.port";
	private static final String MOSAIC_AMQP_PORT_DEFAULT = "21688";
	private static final String MOSAIC_RIAK_HOST = "mosaic.tests.resources.riak.host";
	private static final String MOSAIC_RIAK_HOST_DEFAULT = "127.0.0.1";
	private static final String MOSAIC_RIAK_PORT = "mosaic.tests.resources.riakrest.port";
	private static final String MOSAIC_RIAK_PORT_DEFAULT = "24637";
	
	public static class Scenario<Context extends Object>
				extends eu.mosaic_cloud.platform.implementation.v2.cloudlets.tests.BaseCloudletTest.BaseScenario<Context>
	{
		public AbstractDriverStub amqpDriverStub;
		public ZeroMqChannel connectorsChannel;
		public String connectorsIdentity;
		public ZeroMqChannel driversChannel;
		public String driversEndpoint;
		public String driversIdentity;
		public AbstractDriverStub kvDriverStub;
	}
}
