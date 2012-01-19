/*
 * #%L
 * mosaic-examples-realtime-feeds-indexer
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
package eu.mosaic_cloud.examples.realtime_feeds.indexer;

import java.util.HashMap;
import java.util.Map;

import eu.mosaic_cloud.cloudlets.core.CallbackArguments;
import eu.mosaic_cloud.cloudlets.resources.kvstore.DefaultKeyValueAccessorCallback;
import eu.mosaic_cloud.cloudlets.resources.kvstore.KeyValueCallbackArguments;
import eu.mosaic_cloud.examples.realtime_feeds.indexer.IndexerCloudlet.IndexerCloudletContext;
import eu.mosaic_cloud.platform.core.log.MosaicLogger;



public class ItemsKVCallback extends
		DefaultKeyValueAccessorCallback<IndexerCloudletContext> {

	private static final String BUCKET_NAME = "feed-items";

	@Override
	public void destroySucceeded(IndexerCloudletContext context,
			CallbackArguments<IndexerCloudletContext> arguments) {
		context.itemsStore = null;
	}

	@Override
	public void setFailed(IndexerCloudletContext context,
			KeyValueCallbackArguments<IndexerCloudletContext> arguments) {
		handleError(arguments);
	}

	private void handleError(
			KeyValueCallbackArguments<IndexerCloudletContext> arguments) {
		String key = arguments.getKey();
		MosaicLogger.getLogger().warn(
				"failed fetch (" + ItemsKVCallback.BUCKET_NAME + "," + key
						+ ")");
		Map<String, String> errorMssg = new HashMap<String, String>(4);
		errorMssg.put("reason", "unexpected key-value store error");
		errorMssg.put("message", arguments.getValue().toString());
		errorMssg.put("bucket", ItemsKVCallback.BUCKET_NAME);
		errorMssg.put("key", key);
		IndexWorkflow.onIndexError(errorMssg, arguments.getExtra());
	}

}
