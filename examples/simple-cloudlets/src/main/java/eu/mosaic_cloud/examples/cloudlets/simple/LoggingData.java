/*
 * #%L
 * mosaic-examples-simple-cloudlets
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

package eu.mosaic_cloud.examples.cloudlets.simple;


import java.io.Serializable;


public final class LoggingData
		implements
			Serializable
{
	public LoggingData (final String user, final String password)
	{
		this.user = user;
		this.password = password;
	}
	
	public String getPassword ()
	{
		return this.password;
	}
	
	public String getUser ()
	{
		return this.user;
	}
	
	@Override
	public String toString ()
	{
		return this.user + "(" + this.password + ")";
	}
	
	final String password;
	final String user;
	private static final long serialVersionUID = 3715149789764562975L;
}