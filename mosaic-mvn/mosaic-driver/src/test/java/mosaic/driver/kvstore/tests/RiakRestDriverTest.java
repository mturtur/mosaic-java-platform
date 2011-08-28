package mosaic.driver.kvstore.tests;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import mosaic.core.SerialJunitRunner;
import mosaic.core.TestLoggingHandler;
import mosaic.core.configuration.PropertyTypeConfiguration;
import mosaic.core.ops.IOperationCompletionHandler;
import mosaic.core.ops.IResult;
import mosaic.core.utils.SerDesUtils;
import mosaic.driver.kvstore.BaseKeyValueDriver;
import mosaic.driver.kvstore.RiakRestDriver;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

@RunWith(SerialJunitRunner.class)
public class RiakRestDriverTest {
	private static BaseKeyValueDriver wrapper;
	private static String keyPrefix;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RiakRestDriverTest.wrapper = RiakRestDriver
				.create(PropertyTypeConfiguration.create(
						RiakRestDriverTest.class.getClassLoader(),
						"riakrest-test.prop"));
		RiakRestDriverTest.keyPrefix = UUID.randomUUID().toString();
		RiakRestDriverTest.wrapper.registerClient(RiakRestDriverTest.keyPrefix,
				"test");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		RiakRestDriverTest.wrapper
				.unregisterClient(RiakRestDriverTest.keyPrefix);
		RiakRestDriverTest.wrapper.destroy();
	}

	@Before
	public void setUp() {
	}

	@Test
	public void testConnection() {
		Assert.assertNotNull(RiakRestDriverTest.wrapper);
	}

	@Test
	public void testSet() throws IOException {
		String k1 = RiakRestDriverTest.keyPrefix + "_key_fantastic";
		byte[] b1 = SerDesUtils.pojoToBytes("fantastic");
		IOperationCompletionHandler<Boolean> handler1 = new TestLoggingHandler<Boolean>(
				"set 1");
		IResult<Boolean> r1 = RiakRestDriverTest.wrapper.invokeSetOperation(
				RiakRestDriverTest.keyPrefix, k1, b1, handler1);
		Assert.assertNotNull(r1);

		String k2 = RiakRestDriverTest.keyPrefix + "_key_famous";
		byte[] b2 = SerDesUtils.pojoToBytes("famous");
		IOperationCompletionHandler<Boolean> handler2 = new TestLoggingHandler<Boolean>(
				"set 2");
		IResult<Boolean> r2 = RiakRestDriverTest.wrapper.invokeSetOperation(
				RiakRestDriverTest.keyPrefix, k2, b2, handler2);
		Assert.assertNotNull(r2);

		try {
			Assert.assertTrue(r1.getResult());
			Assert.assertTrue(r2.getResult());
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (ExecutionException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testGet() throws IOException, ClassNotFoundException {
		String k1 = RiakRestDriverTest.keyPrefix + "_key_famous";
		IOperationCompletionHandler<byte[]> handler = new TestLoggingHandler<byte[]>(
				"get");
		IResult<byte[]> r1 = RiakRestDriverTest.wrapper.invokeGetOperation(
				RiakRestDriverTest.keyPrefix, k1, handler);

		try {
			Assert.assertEquals("famous", SerDesUtils.toObject(r1.getResult())
					.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (ExecutionException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testList() {
		String k1 = RiakRestDriverTest.keyPrefix + "_key_fantastic";
		String k2 = RiakRestDriverTest.keyPrefix + "_key_famous";
		// List<String> keys = new ArrayList<String>();
		// keys.add(k1);
		// keys.add(k2);
		IOperationCompletionHandler<List<String>> handler = new TestLoggingHandler<List<String>>(
				"list");
		IResult<List<String>> r1 = RiakRestDriverTest.wrapper
				.invokeListOperation(RiakRestDriverTest.keyPrefix, handler);

		try {
			List<String> lresult = r1.getResult();
			Assert.assertNotNull(lresult);
			// Assert.assertEquals(keys.size(), lresult.size());
			Assert.assertTrue(lresult.contains(k1));
			Assert.assertTrue(lresult.contains(k2));
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (ExecutionException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testDelete() {
		String k1 = RiakRestDriverTest.keyPrefix + "_key_fantastic";
		IOperationCompletionHandler<Boolean> handler1 = new TestLoggingHandler<Boolean>(
				"delete 1");
		IResult<Boolean> r1 = RiakRestDriverTest.wrapper.invokeDeleteOperation(
				RiakRestDriverTest.keyPrefix, k1, handler1);

		try {
			Assert.assertTrue(r1.getResult());
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (ExecutionException e) {
			e.printStackTrace();
			Assert.fail();
		}

		IOperationCompletionHandler<byte[]> handler3 = new TestLoggingHandler<byte[]>(
				"check deleted");
		IResult<byte[]> r3 = RiakRestDriverTest.wrapper.invokeGetOperation(
				RiakRestDriverTest.keyPrefix, k1, handler3);

		try {
			Assert.assertNull(r3.getResult());
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (ExecutionException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	public static void main(String[] args) {
		JUnitCore.main("mosaic.driver.kvstore.tests.RiakRestDriverTest");
	}
}
