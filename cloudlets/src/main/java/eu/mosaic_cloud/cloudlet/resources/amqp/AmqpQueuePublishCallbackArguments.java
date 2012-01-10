/*
 * #%L
 * mosaic-cloudlet
 * %%
 * Copyright (C) 2010 - 2011 mOSAIC Project
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
package eu.mosaic_cloud.cloudlet.resources.amqp;

import eu.mosaic_cloud.cloudlet.core.CallbackArguments;
import eu.mosaic_cloud.cloudlet.core.ICloudletController;

/**
 * The arguments of the cloudlet callback methods for the publish request.
 * 
 * @author Georgiana Macariu
 * 
 * @param <S>
 *            the state of the cloudlet
 * @param <D>
 *            the type of the published data
 */
public class AmqpQueuePublishCallbackArguments<S, D> extends
		CallbackArguments<S> {
	private AmqpQueuePublishMessage<D> message;

	/**
	 * Creates a new callback argument.
	 * 
	 * @param cloudlet
	 *            the cloudlet
	 * @param message
	 *            information about the publish request
	 */
	public AmqpQueuePublishCallbackArguments(ICloudletController<S> cloudlet,
			AmqpQueuePublishMessage<D> message) {
		super(cloudlet);
		this.message = message;
	}

	/**
	 * Returns information about the publish request.
	 * 
	 * @return information about the publish request
	 */
	public AmqpQueuePublishMessage<D> getMessage() {
		return this.message;
	}

}
