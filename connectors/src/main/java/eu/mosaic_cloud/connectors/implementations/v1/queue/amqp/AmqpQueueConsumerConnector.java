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

package eu.mosaic_cloud.connectors.implementations.v1.queue.amqp;


import eu.mosaic_cloud.connectors.implementations.v1.core.ConnectorConfiguration;
import eu.mosaic_cloud.connectors.v1.queue.amqp.IAmqpMessageToken;
import eu.mosaic_cloud.connectors.v1.queue.amqp.IAmqpQueueConsumerCallback;
import eu.mosaic_cloud.connectors.v1.queue.amqp.IAmqpQueueConsumerConnector;
import eu.mosaic_cloud.platform.v1.core.serialization.DataEncoder;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;


public class AmqpQueueConsumerConnector<TMessage>
		extends AmqpQueueConnector<AmqpQueueConsumerConnectorProxy<TMessage>>
		implements
			IAmqpQueueConsumerConnector<TMessage>
{
	protected AmqpQueueConsumerConnector (final AmqpQueueConsumerConnectorProxy<TMessage> proxy)
	{
		super (proxy);
	}
	
	@Override
	public CallbackCompletion<Void> acknowledge (final IAmqpMessageToken token)
	{
		return this.proxy.acknowledge (token);
	}
	
	public static <TMessage> AmqpQueueConsumerConnector<TMessage> create (final ConnectorConfiguration configuration, final Class<TMessage> messageClass, final DataEncoder<TMessage> messageEncoder, final IAmqpQueueConsumerCallback<TMessage> callback)
	{
		final AmqpQueueConsumerConnectorProxy<TMessage> proxy = AmqpQueueConsumerConnectorProxy.create (configuration, messageClass, messageEncoder, callback);
		return new AmqpQueueConsumerConnector<TMessage> (proxy);
	}
}
