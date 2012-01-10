/*
 * #%L
 * mosaic-core
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
package mosaic.core.ops;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import mosaic.core.exceptions.ExceptionTracer;
import mosaic.core.exceptions.ResultSetException;

/**
 * Implementation of an asynchronous operation using only an event driven
 * approach. It is also possible for the caller of the operation to block until
 * its result becomes available in a manner similar to the one defined by the
 * Future pattern. However, this feature should be used only when it absolutely
 * necessary and not as a rule.
 * 
 * @author Georgiana Macariu
 * 
 * @param <T>
 *            The type of the actual result of the operation.
 */
public class EventDrivenOperation<T> implements IOperation<T>,
		IOperationCompletionHandler<T> {

	private CountDownLatch doneSignal;
	private AtomicReference<T> result;
	private AtomicReference<Throwable> exception;
	private List<IOperationCompletionHandler<T>> completionHandlers;
	private Runnable operation = null;

	/**
	 * Creates a new operation.
	 * 
	 * @param complHandlers
	 *            handlers to be called when the operation completes
	 */
	public EventDrivenOperation(
			final List<IOperationCompletionHandler<T>> complHandlers) {
		super();
		this.doneSignal = new CountDownLatch(1);
		this.result = new AtomicReference<T>();
		this.exception = new AtomicReference<Throwable>();
		this.completionHandlers = new ArrayList<IOperationCompletionHandler<T>>();
		this.completionHandlers.add(this);
		this.completionHandlers.addAll(complHandlers);
	}

	/**
	 * Creates a new operation.
	 * 
	 * @param complHandlers
	 *            handlers to be called when the operation completes
	 * @param invocationHandler
	 *            an invocation handler which shall be used to invoke the
	 *            completion handlers. This can be used for controlling how the
	 *            completion handlers are executed
	 */
	public EventDrivenOperation(
			final List<IOperationCompletionHandler<T>> complHandlers,
			final CompletionInvocationHandler<T> invocationHandler) {
		this(complHandlers);

		if (invocationHandler != null) {
			List<IOperationCompletionHandler<T>> cHandlers = new ArrayList<IOperationCompletionHandler<T>>(
					this.completionHandlers);
			this.completionHandlers.clear();
			for (IOperationCompletionHandler<T> handler : cHandlers) {
				CompletionInvocationHandler<T> iHandler = invocationHandler
						.createHandler(handler);
				@SuppressWarnings("unchecked")
				IOperationCompletionHandler<T> proxy = (IOperationCompletionHandler<T>) Proxy
						.newProxyInstance(
								Thread.currentThread().getContextClassLoader(),//handler.getClass().getClassLoader(),
								new Class[] { IOperationCompletionHandler.class },  // NOPMD by georgiana on 10/12/11 5:01 PM
								iHandler);
				this.completionHandlers.add(proxy);
			}
		}
	}

	@Override
	public boolean cancel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return (this.result.get() == null);
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		this.doneSignal.await();
		return this.result.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		this.doneSignal.await(timeout, unit);
		return this.result.get();
	}

	public Runnable getOperation() {
		return this.operation;
	}

	public void setOperation(Runnable operation) {
		if (this.operation == null) {
			this.operation = operation;
		}
	}

	@Override
	public void onSuccess(T response) {
		if (!this.result.compareAndSet(null, response)) {
			ExceptionTracer.traceIgnored(new ResultSetException(
					"Operation result cannot be set."));
		}
		this.doneSignal.countDown();
	}

	@Override
	public <E extends Throwable> void onFailure(E error) {
		if (!this.exception.compareAndSet(null, error)) {
			ExceptionTracer.traceIgnored(new ResultSetException(
					"Operation result cannot be set."));
		}
		this.doneSignal.countDown();

	}

	public List<IOperationCompletionHandler<T>> getCompletionHandlers() {
		return this.completionHandlers;
	}

	// private abstract class Task<V> implements Runnable {
	// private final Callable<V> callable;
	// private volatile Thread runner; // required when implementing cancel
	//
	// public Task(Callable<V> callable) {
	// this.callable = callable;
	// }
	//
	// void run() {
	// runner = Thread.currentThread();
	// V result;
	// try {
	// result = callable.call();
	// } catch (Throwable ex) {
	// ExceptionTracer.traceDeferred(ex);
	// setException(ex);
	// return;
	// }
	// finish(result);
	// }
	//
	// private void setException(Throwable ex) {
	// for (IOperationCompletionHandler handler :
	// EventDrivenOperation.this.completionHandlers) {
	// handler.onFailure(ex);
	// }
	//
	// }
	//
	// private void finish(V result) {
	// for (IOperationCompletionHandler handler :
	// EventDrivenOperation.this.completionHandlers) {
	// handler.onSuccess(result);
	// }
	// }
	//
	// }
}