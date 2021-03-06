/*
 * #%L
 * mosaic-drivers-stubs-kv-common
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

package eu.mosaic_cloud.drivers.kvstore.riak.component;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import eu.mosaic_cloud.components.core.ComponentAcquireReply;
import eu.mosaic_cloud.components.core.ComponentCallReference;
import eu.mosaic_cloud.components.core.ComponentCallReply;
import eu.mosaic_cloud.components.core.ComponentCallRequest;
import eu.mosaic_cloud.components.core.ComponentController;
import eu.mosaic_cloud.components.core.ComponentEnvironment;
import eu.mosaic_cloud.components.core.ComponentIdentifier;
import eu.mosaic_cloud.drivers.ConfigProperties;
import eu.mosaic_cloud.drivers.component.AbstractDriverComponentCallbacks;
import eu.mosaic_cloud.drivers.kvstore.riak.KeyValueDriverFactory;
import eu.mosaic_cloud.drivers.kvstore.riak.interop.KeyValueStub;
import eu.mosaic_cloud.interoperability.implementations.zeromq.ZeroMqChannel;
import eu.mosaic_cloud.platform.implementation.v2.configuration.ConfigUtils;
import eu.mosaic_cloud.platform.implementation.v2.configuration.PropertyTypeConfiguration;
import eu.mosaic_cloud.platform.interop.specs.kvstore.KeyValueSession;
import eu.mosaic_cloud.platform.v2.configuration.Configuration;
import eu.mosaic_cloud.platform.v2.configuration.ConfigurationIdentifier;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;

import com.google.common.base.Preconditions;


/**
 * This callback class enables the Key Value store driver to be exposed as a component. Upon initialization it will look for a
 * Key Value store server and will create a driver object for the server.
 * 
 * @author Georgiana Macariu
 */
