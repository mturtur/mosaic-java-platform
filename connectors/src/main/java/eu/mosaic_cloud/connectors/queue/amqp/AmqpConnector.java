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
package eu.mosaic_cloud.connectors.queue.amqp;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import eu.mosaic_cloud.connectors.ConfigProperties;
import eu.mosaic_cloud.connectors.interop.queue.amqp.AmqpProxy;
import eu.mosaic_cloud.drivers.queue.amqp.AmqpExchangeType;
import eu.mosaic_cloud.drivers.queue.amqp.AmqpOutboundMessage;
import eu.mosaic_cloud.interoperability.implementations.zeromq.ZeroMqChannel;
import eu.mosaic_cloud.platform.core.configuration.ConfigUtils;
import eu.mosaic_cloud.platform.core.configuration.IConfiguration;
import eu.mosaic_cloud.platform.core.log.MosaicLogger;
import eu.mosaic_cloud.platform.core.ops.CompletionInvocationHandler;
import eu.mosaic_cloud.platform.core.ops.EventDrivenOperation;
import eu.mosaic_cloud.platform.core.ops.EventDrivenResult;
import eu.mosaic_cloud.platform.core.ops.IOperationCompletionHandler;
import eu.mosaic_cloud.platform.core.ops.IResult;
import eu.mosaic_cloud.platform.interop.amqp.AmqpSession;
import eu.mosaic_cloud.tools.exceptions.tools.AbortingExceptionTracer;
import eu.mosaic_cloud.tools.threading.core.ThreadConfiguration;
import eu.mosaic_cloud.tools.threading.core.ThreadingContext;

/**
 * Connector for queuing systems implementing the AMQP protocol.
 * 
 * @author Georgiana Macariu
 * 
 */
public class AmqpConnector implements IAmqpQueueConnector {

	private AmqpProxy proxy;
	private ThreadingContext threading;
	private ExecutorService executor;
	private static MosaicLogger logger = MosaicLogger
			.createLogger(AmqpConnector.class);

	/**
	 * Creates a new AMQP connector.
	 * 
	 * @param proxy
	 *            the proxy for the connector
	 * @param noThreads
	 *            the number of threads to be used for processing requests
	 */
	private AmqpConnector(AmqpProxy proxy, ThreadingContext threading,
			int noThreads) {
		this.proxy = proxy;
		this.threading = threading;
		this.executor = this.threading
				.createFixedThreadPool(
						ThreadConfiguration.create(this, "operations", true),
						noThreads);
	}

	/**
	 * Returns an AMQP connector. For AMQP it should always return a new
	 * connector.
	 * 
	 * @param config
	 *            the configuration parameters required by the connector. This
	 *            should also include configuration settings for the
	 *            corresponding driver.
	 * @return the connector
	 * @throws Throwable
	 */
	public static synchronized AmqpConnector create(IConfiguration config,
			ThreadingContext threading) throws Throwable {
		String connectorIdentifier = UUID.randomUUID().toString();
		int noThreads = ConfigUtils
				.resolveParameter(
						config,
						ConfigProperties.getString("AmqpConnector.0"), Integer.class, 1); //$NON-NLS-1$
		String driverChannel = ConfigUtils.resolveParameter(config,
				ConfigProperties.getString("AllConnector.0"), String.class, "");
		String driverIdentifier = ConfigUtils.resolveParameter(config,
				ConfigProperties.getString("AllConnector.1"), String.class, "");

		logger.debug("Connector working with driver on " + driverIdentifier
				+ "(" + driverChannel + ")");
		ZeroMqChannel channel = ZeroMqChannel.create(connectorIdentifier,
				threading, AbortingExceptionTracer.defaultInstance);
		channel.register(AmqpSession.CONNECTOR);
		channel.connect(driverChannel);
		AmqpProxy proxy = AmqpProxy.create(config, connectorIdentifier,
				driverIdentifier, channel);
		return new AmqpConnector(proxy, threading, noThreads);
	}

