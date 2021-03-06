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

package eu.mosaic_cloud.examples.cloudlets.v2.simple;


import eu.mosaic_cloud.platform.implementation.v2.serialization.PlainTextDataEncoder;
import eu.mosaic_cloud.platform.tools.v2.cloudlets.callbacks.DefaultCallback;
import eu.mosaic_cloud.platform.tools.v2.cloudlets.callbacks.DefaultCloudlet;
import eu.mosaic_cloud.platform.tools.v2.cloudlets.callbacks.DefaultCloudletCallback;
import eu.mosaic_cloud.platform.tools.v2.cloudlets.callbacks.DefaultCloudletContext;
import eu.mosaic_cloud.platform.tools.v2.cloudlets.callbacks.DefaultQueueConsumerConnectorCallback;
import eu.mosaic_cloud.platform.v2.cloudlets.connectors.queue.QueueConsumerConnector;
import eu.mosaic_cloud.platform.v2.cloudlets.core.CloudletController;
import eu.mosaic_cloud.platform.v2.connectors.queue.QueueDeliveryToken;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;
import eu.mosaic_cloud.tools.threading.tools.Threading;


public class ConsumerCloudlet
			extends DefaultCloudlet
{
	public static CallbackCompletion<Void> maybeContinue (final Context context) {
		// FIXME: DON'T DO THIS IN YOUR CODE... This is for throttling...
		Threading.sleep (context.delay);
		//----		
		context.count += 1;
		if (context.count >= context.limit)
			context.cloudlet.destroy ();
		return (DefaultCallback.Succeeded);
	}
	
	public static class CloudletCallback
				extends DefaultCloudletCallback<Context>
	{
		@Override
		protected CallbackCompletion<Void> destroy (final Context context) {
			context.logger.info ("ConsumerCloudlet destroying...");
			return (context.connector.destroy ());
		}
		
		@Override
		protected CallbackCompletion<Void> destroySucceeded (final Context context) {
			context.logger.info ("ConsumerCloudlet destroyed successfully.");
			return (DefaultCallback.Succeeded);
		}
		
		@Override
		protected CallbackCompletion<Void> initialize (final Context context) {
			context.logger.info ("ConsumerCloudlet initializing...");
			context.connector = context.createQueueConsumerConnector ("consumer", String.class, PlainTextDataEncoder.DEFAULT_INSTANCE, ConnectorCallback.class);
			return (context.connector.initialize ());
		}
		
		@Override
		protected CallbackCompletion<Void> initializeSucceeded (final Context context) {
			context.logger.info ("ConsumerCloudlet initialized successfully.");
			return (DefaultCallback.Succeeded);
		}
	}
	
	public static class ConnectorCallback
				extends DefaultQueueConsumerConnectorCallback<Context, String, Void>
	{
		@Override
		protected CallbackCompletion<Void> acknowledgeSucceeded (final Context context, final Void extra) {
			return (ConsumerCloudlet.maybeContinue (context));
		}
		
		@Override
		protected CallbackCompletion<Void> consume (final Context context, final String message, final QueueDeliveryToken token) {
			context.logger.info ("ConsumerCloudlet received message `{}`.", message);
			context.connector.acknowledge (token, null);
			return (DefaultCallback.Succeeded);
		}
		
		@Override
		protected CallbackCompletion<Void> destroySucceeded (final Context context) {
			context.logger.info ("ConsumerCloudlet connector destroyed successfully.");
			return (DefaultCallback.Succeeded);
		}
		
		@Override
		protected CallbackCompletion<Void> initializeSucceeded (final Context context) {
			context.logger.info ("ConsumerCloudlet connector initialized successfully.");
			return (DefaultCallback.Succeeded);
		}
	}
	
	public static class Context
				extends DefaultCloudletContext<Context>
	{
		public Context (final CloudletController<Context> cloudlet) {
			super (cloudlet);
		}
		
		QueueConsumerConnector<String, Void> connector;
		int count = 0;
		final int delay = 50;
		final int limit = 10;
	}
}
