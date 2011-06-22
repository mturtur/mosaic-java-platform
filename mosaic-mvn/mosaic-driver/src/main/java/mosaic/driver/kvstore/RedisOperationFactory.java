package mosaic.driver.kvstore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import mosaic.core.ops.GenericOperation;
import mosaic.core.ops.IOperation;
import mosaic.core.ops.IOperationFactory;
import mosaic.core.ops.IOperationType;
import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

/**
 * Factory class which builds the asynchronous calls for the operations defined
 * on the Redis key-value store.
 * 
 * @author Georgiana Macariu
 * 
 */
public class RedisOperationFactory implements IOperationFactory {
	private Jedis redisClient = null;

	private RedisOperationFactory(Jedis mcClient) {
		super();
		this.redisClient = mcClient;
	}

	/**
	 * Creates a new factory.
	 * 
	 * @param client
	 *            the Redis client used for communicating with the key-value
	 *            system
	 * @return the factory
	 */
	public final static RedisOperationFactory getFactory(Jedis client) {
		return new RedisOperationFactory(client);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mosaic.core.IOperationFactory#getOperation(mosaic.core.IOperationType,
	 * java.lang.Object[])
	 */
	@Override
	public IOperation<?> getOperation(final IOperationType type,
			Object... parameters) {
		IOperation<?> operation = null;
		if (!(type instanceof KeyValueOperations)) {
			operation = new GenericOperation<Object>(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					throw new UnsupportedOperationException(
							"Unsupported operation: " + type.toString());
				}

			});
			return operation;
		}

		final KeyValueOperations mType = (KeyValueOperations) type;
		final String key;
		String data;
		final byte[] keyBytes;
		final byte[] dataBytes;
		switch (mType) {
		case SET:
			key = (String) parameters[0];
			data = (String) parameters[1];
			keyBytes = SafeEncoder.encode(key);
			dataBytes = SafeEncoder.encode(data);
			operation = new GenericOperation<Boolean>(
					new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							String opResult = redisClient.set(keyBytes,
									dataBytes);
							opResult = opResult.trim();
							if (opResult.equalsIgnoreCase("OK"))
								return true;
							return false;
						}

					});
			break;
		case GET:
			key = (String) parameters[0];
			operation = new GenericOperation<Object>(
					new Callable<Object>() {

						@Override
						public Object call() throws Exception {
							String result=redisClient.get(key);
							return result;
						}

					});
			break;
		case LIST:
			final String pattern = "*";
			operation = new GenericOperation<List<String>>(
					new Callable<List<String>>() {

						@Override
						public List<String> call() throws Exception {
							Set<String> opResult = redisClient
									.keys(pattern);
							List<String> result = new ArrayList<String>();
							for (String key : opResult) {
								result.add(key);
							}
							return result;
						}

					});
			break;
		case DELETE:
			key = (String) parameters[0];
			keyBytes = SafeEncoder.encode(key);
			operation = new GenericOperation<Boolean>(
					new Callable<Boolean>() {

						@Override
						public Boolean call() throws Exception {
							long opResult = redisClient.del(keyBytes);
							if (opResult == 0)
								return false;
							return true;
						}

					});
			break;
		default:
			operation = new GenericOperation<Object>(
					new Callable<Object>() {

						@Override
						public Object call() throws Exception {
							throw new UnsupportedOperationException(
									"Unsupported operation: "
											+ mType.toString());
						}

					});
		}
		return operation;
	}

}
