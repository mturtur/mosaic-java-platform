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

package eu.mosaic_cloud.connectors.tests;


import java.util.UUID;

import eu.mosaic_cloud.connectors.kvstore.BaseKvStoreConnector;
import eu.mosaic_cloud.platform.core.configuration.IConfiguration;
import eu.mosaic_cloud.platform.core.utils.EncodingMetadata;
import eu.mosaic_cloud.platform.core.utils.MessageEnvelope;

import org.junit.Assert;
import org.junit.Test;


public abstract class BaseKvStoreConnectorTest<TConnector extends BaseKvStoreConnector<String, ?>>
		extends BaseConnectorTest<TConnector, BaseKvStoreConnectorTest.Scenario>
{
	@Override
	@Test
	public void test ()
	{
		this.testConnector ();
		this.testSet ();
		this.testGet ();
		this.testList ();
		this.testDelete ();
	}
	
	protected void testDelete ()
	{
		final String k1 = this.scenario.keyPrefix + "_key_fantastic";
		final String k2 = this.scenario.keyPrefix + "_key_famous";
		// NOTE: In past this would have returned `true`.
		Assert.assertNull (this.awaitOutcome (this.connector.delete (k1)));
		// NOTE: In past this would have returned `false`.
		Assert.assertNull (this.awaitOutcome (this.connector.delete (k2)));
		Assert.assertNull (this.awaitFailure (this.connector.get (k1)));
		Assert.assertNull (this.awaitFailure (this.connector.get (k2)));
	}
	
	protected void testGet ()
	{
		final String k1 = this.scenario.keyPrefix + "_key_fantastic";
		Assert.assertEquals ("fantastic", this.awaitOutcome (this.connector.get (k1)));
	}
	
	protected void testList ()
	{
		Assert.assertNotNull (this.awaitOutcome (this.connector.list ()));
	}
	
	protected void testSet ()
	{
		final String k1 = this.scenario.keyPrefix + "_key_fantastic";
		final String k2 = this.scenario.keyPrefix + "_key_famous";
		final EncodingMetadata encoding1 = new EncodingMetadata ("text/plain", "identity");
		final MessageEnvelope extra1 = new MessageEnvelope ();
		extra1.setEncodingMetadata (encoding1);
		final EncodingMetadata encoding2 = new EncodingMetadata ("text/plain", "identity");
		final MessageEnvelope extra2 = new MessageEnvelope ();
		extra2.setEncodingMetadata (encoding2);
		Assert.assertNull (this.awaitOutcome (this.connector.set (k1, "fantastic", extra1)));
		Assert.assertNull (this.awaitOutcome (this.connector.set (k2, "famous", extra2)));
	}
	
	public static class Scenario
			extends BaseScenario
	{
		public <C extends BaseKvStoreConnector<String, ?>> Scenario (final Class<? extends BaseKvStoreConnectorTest<C>> owner, final IConfiguration configuration)
		{
			super (owner, configuration);
		}
		
		public String keyPrefix = UUID.randomUUID ().toString ();
	}
}
