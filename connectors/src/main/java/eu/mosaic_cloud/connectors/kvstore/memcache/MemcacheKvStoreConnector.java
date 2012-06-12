/*
 * #%L
 * mosaic-connectors
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

package eu.mosaic_cloud.connectors.kvstore.memcache;


import java.util.List;
import java.util.Map;

import eu.mosaic_cloud.connectors.kvstore.BaseKvStoreConnector;
import eu.mosaic_cloud.connectors.tools.ConnectorConfiguration;
import eu.mosaic_cloud.platform.core.utils.DataEncoder;
import eu.mosaic_cloud.platform.core.utils.MessageEnvelope;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;


/**
 * Connector for key-value distributed storage systems implementing the
 * memcached protocol.
 * 
 * @author Georgiana Macariu
 * @param <TValue>
 *            type of stored data
 */
public class MemcacheKvStoreConnector<TValue extends Object, TExtra extends MessageEnvelope>
		extends BaseKvStoreConnector<TValue, TExtra, MemcacheKvStoreConnectorProxy<TValue, TExtra>>
		implements
			IMemcacheKvStoreConnector<TValue, TExtra>
{
	protected MemcacheKvStoreConnector (final MemcacheKvStoreConnectorProxy<TValue, TExtra> proxy)
	{
		super (proxy);
	}
	
	@Override
	public CallbackCompletion<Void> add (final String key, final int exp, final TValue data, final TExtra extra)
	{
		return this.proxy.add (key, exp, data, extra);
	}
	
	@Override
	public CallbackCompletion<Void> append (final String key, final TValue data, final TExtra extra)
	{
		return this.proxy.append (key, data, extra);
	}
	
	@Override
	public CallbackCompletion<Void> cas (final String key, final TValue data, final TExtra extra)
	{
		return this.proxy.cas (key, data, extra);
	}
	
	@Override
	public CallbackCompletion<Map<String, TValue>> getBulk (final List<String> keys, final TExtra extra)
	{
		return this.proxy.getBulk (keys, extra);
	}
	
	@Override
	public CallbackCompletion<Void> prepend (final String key, final TValue data, final TExtra extra)
	{
		return this.proxy.prepend (key, data, extra);
	}
	
	@Override
	public CallbackCompletion<Void> replace (final String key, final int exp, final TValue data, final TExtra extra)
	{
		return this.proxy.replace (key, exp, data, extra);
	}
	
	@Override
	public CallbackCompletion<Void> set (final String key, final int exp, final TValue data, final TExtra extra)
	{
		return this.proxy.set (key, exp, data, extra);
	}
	
	/**
	 * Creates the connector.
	 * 
	 * @param configuration
	 *            the execution environment of a connector
	 * @param encoder
	 *            encoder used for serializing and deserializing data stored in
	 *            the key-value store
	 * @return the connector
	 * @throws Throwable
	 */
	public static <T extends Object, TExtra extends MessageEnvelope> MemcacheKvStoreConnector<T, TExtra> create (final ConnectorConfiguration configuration, final DataEncoder<T> encoder)
	{
		final MemcacheKvStoreConnectorProxy<T, TExtra> proxy = MemcacheKvStoreConnectorProxy.create (configuration, encoder);
		return new MemcacheKvStoreConnector<T, TExtra> (proxy);
	}
}
