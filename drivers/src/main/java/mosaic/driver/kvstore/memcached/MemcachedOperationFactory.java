package mosaic.driver.kvstore.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import mosaic.core.exceptions.ExceptionTracer;
import mosaic.core.ops.GenericOperation;
import mosaic.core.ops.IOperation;
import mosaic.core.ops.IOperationFactory;
import mosaic.core.ops.IOperationType;
import mosaic.driver.kvstore.KeyValueOperations;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.CASResponse;
import net.spy.memcached.MemcachedClient;

/**
 * Factory class which builds the asynchronous calls for the operations defined
 * in the memcached protocol.
 * 
 * @author Georgiana Macariu
 * 
 */
public class MemcachedOperationFactory implements IOperationFactory {
	private MemcachedClient mcClient = null;

	private MemcachedOperationFactory(List<InetSocketAddress> servers,
			String user, String password, String bucket) throws IOException {
		super();
		// mcClient=new MemcachedClient(
		// nodes, bucket, user, passwd);
		this.mcClient = new MemcachedClient(new BinaryConnectionFactory(),
				servers);
	}

	/**
	 * Creates a new factory.
	 * 
	 * @param hosts
	 *            the hostname and port of the Memcached servers
	 * @param user
	 *            the username for connecting to the server
	 * @param passwd
	 *            the password for connecting to the server
	 * @param bucket
	 *            the bucket where all operations are applied
	 * @return the factory
	 */
	public final static MemcachedOperationFactory getFactory(
			List<InetSocketAddress> hosts, String user, String password,
			String bucket) {
		try {
			return new MemcachedOperationFactory(hosts, user, password, bucket);
		} catch (IOException e) {
			ExceptionTracer.traceDeferred(e);
		}
		return null;
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
		final int exp;
		final byte[] data;

		switch (mType) {
		case SET:
			key = (String) parameters[0];
			if (parameters.length == 3) {
				exp = (Integer) parameters[1];
				data = (byte[]) parameters[2];
			} else {
				exp = 0;
				data = (byte[]) parameters[1];
			}
			operation = new GenericOperation<Boolean>(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					Future<Boolean> opResult = MemcachedOperationFactory.this.mcClient
							.set(key, exp, data);
					Boolean result = opResult.get();
					return result;
				}

			});
			break;
		case ADD:
			key = (String) parameters[0];
			exp = (Integer) parameters[1];
			data = (byte[]) parameters[2];
			operation = new GenericOperation<Boolean>(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					Future<Boolean> opResult = MemcachedOperationFactory.this.mcClient
							.add(key, exp, data);
					Boolean result = opResult.get();
					return result;
				}

			});
			break;
		case REPLACE:
			key = (String) parameters[0];
			exp = (Integer) parameters[1];
			data = (byte[]) parameters[2];
			operation = new GenericOperation<Boolean>(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					Future<Boolean> opResult = MemcachedOperationFactory.this.mcClient
							.replace(key, exp, data);
					Boolean result = opResult.get();
					return result;
				}

			});
			break;
		case APPEND:
			key = (String) parameters[0];
			data = (byte[]) parameters[1];
			operation = new GenericOperation<Boolean>(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					long cas = MemcachedOperationFactory.this.mcClient
							.gets(key).getCas();
					Future<Boolean> opResult = MemcachedOperationFactory.this.mcClient
							.append(cas, key, data);
					Boolean result = opResult.get();
					return result;
				}

			});
			break;
		case PREPEND:
			key = (String) parameters[0];
			data = (byte[]) parameters[1];
			operation = new GenericOperation<Boolean>(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					long cas = MemcachedOperationFactory.this.mcClient
							.gets(key).getCas();
					Future<Boolean> opResult = MemcachedOperationFactory.this.mcClient
							.prepend(cas, key, data);
					Boolean result = opResult.get();
					return result;
				}

			});
			break;
		case CAS:
			key = (String) parameters[0];
			data = (byte[]) parameters[1];
			operation = new GenericOperation<Boolean>(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					long cas = MemcachedOperationFactory.this.mcClient
							.gets(key).getCas();
					Future<CASResponse> opResult = MemcachedOperationFactory.this.mcClient
							.asyncCAS(key, cas, data);
					Boolean result = (opResult.get() == CASResponse.OK);
					return result;
				}

			});
			break;
		case GET:
			key = (String) parameters[0];
			operation = new GenericOperation<byte[]>(new Callable<byte[]>() {

				@Override
				public byte[] call() throws Exception {
					Future<Object> opResult = MemcachedOperationFactory.this.mcClient
							.asyncGet(key);
					byte[] result = (byte[]) opResult.get();
					return result;
				}

			});
			break;
		case GET_BULK:
			final String[] keys = (String[]) parameters;
			operation = new GenericOperation<Map<String, byte[]>>(
					new Callable<Map<String, byte[]>>() {

						@Override
						public Map<String, byte[]> call() throws Exception {
							Future<Map<String, Object>> opResult = MemcachedOperationFactory.this.mcClient
									.asyncGetBulk(keys);
							Map<String, byte[]> result = new HashMap<String, byte[]>();
							for (Map.Entry<String, Object> entry : opResult
									.get().entrySet()) {
								result.put(entry.getKey(),
										(byte[]) entry.getValue());
							}
							return result;
						}

					});
			break;
		case DELETE:
			key = (String) parameters[0];
			operation = new GenericOperation<Boolean>(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					Future<Boolean> opResult = MemcachedOperationFactory.this.mcClient
							.delete(key);
					Boolean result = opResult.get();
					return result;
				}

			});
			break;
		default:
			operation = new GenericOperation<Object>(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					throw new UnsupportedOperationException(
							"Unsupported operation: " + mType.toString());
				}

			});

		}

		return operation;
	}

	@Override
	public void destroy() {
		this.mcClient.shutdown(30, TimeUnit.SECONDS);
	}

}