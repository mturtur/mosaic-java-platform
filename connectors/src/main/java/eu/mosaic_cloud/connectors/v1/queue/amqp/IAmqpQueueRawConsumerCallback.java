/*
 * #%L
 * mosaic-connectors
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

package eu.mosaic_cloud.connectors.v1.queue.amqp;


import eu.mosaic_cloud.platform.interop.common.amqp.AmqpInboundMessage;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;
import eu.mosaic_cloud.tools.callbacks.core.Callbacks;


/**
 * Interface for handlers (callbacks) to be called when a queue consumer
 * receives a message. Methods defined in this interface are called by the
 * connector when one of the consume messages is received from the driver.
 * 
 * @author Georgiana Macariu
 * 
 */
public interface IAmqpQueueRawConsumerCallback
		extends
			Callbacks
{
	/**
	 * Handles the Cancel OK message.
	 * 
	 * @param consumerTag
	 *            the consumer identifier
	 */
	CallbackCompletion<Void> handleCancelOk (String consumerTag);
	
	/**
	 * Handles the Consume OK message.
	 * 
	 * @param consumerTag
	 *            the consumer identifier
	 */
	CallbackCompletion<Void> handleConsumeOk (String consumerTag);
	
	/**
	 * Handles a delivered message.
	 * 
	 * @param message
	 *            the message and all its properties
	 */
	CallbackCompletion<Void> handleDelivery (AmqpInboundMessage message);
	
	/**
	 * Handles the shutdown signals.
	 * 
	 * @param consumerTag
	 *            the consumer identifier
	 * @param signalMessage
	 *            the signal message
	 */
	CallbackCompletion<Void> handleShutdownSignal (String consumerTag, String message);
}