/*
 * #%L
 * mosaic-platform-core
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

package eu.mosaic_cloud.platform.implementation.v2.serialization;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 * Defines utility methods for serializing and deserializing messages.
 * 
 * @author Georgiana Macariu
 */
public final class SerDesUtils
{
	private SerDesUtils () {
		throw (new UnsupportedOperationException ());
	}
	
	/**
	 * Converts an array of bytes corresponding to a JSON object back to its constituent Java Bean object. The input array is
	 * assumed to have been created from the original object.
	 * 
	 * @param bytes
	 *            the byte array to convert.
	 * @param valueClass
	 *            the class of the bean object
	 * @return the associated object.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static <T extends Object> T jsonToObject (final byte[] bytes, final Class<T> valueClass, final Charset charset)
				throws IOException {
		// FIXME: Should be able to use the charset...
		T object = null;
		if (bytes.length > 0) {
			object = SerDesUtils.objectMapper.readValue (bytes, 0, bytes.length, valueClass);
		}
		return object;
	}
	
	public static JSONObject jsonToRawObject (final byte[] dataBytes, final Charset charset)
				throws JSONException {
		if (dataBytes.length == 0) {
			return null;
		}
		final BufferedReader reader = new BufferedReader (new InputStreamReader (new ByteArrayInputStream (dataBytes), charset));
		final JSONTokener tokener = new JSONTokener (reader);
		final JSONObject object = new JSONObject (tokener);
		return object;
	}
	
	/**
	 * Converts an object to an array of bytes .
	 * 
	 * @param object
	 *            the object to convert.
	 * @return the associated byte array.
	 */
	public static byte[] pojoToBytes (final Object object)
				throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		final ObjectOutputStream oos = new ObjectOutputStream (baos);
		oos.writeObject (object);
		oos.close ();
		return baos.toByteArray ();
	}
	
	public static byte[] toJsonBytes (final JSONObject data, final Charset charset)
				throws JSONException, IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		final Writer writer = new BufferedWriter (new OutputStreamWriter (baos, charset));
		data.write (writer);
		writer.close ();
		return baos.toByteArray ();
	}
	
	/**
	 * Converts an object (Java Bean) to an array of bytes corresponding to the JSON encoding of the bean..
	 * 
	 * @param object
	 *            the object to convert.
	 * @return the associated byte array.
	 */
	public static byte[] toJsonBytes (final Object object, final Charset charset)
				throws IOException {
		// FIXME: Should be able to use the charset...
		final byte[] bytes = SerDesUtils.objectMapper.writeValueAsString (object).getBytes ();
		return bytes;
	}
	
	/**
	 * Converts an array of bytes back to its constituent object. The input array is assumed to have been created from the
	 * original object.
	 * 
	 * @param bytes
	 *            the byte array to convert.
	 * @return the associated object.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Object toObject (final byte[] bytes)
				throws IOException, ClassNotFoundException {
		Object object = null;
		if (bytes.length > 0) {
			final ObjectInputStream stream = new SpecialObjectInputStream (new ByteArrayInputStream (bytes));
			object = stream.readObject ();
			stream.close ();
		}
		return object;
	}
	
	static {
		objectMapper = new ObjectMapper ();
		SerDesUtils.objectMapper.configure (SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
	}
	private static final ObjectMapper objectMapper;
	
	private static class SpecialObjectInputStream
				extends ObjectInputStream
	{
		public SpecialObjectInputStream (final InputStream stream)
					throws IOException {
			super (stream);
		}
		
		@Override
		public Class<?> resolveClass (final ObjectStreamClass descriptor)
					throws ClassNotFoundException, IOException {
			final ClassLoader currentLoader = Thread.currentThread ().getContextClassLoader ();
			Class<?> clasz = null;
			if (currentLoader != null) {
				try {
					clasz = currentLoader.loadClass (descriptor.getName ());
				} catch (final ClassNotFoundException exception) {
					// NOTE: intentional
				}
			}
			if (clasz == null) {
				clasz = super.resolveClass (descriptor);
			}
			return clasz;
		}
	}
}
