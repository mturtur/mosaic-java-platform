/*
 * #%L
 * mosaic-examples-simple-cloudlets
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

package eu.mosaic_cloud.examples.cloudlets.simple;

import eu.mosaic_cloud.cloudlets.connectors.kvstore.IKvStoreConnector;
import eu.mosaic_cloud.cloudlets.connectors.kvstore.IKvStoreConnectorFactory;
import eu.mosaic_cloud.cloudlets.connectors.queue.amqp.AmqpQueueConsumeCallbackArguments;
import eu.mosaic_cloud.cloudlets.connectors.queue.amqp.IAmqpQueueConsumerConnector;
import eu.mosaic_cloud.cloudlets.connectors.queue.amqp.IAmqpQueueConsumerConnectorFactory;
import eu.mosaic_cloud.cloudlets.connectors.queue.amqp.IAmqpQueuePublisherConnector;
import eu.mosaic_cloud.cloudlets.connectors.queue.amqp.IAmqpQueuePublisherConnectorFactory;
import eu.mosaic_cloud.cloudlets.core.CallbackArguments;
import eu.mosaic_cloud.cloudlets.core.CloudletCallbackArguments;
import eu.mosaic_cloud.cloudlets.core.CloudletCallbackCompletionArguments;
import eu.mosaic_cloud.cloudlets.core.ICallback;
import eu.mosaic_cloud.cloudlets.core.ICloudletController;
import eu.mosaic_cloud.cloudlets.tools.DefaultAmqpPublisherConnectorCallback;
import eu.mosaic_cloud.cloudlets.tools.DefaultAmqpQueueConsumerConnectorCallback;
import eu.mosaic_cloud.cloudlets.tools.DefaultCloudletCallback;
import eu.mosaic_cloud.cloudlets.tools.DefaultKvStoreConnectorCallback;
import eu.mosaic_cloud.platform.core.configuration.ConfigUtils;
import eu.mosaic_cloud.platform.core.configuration.ConfigurationIdentifier;
import eu.mosaic_cloud.platform.core.configuration.IConfiguration;
import eu.mosaic_cloud.platform.core.utils.PojoDataEncoder;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;

public class LoggingCloudlet {

    public static final class AmqpConsumerCallback
            extends
            DefaultAmqpQueueConsumerConnectorCallback<LoggingCloudletContext, LoggingData, Void> {

        @Override
        public CallbackCompletion<Void> consume(LoggingCloudletContext context,
                AmqpQueueConsumeCallbackArguments<LoggingData, Void> arguments) {
            final LoggingData data = arguments.getMessage();
            this.logger
                    .info("LoggingCloudlet received logging message for user "
                            + data.user);
            final CallbackCompletion<String> result = context.kvStore.get(
                    data.user, null);
            String passOb;
            String token = null;
            result.await();
            passOb = result.getOutcome();
            final String pass = passOb;
            if (pass.equals(data.password)) {
                token = ConfigUtils.resolveParameter(arguments.getCloudlet()
                        .getConfiguration(), "test.token", String.class,
                        "token");
                context.kvStore.set(data.user, token, null);
            }
            final AuthenticationToken aToken = new AuthenticationToken(token);
            context.publisher.publish(aToken, null);
            context.consumer.acknowledge(arguments.getDelivery());
            context.cloudlet.destroy ();
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> destroySucceeded(
                LoggingCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("LoggingCloudlet consumer was destroyed successfully.");
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> initializeSucceeded(
                LoggingCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("LoggingCloudlet consumer initialized successfully.");
            return ICallback.SUCCESS;
        }
    }

    public static final class AmqpPublisherCallback
            extends
            DefaultAmqpPublisherConnectorCallback<LoggingCloudletContext, AuthenticationToken, Void> {

        @Override
        public CallbackCompletion<Void> destroySucceeded(
                LoggingCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("LoggingCloudlet publisher was destroyed successfully.");
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> initializeSucceeded(
                LoggingCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("LoggingCloudlet publisher initialized successfully.");
            return ICallback.SUCCESS;
        }
    }

    public static final class KeyValueCallback
            extends
            DefaultKvStoreConnectorCallback<LoggingCloudletContext, String, Void> {

        @Override
        public CallbackCompletion<Void> destroySucceeded(
                LoggingCloudletContext context, CallbackArguments arguments) {
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> initializeSucceeded(
                LoggingCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("LoggingCloudlet - KeyValue accessor initialized successfully");
            final String user = ConfigUtils.resolveParameter(arguments
                    .getCloudlet().getConfiguration(), "test.user",
                    String.class, "error");
            final String pass = ConfigUtils.resolveParameter(arguments
                    .getCloudlet().getConfiguration(), "test.password",
                    String.class, "");
            context.kvStore.set(user, pass, null);
            return ICallback.SUCCESS;
        }
    }

    public static final class LifeCycleHandler extends
            DefaultCloudletCallback<LoggingCloudletContext> {

        @Override
        public CallbackCompletion<Void> destroy(LoggingCloudletContext context,
                CloudletCallbackArguments<LoggingCloudletContext> arguments) {
            this.logger.info("LoggingCloudlet is being destroyed.");
            return CallbackCompletion.createAndChained (
            		context.kvStore.destroy (),
            		context.consumer.destroy (),
            		context.publisher.destroy ());
        }

        @Override
        public CallbackCompletion<Void> destroySucceeded(
                LoggingCloudletContext context,
                CloudletCallbackCompletionArguments<LoggingCloudletContext> arguments) {
            this.logger.info("LoggingCloudlet was destroyed successfully.");
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> initialize(
                LoggingCloudletContext context,
                CloudletCallbackArguments<LoggingCloudletContext> arguments) {
            this.logger.info("LoggingCloudlet is being initialized.");
            context.cloudlet = arguments.getCloudlet();
            final IConfiguration configuration = context.cloudlet.getConfiguration();
            final IConfiguration kvConfiguration = configuration
                    .spliceConfiguration(ConfigurationIdentifier
                            .resolveAbsolute("kvstore"));
            context.kvStore = context.cloudlet.getConnectorFactory(
                    IKvStoreConnectorFactory.class).create(kvConfiguration,
                    String.class, new PojoDataEncoder<String>(String.class),
                    new KeyValueCallback(), context);
            final IConfiguration queueConfiguration = configuration
                    .spliceConfiguration(ConfigurationIdentifier
                            .resolveAbsolute("queue"));
            context.consumer = context.cloudlet.getConnectorFactory(
                    IAmqpQueueConsumerConnectorFactory.class).create(
                    queueConfiguration, LoggingData.class,
                    new PojoDataEncoder<LoggingData>(LoggingData.class),
                    new AmqpConsumerCallback(), context);
            context.publisher = context.cloudlet.getConnectorFactory(
                    IAmqpQueuePublisherConnectorFactory.class).create(
                    queueConfiguration,
                    AuthenticationToken.class,
                    new PojoDataEncoder<AuthenticationToken>(
                            AuthenticationToken.class),
                    new AmqpPublisherCallback(), context);
            return CallbackCompletion.createAndChained (
            		context.kvStore.initialize (),
            		context.consumer.initialize (),
            		context.publisher.initialize ());
        }

        @Override
        public CallbackCompletion<Void> initializeSucceeded(
                LoggingCloudletContext context,
                CloudletCallbackCompletionArguments<LoggingCloudletContext> arguments) {
            this.logger.info("LoggingCloudlet initialized successfully.");
            return ICallback.SUCCESS;
        }
    }

    public static final class LoggingCloudletContext {

    	ICloudletController<LoggingCloudletContext> cloudlet;
        IAmqpQueueConsumerConnector<LoggingData, Void> consumer;
        IAmqpQueuePublisherConnector<AuthenticationToken, Void> publisher;
        IKvStoreConnector<String, Void> kvStore;
    }
}
