/*
 * #%L
 * mosaic-cloudlets
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

package eu.mosaic_cloud.cloudlets.connectors.httpg;


import eu.mosaic_cloud.cloudlets.connectors.core.BaseConnector;
import eu.mosaic_cloud.cloudlets.core.GenericCallbackCompletionArguments;
import eu.mosaic_cloud.cloudlets.core.ICloudletController;
import eu.mosaic_cloud.connectors.httpg.HttpgRequestMessage;
import eu.mosaic_cloud.connectors.httpg.HttpgResponseMessage;
import eu.mosaic_cloud.platform.core.configuration.IConfiguration;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletionObserver;


public class HttpgQueueConnector<TContext, TRequestBody, TResponseBody, TExtra>
		extends BaseConnector<eu.mosaic_cloud.connectors.httpg.IHttpgQueueConnector<TRequestBody, TResponseBody>, IHttpgQueueConnectorCallback<TContext, TRequestBody, TResponseBody, TExtra>, TContext>
		implements
			IHttpgQueueConnector<TRequestBody, TResponseBody, TExtra>
{
	@SuppressWarnings ("synthetic-access")
	public HttpgQueueConnector (final ICloudletController<?> cloudlet, final eu.mosaic_cloud.connectors.httpg.IHttpgQueueConnector<TRequestBody, TResponseBody> connector, final IConfiguration configuration, final IHttpgQueueConnectorCallback<TContext, TRequestBody, TResponseBody, TExtra> callback, final TContext context, final Callback<TRequestBody, TResponseBody> backingCallback)
	{
		super (cloudlet, connector, configuration, callback, context);
		backingCallback.connector = this;
	}
	
	@Override
	public CallbackCompletion<Void> respond (final HttpgResponseMessage<TResponseBody> response)
	{
		return this.respond (response, null);
	}
	
	@Override
	public CallbackCompletion<Void> respond (final HttpgResponseMessage<TResponseBody> response, final TExtra extra)
	{
		final CallbackCompletion<Void> completion = this.connector.respond (response);
		if (this.callback != null) {
			completion.observe (new CallbackCompletionObserver () {
				@SuppressWarnings ("synthetic-access")
				@Override
				public CallbackCompletion<Void> completed (final CallbackCompletion<?> completion_)
				{
					assert (completion_ == completion);
					CallbackCompletion<Void> result;
					if (completion.getException () == null) {
						result = HttpgQueueConnector.this.callback.respondSucceeded (HttpgQueueConnector.this.context, new GenericCallbackCompletionArguments<TExtra> (HttpgQueueConnector.this.cloudlet, extra));
					} else {
						result = HttpgQueueConnector.this.callback.respondFailed (HttpgQueueConnector.this.context, new GenericCallbackCompletionArguments<TExtra> (HttpgQueueConnector.this.cloudlet, completion.getException ()));
					}
					return result;
				}
			});
		}
		return completion;
	}
	
	protected CallbackCompletion<Void> requested (final HttpgRequestMessage<TRequestBody> request)
	{
		CallbackCompletion<Void> result;
		if (this.callback == null) {
			result = CallbackCompletion.createFailure (new IllegalStateException ());
		} else {
			result = this.callback.requested (this.context, new HttpgQueueRequestedCallbackArguments<TRequestBody> (this.cloudlet, request));
		}
		return result;
	}
	
	public static final class Callback<TRequestBody, TResponseBody>
			implements
				eu.mosaic_cloud.connectors.httpg.IHttpgQueueCallback<TRequestBody, TResponseBody>
	{
		@Override
		public CallbackCompletion<Void> requested (final HttpgRequestMessage<TRequestBody> request)
		{
			return this.connector.requested (request);
		}
		
		private HttpgQueueConnector<?, TRequestBody, TResponseBody, ?> connector = null;
	}
}