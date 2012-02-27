/*
 * #%L
 * mosaic-drivers
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

package eu.mosaic_cloud.drivers;

import eu.mosaic_cloud.components.core.ComponentCallReference;
import eu.mosaic_cloud.components.core.ComponentCallbacks;
import eu.mosaic_cloud.components.core.ComponentCastRequest;
import eu.mosaic_cloud.components.core.ComponentEnvironment;
import eu.mosaic_cloud.components.core.ComponentController;
import eu.mosaic_cloud.components.core.ComponentIdentifier;
import eu.mosaic_cloud.drivers.interop.AbstractDriverStub;
import eu.mosaic_cloud.interoperability.core.SessionSpecification;
import eu.mosaic_cloud.interoperability.implementations.zeromq.ZeroMqChannel;
import eu.mosaic_cloud.platform.core.configuration.ConfigUtils;
import eu.mosaic_cloud.platform.core.configuration.IConfiguration;
import eu.mosaic_cloud.platform.core.exceptions.ExceptionTracer;
import eu.mosaic_cloud.platform.core.log.MosaicLogger;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;
import eu.mosaic_cloud.tools.callbacks.core.CallbackHandler;
import eu.mosaic_cloud.tools.callbacks.core.CallbackIsolate;
import eu.mosaic_cloud.tools.callbacks.core.Callbacks;
import eu.mosaic_cloud.tools.exceptions.tools.AbortingExceptionTracer;
import eu.mosaic_cloud.tools.threading.core.ThreadingContext;

import com.google.common.base.Preconditions;

/**
 * This callback class enables a resource driver to be exposed as a component.
 * Upon initialization it will look for the resource and will create a driver
 * object for the resource.
 * 
 * @author Georgiana Macariu
 * 
 */
public abstract class AbstractDriverComponentCallbacks implements ComponentCallbacks,
        CallbackHandler {

    protected static enum Status {
        Created, Registered, Terminated, Unregistered, WaitingResourceResolved;
    }

    protected Status status;

    protected ComponentController component;

    protected ComponentCallReference pendingReference;

    protected AbstractDriverStub stub;

    protected ComponentIdentifier resourceGroup;

    protected ComponentIdentifier selfGroup;

    protected IConfiguration driverConfiguration;

    protected ThreadingContext threading;

    protected MosaicLogger logger;

    protected AbstractDriverComponentCallbacks(ComponentEnvironment context) {
        this.threading = context.threading;
        this.logger = MosaicLogger.createLogger(this);
    }

    @Override
    public CallbackCompletion<Void> casted(ComponentController component,
            ComponentCastRequest request) {
        Preconditions.checkState(this.component == component);
        Preconditions.checkState((this.status != Status.Terminated)
                && (this.status != Status.Unregistered));
        throw new UnsupportedOperationException();
    }

    protected ZeroMqChannel createDriverChannel(String channelIdentifierProp,
            String channelEndpointProp, SessionSpecification role) {
        // NOTE: create stub and interop channel
        Preconditions.checkNotNull(this.driverConfiguration);
        final ZeroMqChannel driverChannel = ZeroMqChannel.create(ConfigUtils.resolveParameter(
                this.driverConfiguration, channelIdentifierProp, String.class, ""), this.threading,
                AbortingExceptionTracer.defaultInstance);
        driverChannel.register(role);
        driverChannel.accept(ConfigUtils.resolveParameter(this.driverConfiguration,
                channelEndpointProp, String.class, ""));
        return driverChannel;
    }

    @Override
    public CallbackCompletion<Void> failed(ComponentController component, Throwable exception) {
        Preconditions.checkState(this.component == component);
        Preconditions.checkState((this.status != Status.Terminated)
                && (this.status != Status.Unregistered));
        if (this.stub != null) {
            this.stub.destroy();
        }
        this.component = null; // NOPMD by georgiana on 10/10/11 1:56 PM
        this.status = Status.Terminated;
        ExceptionTracer.traceIgnored(exception);
        return null;
    }

    @Override
    public final void failedCallbacks(Callbacks trigger, Throwable exception) {
        this.failed(this.component, exception);
    }

    protected IConfiguration getDriverConfiguration() {
        return this.driverConfiguration;
    }

    @Override
    public final void registeredCallbacks(Callbacks trigger, CallbackIsolate isolate) {
    }

    protected void setDriverConfiguration(IConfiguration driverConfiguration) {
        this.driverConfiguration = driverConfiguration;
    }

    public void terminate() {
        Preconditions.checkState(this.component != null);
        this.component.terminate();
    }

    @Override
    public CallbackCompletion<Void> terminated(ComponentController component) {
        Preconditions.checkState(this.component == component);
        Preconditions.checkState((this.status != Status.Terminated)
                && (this.status != Status.Unregistered));
        if (this.stub != null) {
            this.stub.destroy();
            this.logger.trace("Driver callbacks terminated.");
        }
        this.component = null; // NOPMD by georgiana on 10/10/11 1:56 PM
        this.status = Status.Terminated;
        return null;
    }

    @Override
    public final void unregisteredCallbacks(Callbacks trigger) {
    }
}
