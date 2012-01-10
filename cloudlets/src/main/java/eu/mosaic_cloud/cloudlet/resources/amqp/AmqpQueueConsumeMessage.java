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

import eu.mosaic_cloud.driver.queue.amqp.AmqpInboundMessage;

/**
 * An object of this class embeds the essential information about a consume
 * request.
 * 
 * @author Georgiana Macariu
 * 
 * @param <D>
 *            the type of the data in the consumed message
 */
public class AmqpQueueConsumeMessage<D extends Object> {
	private final AmqpQueueConsumer<? extends Object, D> consumer;
	private final AmqpInboundMessage message;
	private final D data;

	public AmqpQueueConsumeMessage(
			AmqpQueueConsumer<? extends Object, D> consumer,
			AmqpInboundMessage message, D data) {
		super();
		this.consumer = consumer;
		this.message = message;
		this.data = data;
	}

	/**
	 * Acknowledges the message.
	 */
	public void acknowledge() {
		this.consumer.acknowledge(this);
	}

	/**
	 * Returns the data in the consumed message.
	 * 
	 * @return the data in the consumed message
	 */
	public D getData() {
		return this.data;
	}

	AmqpInboundMessage getMessage() {
		return this.message;
	}

	/**
	 * Returns the consumer object.
	 * 
	 * @return the consumer object
	 */
	public AmqpQueueConsumer<? extends Object, D> getConsumer() {
		return this.consumer;
	}

}