public final class KvDriverComponentCallbacks
			extends AbstractDriverComponentCallbacks
{
	/**
	 * Creates a driver callback.
	 */
	public KvDriverComponentCallbacks (final ComponentEnvironment context) {
		super (context);
		try {
			final Configuration configuration = PropertyTypeConfiguration.create (KvDriverComponentCallbacks.class.getResourceAsStream ("driver-component.properties"));
			this.setDriverConfiguration (configuration);
			this.resourceGroup = ComponentIdentifier.resolve (ConfigUtils.resolveParameter (this.getDriverConfiguration (), ConfigProperties.KVDriverComponentCallbacks_0, String.class, ""));
			this.selfGroup = ComponentIdentifier.resolve (ConfigUtils.resolveParameter (this.getDriverConfiguration (), ConfigProperties.KVDriverComponentCallbacks_1, String.class, ""));
			this.driverName = ConfigUtils.resolveParameter (this.getDriverConfiguration (), ConfigProperties.KVStoreDriver_6, String.class, "");
			this.status = Status.Created;
		} catch (final IOException e) {
			this.exceptions.traceIgnoredException (e);
		}
	}
	
	@Override
	public final CallbackCompletion<Void> acquireReturned (final ComponentController component, final ComponentAcquireReply reply) {
		throw (new IllegalStateException ());
	}
	
	@Override
	public CallbackCompletion<Void> called (final ComponentController component, final ComponentCallRequest request) {
		Preconditions.checkState (this.component == component);
		Preconditions.checkState ((this.status != KvDriverComponentCallbacks.Status.Terminated) && (this.status != KvDriverComponentCallbacks.Status.Unregistered));
		if (this.status == KvDriverComponentCallbacks.Status.Registered) {
			if (request.operation.equals (ConfigProperties.KVDriverComponentCallbacks_5)) {
				String channelEndpoint = ConfigUtils.resolveParameter (this.getDriverConfiguration (), ConfigProperties.KVDriverComponentCallbacks_3, String.class, "");
				// FIXME: These parameters should be determined through component "resource acquire" operations.
				//-- Also this hack reduces the number of driver instances of the same type to one per VM.
				try {
					if (System.getenv ("mosaic_node_ip") != null) {
						channelEndpoint = channelEndpoint.replace ("0.0.0.0", System.getenv ("mosaic_node_ip"));
					} else {
						channelEndpoint = channelEndpoint.replace ("0.0.0.0", InetAddress.getLocalHost ().getHostAddress ());
					}
				} catch (final UnknownHostException e) {
					this.exceptions.traceIgnoredException (e);
				}
				final String channelId = ConfigUtils.resolveParameter (this.getDriverConfiguration (), ConfigProperties.KVDriverComponentCallbacks_4, String.class, "");
				final Map<String, String> outcome = new HashMap<String, String> ();
				outcome.put ("channelEndpoint", channelEndpoint);
				outcome.put ("channelIdentifier", channelId);
				final ComponentCallReply reply = ComponentCallReply.create (true, outcome, ByteBuffer.allocate (0), request.reference);
				component.callReturn (reply);
			} else {
				throw new UnsupportedOperationException ();
			}
		} else {
			throw new UnsupportedOperationException ();
		}
		return null;
	}
	
	@Override
	public CallbackCompletion<Void> callReturned (final ComponentController component, final ComponentCallReply reply) {
		Preconditions.checkState (this.component == component);
		if (this.pendingReference == reply.reference) {
			if (this.status == Status.WaitingResourceResolved) {
				// FIXME: this.pendingReference = null;
				String ipAddress;
				Integer port;
				try {
					Preconditions.checkArgument (reply.ok);
					Preconditions.checkArgument (reply.outputsOrError instanceof Map);
					final Map<?, ?> outputs = (Map<?, ?>) reply.outputsOrError;
					this.logger.trace ("Resource search returned " + outputs);
					ipAddress = (String) outputs.get ("ip");
					Preconditions.checkArgument (ipAddress != null);
					port = (Integer) outputs.get ("port");
					Preconditions.checkArgument (port != null);
				} catch (final IllegalArgumentException exception) {
					this.terminate ();
					this.exceptions.traceDeferredException (exception, "failed resolving Riak broker endpoint: `%s`; terminating!", reply.outputsOrError);
					throw new IllegalStateException (exception);
				}
				this.logger.trace ("Resolved Riak on " + ipAddress + ":" + port);
				this.configureDriver (ipAddress, port.toString ());
				if (this.selfGroup != null) {
					this.pendingReference = ComponentCallReference.create ();
					this.status = Status.Unregistered;
					this.component.register (this.selfGroup, this.pendingReference);
				}
			} else {
				throw new IllegalStateException ();
			}
		} else {
			throw new IllegalStateException ();
		}
		return null;
	}
	
	@Override
	public CallbackCompletion<Void> initialized (final ComponentController component) {
		Preconditions.checkState (this.component == null);
		Preconditions.checkState (this.status == Status.Created);
		this.component = component;
		final ComponentCallReference callReference = ComponentCallReference.create ();
		String operation;
		if (this.driverName.equalsIgnoreCase (KeyValueDriverFactory.DriverType.RIAKPB.toString ())) {
			operation = ConfigProperties.KVDriverComponentCallbacks_2;
		} else {
			operation = ConfigProperties.KVDriverComponentCallbacks_6;
		}
		this.pendingReference = callReference;
		this.status = Status.WaitingResourceResolved;
		this.component.call (this.resourceGroup, ComponentCallRequest.create (operation, null, callReference));
		this.logger.trace ("Key Value driver callback initialized.");
		return null;
	}
	
	@Override
	public CallbackCompletion<Void> registerReturned (final ComponentController component, final ComponentCallReference reference, final boolean success) {
		Preconditions.checkState (this.component == component);
		if (this.pendingReference == reference) {
			if (!success) {
				final Exception e = new Exception ("failed registering to group; terminating!");
				this.exceptions.traceDeferredException (e);
				this.component.terminate ();
				throw (new IllegalStateException (e));
			}
			this.logger.info ("Key Value Store driver callback registered to group " + this.selfGroup);
			this.status = Status.Registered;
			// NOTE: create stub and interop channel
			final ZeroMqChannel driverChannel = this.createDriverChannel (ConfigProperties.KVDriverComponentCallbacks_4, ConfigProperties.KVDriverComponentCallbacks_3, KeyValueSession.DRIVER);
			this.stub = KeyValueStub.create (this.getDriverConfiguration (), this.threading, driverChannel);
		} else {
			throw new IllegalStateException ();
		}
		return null;
	}
	
	private void configureDriver (final String brokerIp, final String port) {
		this.getDriverConfiguration ().addParameter (ConfigurationIdentifier.resolveRelative (ConfigProperties.KVStoreDriver_0), brokerIp);
		this.getDriverConfiguration ().addParameter (ConfigurationIdentifier.resolveRelative (ConfigProperties.KVStoreDriver_1), port);
	}
	
	private String driverName;
}
