/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package mosaic.interop.idl.kvstore;

@SuppressWarnings("all")
public class DeleteOperation extends
		org.apache.avro.specific.SpecificRecordBase implements
		org.apache.avro.specific.SpecificRecord {
	public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema
			.parse("{\"type\":\"record\",\"name\":\"DeleteOperation\",\"namespace\":\"mosaic.interop.idl.kvstore\",\"fields\":[{\"name\":\"token\",\"type\":{\"type\":\"record\",\"name\":\"CompletionToken\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"client_id\",\"type\":\"string\"}]}},{\"name\":\"name\",\"type\":{\"type\":\"enum\",\"name\":\"OperationNames\",\"symbols\":[\"SET\",\"ADD\",\"REPLACE\",\"APPEND\",\"PREPEND\",\"CAS\",\"GET\",\"GET_BULK\",\"DELETE\"]}},{\"name\":\"key\",\"type\":\"string\"}]}");
	public mosaic.interop.idl.kvstore.CompletionToken token;
	public mosaic.interop.idl.kvstore.OperationNames name;
	public java.lang.CharSequence key;

	public org.apache.avro.Schema getSchema() {
		return SCHEMA$;
	}

	// Used by DatumWriter. Applications should not call.
	public java.lang.Object get(int field$) {
		switch (field$) {
		case 0:
			return token;
		case 1:
			return name;
		case 2:
			return key;
		default:
			throw new org.apache.avro.AvroRuntimeException("Bad index");
		}
	}

	// Used by DatumReader. Applications should not call.
	@SuppressWarnings(value = "unchecked")
	public void put(int field$, java.lang.Object value$) {
		switch (field$) {
		case 0:
			token = (mosaic.interop.idl.kvstore.CompletionToken) value$;
			break;
		case 1:
			name = (mosaic.interop.idl.kvstore.OperationNames) value$;
			break;
		case 2:
			key = (java.lang.CharSequence) value$;
			break;
		default:
			throw new org.apache.avro.AvroRuntimeException("Bad index");
		}
	}
}
