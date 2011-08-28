package mosaic.driver.interop.kvstore.memcached;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mosaic.core.configuration.IConfiguration;
import mosaic.core.exceptions.ConnectionException;
import mosaic.core.exceptions.ExceptionTracer;
import mosaic.core.log.MosaicLogger;
import mosaic.core.ops.IResult;
import mosaic.driver.interop.AbstractDriverStub;
import mosaic.driver.interop.DriverConnectionData;
import mosaic.driver.interop.kvstore.KeyValueResponseTransmitter;
import mosaic.driver.interop.kvstore.KeyValueStub;
import mosaic.driver.kvstore.BaseKeyValueDriver;
import mosaic.driver.kvstore.KeyValueOperations;
import mosaic.driver.kvstore.memcached.MemcachedDriver;
import mosaic.interop.idl.IdlCommon.CompletionToken;
import mosaic.interop.idl.kvstore.KeyValuePayloads;
import mosaic.interop.idl.kvstore.KeyValuePayloads.GetRequest;
import mosaic.interop.idl.kvstore.KeyValuePayloads.SetRequest;
import mosaic.interop.idl.kvstore.MemcachedPayloads;
import mosaic.interop.idl.kvstore.MemcachedPayloads.AddRequest;
import mosaic.interop.idl.kvstore.MemcachedPayloads.AppendRequest;
import mosaic.interop.idl.kvstore.MemcachedPayloads.CasRequest;
import mosaic.interop.idl.kvstore.MemcachedPayloads.PrependRequest;
import mosaic.interop.idl.kvstore.MemcachedPayloads.ReplaceRequest;
import mosaic.interop.kvstore.KeyValueMessage;
import mosaic.interop.kvstore.KeyValueSession;
import mosaic.interop.kvstore.MemcachedMessage;
import mosaic.interop.kvstore.MemcachedSession;

import com.google.common.base.Preconditions;

import eu.mosaic_cloud.interoperability.core.Message;
import eu.mosaic_cloud.interoperability.core.Session;
import eu.mosaic_cloud.interoperability.implementations.zeromq.ZeroMqChannel;

/**
 * Stub for the driver for key-value distributed storage systems implementing
 * the memcached protocol. This is used for communicating with a memcached
 * driver.
 * 
 * @author Georgiana Macariu
 * 
 */
public class MemcachedStub extends KeyValueStub {

	private static Map<DriverConnectionData, MemcachedStub> stubs = new HashMap<DriverConnectionData, MemcachedStub>();

	/**
	 * Creates a new stub for the Memcached driver.
	 * 
	 * @param config
	 *            the configuration data for the stub and driver
	 * @param transmitter
	 *            the transmitter object which will send responses to requests
	 *            submitted to this stub
	 * @param driver
	 *            the driver used for processing requests submitted to this stub
	 * @param commChannel
	 *            the channel for communicating with connectors
	 */
	public MemcachedStub(IConfiguration config,
			KeyValueResponseTransmitter transmitter, BaseKeyValueDriver driver,
			ZeroMqChannel commChannel) {
		super(config, transmitter, driver, commChannel);

	}

	/**
	 * Returns a stub for the Memcached driver.
	 * 
	 * @param config
	 *            the configuration data for the stub and driver
	 * @param channel
	 *            the channel used by the driver for receiving requests
	 * @return the Memcached driver stub
	 */
	public static MemcachedStub create(IConfiguration config,
			ZeroMqChannel channel) {
		DriverConnectionData cData = KeyValueStub.readConnectionData(config);
		MemcachedStub stub = null;
		synchronized (AbstractDriverStub.lock) {
			stub = MemcachedStub.stubs.get(cData);
			try {
				if (stub == null) {
					MosaicLogger.getLogger().trace(
							"MemcachedStub: create new stub.");

					MemcachedResponseTransmitter transmitter = new MemcachedResponseTransmitter(
							config);
					MemcachedDriver driver = MemcachedDriver.create(config);
					stub = new MemcachedStub(config, transmitter, driver,
							channel);
					MemcachedStub.stubs.put(cData, stub);
					incDriverReference(stub);
					channel.accept(KeyValueSession.DRIVER, stub);
					channel.accept(MemcachedSession.DRIVER, stub);
				} else {
					MosaicLogger.getLogger().trace(
							"MemcachedStub: use existing stub.");
					incDriverReference(stub);
				}
			} catch (IOException e) {
				ExceptionTracer.traceDeferred(new ConnectionException(
						"The Memcached proxy cannot connect to the driver: "
								+ e.getMessage(), e));
			}
		}
		return stub;
	}

