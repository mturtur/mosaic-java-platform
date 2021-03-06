/*
 * #%L
 * mosaic-platform-interop
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

package eu.mosaic_cloud.platform.interop.specs.dfs;


import eu.mosaic_cloud.interoperability.core.MessageSpecification;
import eu.mosaic_cloud.interoperability.core.MessageType;
import eu.mosaic_cloud.interoperability.core.PayloadCoder;
import eu.mosaic_cloud.interoperability.tools.Identifiers;
import eu.mosaic_cloud.platform.interop.idl.IdlCommon;
import eu.mosaic_cloud.platform.interop.idl.dfs.DFSPayloads;
import eu.mosaic_cloud.platform.interop.tools.DefaultPBPayloadCoder;

import com.google.protobuf.GeneratedMessage;


public enum DFSHandlerMessage
			implements
				MessageSpecification
{
	BYTES (MessageType.Exchange, DFSPayloads.FileRead.class),
	CLOSE (MessageType.Exchange, DFSPayloads.CloseFile.class),
	FLUSH (MessageType.Exchange, DFSPayloads.FlushFile.class),
	OK (MessageType.Exchange, IdlCommon.Ok.class),
	READ (MessageType.Exchange, DFSPayloads.ReadFile.class),
	SEEK (MessageType.Exchange, DFSPayloads.SeekFile.class),
	SUCCESS (MessageType.Exchange, DFSPayloads.SuccessResponse.class),
	WRITE (MessageType.Exchange, DFSPayloads.WriteFile.class);
	DFSHandlerMessage (final MessageType type, final Class<? extends GeneratedMessage> clasz) {
		this.identifier = Identifiers.generate (this);
		this.type = type;
		if (clasz != null) {
			this.coder = new DefaultPBPayloadCoder (clasz, false);
		}
	}
	
	@Override
	public String getIdentifier () {
		return this.identifier;
	}
	
	@Override
	public PayloadCoder getPayloadCoder () {
		return this.coder;
	}
	
	@Override
	public String getQualifiedName () {
		return (Identifiers.generateName (this));
	}
	
	@Override
	public MessageType getType () {
		return this.type;
	}
	
	public PayloadCoder coder = null;
	public final String identifier;
	public final MessageType type;
}
