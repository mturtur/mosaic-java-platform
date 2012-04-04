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
import eu.mosaic_cloud.cloudlets.connectors.kvstore.KvStoreCallbackCompletionArguments;
import eu.mosaic_cloud.cloudlets.connectors.queue.amqp.AmqpQueueConsumeCallbackArguments;
import eu.mosaic_cloud.cloudlets.connectors.queue.amqp.IAmqpQueueConsumerConnector;
import eu.mosaic_cloud.cloudlets.connectors.queue.amqp.IAmqpQueueConsumerConnectorFactory;
import eu.mosaic_cloud.cloudlets.connectors.queue.amqp.IAmqpQueuePublisherConnector;
import eu.mosaic_cloud.cloudlets.connectors.queue.amqp.IAmqpQueuePublisherConnectorFactory;
import eu.mosaic_cloud.cloudlets.core.CallbackArguments;
import eu.mosaic_cloud.cloudlets.core.CloudletCallbackArguments;
import eu.mosaic_cloud.cloudlets.core.CloudletCallbackCompletionArguments;
import eu.mosaic_cloud.cloudlets.core.GenericCallbackCompletionArguments;
import eu.mosaic_cloud.cloudlets.core.ICallback;
import eu.mosaic_cloud.cloudlets.core.ICloudletController;
import eu.mosaic_cloud.cloudlets.tools.DefaultAmqpPublisherConnectorCallback;
import eu.mosaic_cloud.cloudlets.tools.DefaultAmqpQueueConsumerConnectorCallback;
import eu.mosaic_cloud.cloudlets.tools.DefaultCloudletCallback;
import eu.mosaic_cloud.cloudlets.tools.DefaultKvStoreConnectorCallback;
import eu.mosaic_cloud.platform.core.configuration.ConfigurationIdentifier;
import eu.mosaic_cloud.platform.core.configuration.IConfiguration;
import eu.mosaic_cloud.platform.core.utils.JsonDataEncoder;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;

public class PongCloudlet {

    public static final class AmqpConsumerCallback
            extends
            DefaultAmqpQueueConsumerConnectorCallback<PongCloudletContext, PingMessage, Void> {

        @Override
        public CallbackCompletion<Void> consume(PongCloudletContext context,
                AmqpQueueConsumeCallbackArguments<PingMessage, Void> arguments) {
            // NOTE: retrieve message data
            final PingMessage data = arguments.getMessage();
            this.logger.info("Pong Cloudlet received fetch request for key "
                    + data.getKey());
            // NOTE: get value from key value store
            context.kvStore.get(data.getKey(), null);
            context.consumer.acknowledge(arguments.getDelivery());
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> destroySucceeded(
                PongCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("Pong Cloudlet consumer was destroyed successfully.");
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> initializeSucceeded(
                PongCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("Pong Cloudlet consumer initialized successfully.");
            return ICallback.SUCCESS;
        }
    }

    public static final class AmqpPublisherCallback
            extends
            DefaultAmqpPublisherConnectorCallback<PongCloudletContext, PongMessage, Void> {

        @Override
        public CallbackCompletion<Void> destroySucceeded(
                PongCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("Pong Cloudlet publisher was destroyed successfully.");
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> initializeSucceeded(
                PongCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("Pong Cloudlet publisher initialized successfully.");
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> publishSucceeded(
                PongCloudletContext context,
                GenericCallbackCompletionArguments<Void> arguments) {
            context.cloudlet.destroy ();
            return ICallback.SUCCESS;
        }
    }

    public static final class KeyValueCallback
            extends
            DefaultKvStoreConnectorCallback<PongCloudletContext, PingPongData, Void> {

        @Override
        public CallbackCompletion<Void> destroySucceeded(
                PongCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("Pong Cloudlet - KeyValue accessor destroyed successfully");
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> getSucceeded(
                PongCloudletContext context,
                KvStoreCallbackCompletionArguments<PingPongData, Void> arguments) {
            this.logger.info("Pong Cloudlet - key value fetch data succeeded");
            // NOTE: send reply to Ping Cloudlet
            final PongMessage pong = new PongMessage(arguments.getKey(),
                    arguments.getValue());
            context.publisher.publish(pong, null);
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> initializeSucceeded(
                PongCloudletContext context, CallbackArguments arguments) {
            this.logger
                    .info("Pong Cloudlet - KeyValue accessor initialized successfully");
            return ICallback.SUCCESS;
        }
    }

    public static final class LifeCycleHandler extends
            DefaultCloudletCallback<PongCloudletContext> {

        @Override
        public CallbackCompletion<Void> destroy(PongCloudletContext context,
                CloudletCallbackArguments<PongCloudletContext> arguments) {
            this.logger.info("Pong Cloudlet is being destroyed.");
            return CallbackCompletion.createAndChained (
            		context.kvStore.destroy (),
            		context.consumer.destroy (),
            		context.publisher.destroy ());
        }

        @Override
        public CallbackCompletion<Void> destroySucceeded(
                PongCloudletContext context,
                CloudletCallbackCompletionArguments<PongCloudletContext> arguments) {
            this.logger.info("Pong Cloudlet was destroyed successfully.");
            return ICallback.SUCCESS;
        }

        @Override
        public CallbackCompletion<Void> initialize(PongCloudletContext context,
                CloudletCallbackArguments<PongCloudletContext> arguments) {
            this.logger.info("Pong Cloudlet is being initialized.");
            final ICloudletController<PongCloudletContext> cloudlet = arguments
                    .getCloudlet();
            final IConfiguration configuration = cloudlet.getConfiguration();
            final IConfiguration kvConfiguration = configuration
                    .spliceConfiguration(ConfigurationIdentifier
                            .resolveAbsolute("kvstore"));
            context.kvStore = cloudlet.getConnectorFactory(
                    IKvStoreConnectorFactory.class).create(kvConfiguration,
                    PingPongData.class,
                    new JsonDataEncoder<PingPongData>(PingPongData.class),
                    new KeyValueCallback(), context);
            final IConfiguration queueConfiguration = configuration
                    .spliceConfiguration(ConfigurationIdentifier
                            .resolveAbsolute("queue"));
            context.consumer = cloudlet.getConnectorFactory(
                    IAmqpQueueConsumerConnectorFactory.class).create(
                    queueConfiguration, PingMessage.class,
                    new JsonDataEncoder<PingMessage>(PingMessage.class),
                    new AmqpConsumerCallback(), context);
            context.publisher = cloudlet.getConnectorFactory(
                    IAmqpQueuePublisherConnectorFactory.class).create(
                    queueConfiguration, PongMessage.class,
                    new JsonDataEncoder<PongMessage>(PongMessage.class),
                    new AmqpPublisherCallback(), context);
            return CallbackCompletion.createAndChained (
            		context.kvStore.initialize (),
            		context.consumer.initialize (),
            		context.publisher.initialize ());
        }

        @Override
        public CallbackCompletion<Void> initializeSucceeded(
                PongCloudletContext context,
                CloudletCallbackCompletionArguments<PongCloudletContext> arguments) {
            this.logger.info("Pong Cloudlet initialized successfully.");
            return ICallback.SUCCESS;
        }
    }

    public static final class PongCloudletContext {

    	ICloudletController<PongCloudletContext> cloudlet;
        IAmqpQueueConsumerConnector<PingMessage, Void> consumer;
        IAmqpQueuePublisherConnector<PongMessage, Void> publisher;
        IKvStoreConnector<PingPongData, Void> kvStore;
    }
}
