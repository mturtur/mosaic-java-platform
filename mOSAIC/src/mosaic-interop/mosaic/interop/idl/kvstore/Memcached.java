/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package mosaic.interop.idl.kvstore;

@SuppressWarnings("all")
public interface Memcached {
	public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol
			.parse("{\"protocol\":\"Memcached\",\"namespace\":\"mosaic.interop.idl.kvstore\",\"types\":[{\"type\":\"enum\",\"name\":\"OperationNames\",\"symbols\":[\"SET\",\"ADD\",\"REPLACE\",\"APPEND\",\"PREPEND\",\"CAS\",\"GET\",\"GET_BULK\",\"DELETE\"]},{\"type\":\"error\",\"name\":\"MemcachedError\",\"fields\":[{\"name\":\"explanation\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"CompletionToken\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"client_id\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"StoreOperation\",\"fields\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"name\",\"type\":\"OperationNames\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"exptime\",\"type\":\"int\"},{\"name\":\"data\",\"type\":\"bytes\"}]},{\"type\":\"record\",\"name\":\"GetOperation\",\"fields\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"name\",\"type\":\"OperationNames\"},{\"name\":\"keys\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]},{\"type\":\"record\",\"name\":\"DeleteOperation\",\"fields\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"name\",\"type\":\"OperationNames\"},{\"name\":\"key\",\"type\":\"string\"}]},{\"type\":\"record\",\"name\":\"Operation\",\"fields\":[{\"name\":\"operation\",\"type\":[\"StoreOperation\",\"GetOperation\",\"DeleteOperation\"]}]},{\"type\":\"record\",\"name\":\"OperationResponse\",\"fields\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"name\",\"type\":\"OperationNames\"},{\"name\":\"is_error\",\"type\":\"boolean\"},{\"name\":\"response\",\"type\":[\"MemcachedError\",\"boolean\",{\"type\":\"map\",\"values\":\"bytes\"}]}]}],\"messages\":{\"add\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"exptime\",\"type\":\"long\"},{\"name\":\"nobytes\",\"type\":\"int\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"response\":\"null\",\"one-way\":true},\"set\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"exptime\",\"type\":\"long\"},{\"name\":\"nobytes\",\"type\":\"int\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"response\":\"null\",\"one-way\":true},\"replace\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"exptime\",\"type\":\"long\"},{\"name\":\"nobytes\",\"type\":\"int\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"response\":\"null\",\"one-way\":true},\"append\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"exptime\",\"type\":\"long\"},{\"name\":\"nobytes\",\"type\":\"int\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"response\":\"null\",\"one-way\":true},\"prepend\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"exptime\",\"type\":\"long\"},{\"name\":\"nobytes\",\"type\":\"int\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"response\":\"null\",\"one-way\":true},\"cas\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"exptime\",\"type\":\"long\"},{\"name\":\"nobytes\",\"type\":\"int\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"response\":\"null\",\"one-way\":true},\"get\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"key\",\"type\":\"string\"}],\"response\":\"null\",\"one-way\":true},\"get_bulk\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"key\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}],\"response\":\"null\",\"one-way\":true},\"delete\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"key\",\"type\":\"string\"}],\"response\":\"null\",\"one-way\":true},\"store_response\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"response\",\"type\":\"string\"}],\"response\":\"null\",\"one-way\":true},\"get_response\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"response\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}],\"response\":\"null\",\"one-way\":true},\"delete_response\":{\"request\":[{\"name\":\"token\",\"type\":\"CompletionToken\"},{\"name\":\"response\",\"type\":\"string\"}],\"response\":\"null\",\"one-way\":true}}}");

	void add(mosaic.interop.idl.kvstore.CompletionToken token,
			java.lang.CharSequence key, long exptime, int nobytes,
			java.nio.ByteBuffer data);

	void set(mosaic.interop.idl.kvstore.CompletionToken token,
			java.lang.CharSequence key, long exptime, int nobytes,
			java.nio.ByteBuffer data);

	void replace(mosaic.interop.idl.kvstore.CompletionToken token,
			java.lang.CharSequence key, long exptime, int nobytes,
			java.nio.ByteBuffer data);

	void append(mosaic.interop.idl.kvstore.CompletionToken token,
			java.lang.CharSequence key, long exptime, int nobytes,
			java.nio.ByteBuffer data);

	void prepend(mosaic.interop.idl.kvstore.CompletionToken token,
			java.lang.CharSequence key, long exptime, int nobytes,
			java.nio.ByteBuffer data);

	void cas(mosaic.interop.idl.kvstore.CompletionToken token,
			java.lang.CharSequence key, long exptime, int nobytes,
			java.nio.ByteBuffer data);

	void get(mosaic.interop.idl.kvstore.CompletionToken token,
			java.lang.CharSequence key);

	void get_bulk(mosaic.interop.idl.kvstore.CompletionToken token,
			java.util.List<java.lang.CharSequence> key);

	void delete(mosaic.interop.idl.kvstore.CompletionToken token,
			java.lang.CharSequence key);

	void store_response(mosaic.interop.idl.kvstore.CompletionToken token,
			java.lang.CharSequence response);

	void get_response(mosaic.interop.idl.kvstore.CompletionToken token,
			java.util.List<java.lang.CharSequence> response);

	void delete_response(mosaic.interop.idl.kvstore.CompletionToken token,
			java.lang.CharSequence response);
}