	@Override
	public void destroy() throws Throwable {
		this.proxy.destroy();
		this.executor.shutdownNow();
		logger.trace("AmqpConnector was destroyed.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.mosaic_cloud.connectors.queue.IAmqpQueue#declareExchange(java.lang
	 * .String, eu.mosaic_cloud.drivers.queue.AmqpExchangeType, boolean,
	 * boolean, boolean, java.util.List,
	 * eu.mosaic_cloud.platform.core.ops.CompletionInvocationHandler)
	 */
	@Override
	public IResult<Boolean> declareExchange(final String name,
			final AmqpExchangeType type, final boolean durable,
			final boolean autoDelete, final boolean passive,
			List<IOperationCompletionHandler<Boolean>> handlers,
			CompletionInvocationHandler<Boolean> iHandler) {
		IResult<Boolean> result = null;
		// synchronized (this) {
		final EventDrivenOperation<Boolean> op = new EventDrivenOperation<Boolean>(
				handlers, iHandler);
		op.setOperation(new Runnable() {

			@Override
			public void run() {
				AmqpConnector.this.proxy.declareExchange(name, type, durable,
						autoDelete, passive, op.getCompletionHandlers());

			}
		});
		result = new EventDrivenResult<Boolean>(op);
		this.executor.submit(op.getOperation());
		// }

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.mosaic_cloud.connectors.queue.IAmqpQueue#declareQueue(java.lang.String
	 * , boolean, boolean, boolean, boolean, java.util.List,
	 * eu.mosaic_cloud.platform.core.ops.CompletionInvocationHandler)
	 */
	@Override
	public IResult<Boolean> declareQueue(final String queue,
			final boolean exclusive, final boolean durable,
			final boolean autoDelete, final boolean passive,
			List<IOperationCompletionHandler<Boolean>> handlers,
			CompletionInvocationHandler<Boolean> iHandler) {
		IResult<Boolean> result = null;
		// synchronized (this) {
		final EventDrivenOperation<Boolean> op = new EventDrivenOperation<Boolean>(
				handlers, iHandler);
		op.setOperation(new Runnable() {

			@Override
			public void run() {
				AmqpConnector.this.proxy.declareQueue(queue, exclusive,
						durable, autoDelete, passive,
						op.getCompletionHandlers());

			}
		});
		result = new EventDrivenResult<Boolean>(op);
		this.executor.submit(op.getOperation());
		// }

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.mosaic_cloud.connectors.queue.IAmqpQueue#bindQueue(java.lang.String,
	 * java.lang.String, java.lang.String, java.util.List,
	 * eu.mosaic_cloud.platform.core.ops.CompletionInvocationHandler)
	 */
	@Override
	public IResult<Boolean> bindQueue(final String exchange,
			final String queue, final String routingKey,
			List<IOperationCompletionHandler<Boolean>> handlers,
			CompletionInvocationHandler<Boolean> iHandler) {
		IResult<Boolean> result = null;
		// synchronized (this) {
		final EventDrivenOperation<Boolean> op = new EventDrivenOperation<Boolean>(
				handlers, iHandler);
		op.setOperation(new Runnable() {

			@Override
			public void run() {
				AmqpConnector.this.proxy.bindQueue(exchange, queue, routingKey,
						op.getCompletionHandlers());

			}
		});
		result = new EventDrivenResult<Boolean>(op);
		this.executor.submit(op.getOperation());
		// }

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.mosaic_cloud.connectors.queue.IAmqpQueue#publish(eu.mosaic_cloud.
	 * connectors.queue. AmqpOutboundMessage, java.util.List,
	 * eu.mosaic_cloud.platform.core.ops.CompletionInvocationHandler)
	 */
	@Override
	public IResult<Boolean> publish(final AmqpOutboundMessage message,
			List<IOperationCompletionHandler<Boolean>> handlers,
			CompletionInvocationHandler<Boolean> iHandler) {
		IResult<Boolean> result = null;
		// synchronized (this) {
		final EventDrivenOperation<Boolean> op = new EventDrivenOperation<Boolean>(
				handlers, iHandler);
		op.setOperation(new Runnable() {

			@Override
			public void run() {
				AmqpConnector.this.proxy.publish(message,
						op.getCompletionHandlers());

			}
		});
		result = new EventDrivenResult<Boolean>(op);
		this.executor.submit(op.getOperation());
		// }

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.mosaic_cloud.connectors.queue.IAmqpQueue#consume(java.lang.String,
	 * java.lang.String, boolean, boolean, java.lang.Object, java.util.List,
	 * eu.mosaic_cloud.platform.core.ops.CompletionInvocationHandler,
	 * eu.mosaic_cloud.connectors.queue.IAmqpConsumerCallback)
	 */
	@Override
	public IResult<String> consume(final String queue, final String consumer,
			final boolean exclusive, final boolean autoAck, final Object extra,
			List<IOperationCompletionHandler<String>> handlers,
			CompletionInvocationHandler<String> iHandler,
			final IAmqpConsumerCallback consumerCallback) {
		IResult<String> result = null;
		// synchronized (this) {
		final EventDrivenOperation<String> op = new EventDrivenOperation<String>(
				handlers, iHandler);
		op.setOperation(new Runnable() {

			@Override
			public void run() {
				AmqpConnector.this.proxy.consume(queue, consumer, exclusive,
						autoAck, extra, op.getCompletionHandlers(),
						consumerCallback);

			}
		});
		result = new EventDrivenResult<String>(op);
		this.executor.submit(op.getOperation());
		// }

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.mosaic_cloud.connectors.queue.IAmqpQueue#cancel(java.lang.String,
	 * java.util.List,
	 * eu.mosaic_cloud.platform.core.ops.CompletionInvocationHandler)
	 */
	@Override
	public IResult<Boolean> cancel(final String consumer,
			List<IOperationCompletionHandler<Boolean>> handlers,
			CompletionInvocationHandler<Boolean> iHandler) {
		IResult<Boolean> result = null;
		// synchronized (this) {
		final EventDrivenOperation<Boolean> op = new EventDrivenOperation<Boolean>(
				handlers, iHandler);
		op.setOperation(new Runnable() {

			@Override
			public void run() {
				AmqpConnector.this.proxy.cancel(consumer,
						op.getCompletionHandlers());

			}
		});
		result = new EventDrivenResult<Boolean>(op);
		this.executor.submit(op.getOperation());
		// }

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.mosaic_cloud.connectors.queue.IAmqpQueue#get(java.lang.String,
	 * boolean, java.util.List,
	 * eu.mosaic_cloud.platform.core.ops.CompletionInvocationHandler)
	 */
	@Override
	public IResult<Boolean> get(final String queue, final boolean autoAck,
			List<IOperationCompletionHandler<Boolean>> handlers,
			CompletionInvocationHandler<Boolean> iHandler) {
		IResult<Boolean> result = null;
		// synchronized (this) {
		final EventDrivenOperation<Boolean> op = new EventDrivenOperation<Boolean>(
				handlers, iHandler);
		op.setOperation(new Runnable() {

			@Override
			public void run() {
				AmqpConnector.this.proxy.get(queue, autoAck,
						op.getCompletionHandlers());

			}
		});
		result = new EventDrivenResult<Boolean>(op);
		this.executor.submit(op.getOperation());
		// }

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.mosaic_cloud.connectors.queue.IAmqpQueue#ack(long, boolean,
	 * java.util.List,
	 * eu.mosaic_cloud.platform.core.ops.CompletionInvocationHandler)
	 */
	@Override
	public IResult<Boolean> ack(final long delivery, final boolean multiple,
			List<IOperationCompletionHandler<Boolean>> handlers,
			CompletionInvocationHandler<Boolean> iHandler) {
		IResult<Boolean> result = null;
		// synchronized (this) {
		final EventDrivenOperation<Boolean> op = new EventDrivenOperation<Boolean>(
				handlers, iHandler);
		op.setOperation(new Runnable() {

			@Override
			public void run() {
				AmqpConnector.this.proxy.ack(delivery, multiple,
						op.getCompletionHandlers());

			}
		});
		result = new EventDrivenResult<Boolean>(op);
		this.executor.submit(op.getOperation());
		// }

		return result;
	}

}