	@Override
	public void destroy() {
		synchronized (AbstractDriverStub.lock) {
			int ref = decDriverReference(this);
			if ((ref == 0)) {
				DriverConnectionData cData = KeyValueStub
						.readConnectionData(this.configuration);
				MemcachedStub.stubs.remove(cData);
			}

		}
		super.destroy();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void startOperation(Message message, Session session)
			throws IOException, ClassNotFoundException {
		Preconditions
				.checkArgument((message.specification instanceof KeyValueMessage)
						|| (message.specification instanceof MemcachedMessage));

		byte[] data;
		CompletionToken token = null;
		String key;
		int exp;
		IResult<Boolean> resultStore = null;
		DriverOperationFinishedHandler callback;

		MemcachedDriver driver = super.getDriver(MemcachedDriver.class);
		if (message.specification instanceof KeyValueMessage) {
			// handle set with exp
			boolean handle = false;
			KeyValueMessage kvMessage = (KeyValueMessage) message.specification;
			if (kvMessage == KeyValueMessage.SET_REQUEST) {
				KeyValuePayloads.SetRequest setRequest = (SetRequest) message.payload;
				if (setRequest.hasExpTime()) {
					token = setRequest.getToken();
					key = setRequest.getKey();

					MosaicLogger.getLogger().trace(
							"MemcachedStub - Received request for "
									+ " SET key: " + key + " - request id: "
									+ token.getMessageId() + " client id: "
									+ token.getClientId());

					exp = setRequest.getExpTime();
					data = setRequest.getValue().toByteArray();

					callback = new DriverOperationFinishedHandler(token,
							session, MemcachedDriver.class,
							MemcachedResponseTransmitter.class);
					resultStore = driver.invokeSetOperation(
							token.getClientId(), key, exp, data, callback);
					callback.setDetails(KeyValueOperations.SET, resultStore);
					handle = true;
				}
			} else if (kvMessage == KeyValueMessage.GET_REQUEST) {
				KeyValuePayloads.GetRequest getRequest = (GetRequest) message.payload;
				if (getRequest.getKeyCount() > 1) {
					token = getRequest.getToken();
					MosaicLogger.getLogger().trace(
							"KeyValueStub - Received request for "
									+ "GET_BULK  - request id: "
									+ token.getMessageId() + " client id: "
									+ token.getClientId());

					callback = new DriverOperationFinishedHandler(token,
							session, MemcachedDriver.class,
							MemcachedResponseTransmitter.class);
					IResult<Map<String, byte[]>> resultGet = driver
							.invokeGetBulkOperation(token.getClientId(),
									getRequest.getKeyList(), callback);

					callback.setDetails(KeyValueOperations.GET, resultGet);
					handle = true;
				}
			}

			if (!handle) {
				handleKVOperation(message, session, driver,
						MemcachedResponseTransmitter.class);
			}
			return;
		}

		MemcachedMessage mcMessage = (MemcachedMessage) message.specification;

		switch (mcMessage) {
		case ADD_REQUEST:
			MemcachedPayloads.AddRequest addRequest = (AddRequest) message.payload;
			token = addRequest.getToken();
			key = addRequest.getKey();

			MosaicLogger.getLogger().trace(
					"KeyValueStub - Received request for "
							+ mcMessage.toString() + " key: " + key
							+ " - request id: " + token.getMessageId()
							+ " client id: " + token.getClientId());

			exp = addRequest.getExpTime();
			data = addRequest.getValue().toByteArray();

			callback = new DriverOperationFinishedHandler(token, session,
					MemcachedDriver.class, MemcachedResponseTransmitter.class);
			resultStore = driver.invokeAddOperation(token.getClientId(), key,
					exp, data, callback);
			callback.setDetails(KeyValueOperations.ADD, resultStore);
			break;
		case APPEND_REQUEST:
			MemcachedPayloads.AppendRequest appendRequest = (AppendRequest) message.payload;
			token = appendRequest.getToken();
			key = appendRequest.getKey();

			MosaicLogger.getLogger().trace(
					"KeyValueStub - Received request for "
							+ mcMessage.toString() + " key: " + key
							+ " - request id: " + token.getMessageId()
							+ " client id: " + token.getClientId());

			exp = appendRequest.getExpTime();
			data = appendRequest.getValue().toByteArray();

			callback = new DriverOperationFinishedHandler(token, session,
					MemcachedDriver.class, MemcachedResponseTransmitter.class);
			resultStore = driver.invokeAppendOperation(token.getClientId(),
					key, data, callback);
			callback.setDetails(KeyValueOperations.APPEND, resultStore);
			break;
		case PREPEND_REQUEST:
			MemcachedPayloads.PrependRequest prependRequest = (PrependRequest) message.payload;
			token = prependRequest.getToken();
			key = prependRequest.getKey();

			MosaicLogger.getLogger().trace(
					"KeyValueStub - Received request for "
							+ mcMessage.toString() + " key: " + key
							+ " - request id: " + token.getMessageId()
							+ " client id: " + token.getClientId());

			exp = prependRequest.getExpTime();
			data = prependRequest.getValue().toByteArray();

			callback = new DriverOperationFinishedHandler(token, session,
					MemcachedDriver.class, MemcachedResponseTransmitter.class);
			resultStore = driver.invokePrependOperation(token.getClientId(),
					key, data, callback);
			callback.setDetails(KeyValueOperations.PREPEND, resultStore);
			break;
		case REPLACE_REQUEST:
			MemcachedPayloads.ReplaceRequest replaceRequest = (ReplaceRequest) message.payload;
			token = replaceRequest.getToken();
			key = replaceRequest.getKey();

			MosaicLogger.getLogger().trace(
					"KeyValueStub - Received request for "
							+ mcMessage.toString() + " key: " + key
							+ " - request id: " + token.getMessageId()
							+ " client id: " + token.getClientId());

			exp = replaceRequest.getExpTime();
			data = replaceRequest.getValue().toByteArray();

			callback = new DriverOperationFinishedHandler(token, session,
					MemcachedDriver.class, MemcachedResponseTransmitter.class);
			resultStore = driver.invokeReplaceOperation(token.getClientId(),
					key, exp, data, callback);
			callback.setDetails(KeyValueOperations.REPLACE, resultStore);
			break;
		case CAS_REQUEST:
			MemcachedPayloads.CasRequest casRequest = (CasRequest) message.payload;
			token = casRequest.getToken();
			key = casRequest.getKey();

			MosaicLogger.getLogger().trace(
					"KeyValueStub - Received request for "
							+ mcMessage.toString() + " key: " + key
							+ " - request id: " + token.getMessageId()
							+ " client id: " + token.getClientId());

			exp = casRequest.getExpTime();
			data = casRequest.getValue().toByteArray();

			callback = new DriverOperationFinishedHandler(token, session,
					MemcachedDriver.class, MemcachedResponseTransmitter.class);
			resultStore = driver.invokeCASOperation(token.getClientId(), key,
					data, callback);
			callback.setDetails(KeyValueOperations.CAS, resultStore);
			break;
		}
	}

}
