/*
 * #%L
 * mosaic-cloudlets
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

package eu.mosaic_cloud.cloudlets.tools.v1.callbacks;


import eu.mosaic_cloud.cloudlets.v1.connectors.queue.amqp.AmqpQueueConnectorCallback;


/**
 * Default AMQP resource accessor callback.
 * 
 * @author Georgiana Macariu
 * @param <TContext>
 *            the type of the context of the cloudlet using this callback
 */
public class DefaultAmqpQueueConnectorCallback<TContext>
			extends DefaultQueueConnectorCallback<TContext>
			implements
				AmqpQueueConnectorCallback<TContext>
{}
