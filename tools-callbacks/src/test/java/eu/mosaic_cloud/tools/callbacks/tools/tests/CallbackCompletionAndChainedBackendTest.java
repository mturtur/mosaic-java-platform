/*
 * #%L
 * mosaic-tools-callbacks
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

package eu.mosaic_cloud.tools.callbacks.tools.tests;


import java.util.concurrent.atomic.AtomicBoolean;

import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletionObserver;
import eu.mosaic_cloud.tools.callbacks.tools.CallbackCompletionDeferredFuture;
import eu.mosaic_cloud.tools.exceptions.tools.QueuedExceptions;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Preconditions;


public final class CallbackCompletionAndChainedBackendTest
{
	@Test
	public final void testFailure1 () {
		final Throwable marker = new Throwable ();
		final AtomicBoolean observed1 = new AtomicBoolean (false);
		final AtomicBoolean observed2 = new AtomicBoolean (false);
		final CallbackCompletionDeferredFuture<Void> future1 = CallbackCompletionDeferredFuture.create (Void.class);
		final CallbackCompletionDeferredFuture<Void> future2 = CallbackCompletionDeferredFuture.create (Void.class);
		final CallbackCompletion<Void> completion = CallbackCompletion.createChained (future1.completion, future2.completion);
		Assert.assertFalse (completion.await (0));
		Assert.assertFalse (completion.await (10));
		completion.observe (new Observer (completion, observed1));
		future1.trigger.triggerFailed (marker);
		Assert.assertFalse (completion.await (0));
		Assert.assertFalse (completion.await (10));
		future2.trigger.triggerSucceeded (null);
		Assert.assertTrue (completion.await (0));
		completion.observe (new Observer (completion, observed2));
		final Throwable failure = completion.getException ();
		Assert.assertNotNull (failure);
		Assert.assertTrue (failure instanceof QueuedExceptions);
		final Throwable caught = ((QueuedExceptions) failure).queue.poll ().caught;
		Assert.assertSame (marker, caught);
		try {
			completion.getOutcome ();
			throw (new AssertionError ());
		} catch (final Throwable exception) {
			Assert.assertTrue (exception instanceof IllegalStateException);
		}
		Assert.assertTrue (observed1.get ());
		Assert.assertTrue (observed2.get ());
	}
	
	@Test
	public final void testFailure2 () {
		final Throwable marker = new Throwable ();
		final AtomicBoolean observed1 = new AtomicBoolean (false);
		final AtomicBoolean observed2 = new AtomicBoolean (false);
		final CallbackCompletionDeferredFuture<Void> future1 = CallbackCompletionDeferredFuture.create (Void.class);
		final CallbackCompletionDeferredFuture<Void> future2 = CallbackCompletionDeferredFuture.create (Void.class);
		final CallbackCompletion<Void> completion = CallbackCompletion.createChained (future1.completion, future2.completion);
		Assert.assertFalse (completion.await (0));
		Assert.assertFalse (completion.await (10));
		completion.observe (new Observer (completion, observed1));
		future1.trigger.triggerSucceeded (null);
		Assert.assertFalse (completion.await (0));
		Assert.assertFalse (completion.await (10));
		future2.trigger.triggerFailed (marker);
		Assert.assertTrue (completion.await (0));
		completion.observe (new Observer (completion, observed2));
		final Throwable failure = completion.getException ();
		Assert.assertNotNull (failure);
		Assert.assertTrue (failure instanceof QueuedExceptions);
		final Throwable caught = ((QueuedExceptions) failure).queue.poll ().caught;
		Assert.assertSame (marker, caught);
		try {
			completion.getOutcome ();
			throw (new AssertionError ());
		} catch (final Throwable exception) {
			Assert.assertTrue (exception instanceof IllegalStateException);
		}
		Assert.assertTrue (observed1.get ());
		Assert.assertTrue (observed2.get ());
	}
	
	@Test
	public final void testSucceeded () {
		final AtomicBoolean observed1 = new AtomicBoolean (false);
		final AtomicBoolean observed2 = new AtomicBoolean (false);
		final CallbackCompletionDeferredFuture<Void> future1 = CallbackCompletionDeferredFuture.create (Void.class);
		final CallbackCompletionDeferredFuture<Void> future2 = CallbackCompletionDeferredFuture.create (Void.class);
		final CallbackCompletion<Void> completion = CallbackCompletion.createChained (future1.completion, future2.completion);
		Assert.assertFalse (completion.await (0));
		Assert.assertFalse (completion.await (10));
		completion.observe (new Observer (completion, observed1));
		future1.trigger.triggerSucceeded (null);
		Assert.assertFalse (completion.await (0));
		Assert.assertFalse (completion.await (10));
		future2.trigger.triggerSucceeded (null);
		Assert.assertTrue (completion.await (0));
		completion.observe (new Observer (completion, observed2));
		Assert.assertNull (completion.getException ());
		Assert.assertSame (null, completion.getOutcome ());
		Assert.assertTrue (observed1.get ());
		Assert.assertTrue (observed2.get ());
	}
	
	final class Observer
				extends Object
				implements
					CallbackCompletionObserver
	{
		Observer (final CallbackCompletion<?> completion, final AtomicBoolean triggered) {
			super ();
			this.completion = completion;
			this.obvserved = triggered;
		}
		
		@Override
		public CallbackCompletion<Void> completed (final CallbackCompletion<?> completion) {
			Preconditions.checkArgument (completion == this.completion);
			Preconditions.checkState (this.obvserved.compareAndSet (false, true));
			return (CallbackCompletion.createOutcome ());
		}
		
		final CallbackCompletion<?> completion;
		final AtomicBoolean obvserved;
	}
}
