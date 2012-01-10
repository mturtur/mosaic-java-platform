/*
 * #%L
 * tools-exceptions
 * %%
 * Copyright (C) 2010 - 2011 mOSAIC Project
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

package eu.mosaic_cloud.exceptions.tools;


import eu.mosaic_cloud.exceptions.core.ExceptionResolution;
import eu.mosaic_cloud.exceptions.core.ExceptionTracer;


public class NullExceptionTracer
		extends Object
		implements
			ExceptionTracer
{
	private NullExceptionTracer ()
	{
		super ();
	}
	
	@Override
	public void trace (final ExceptionResolution resolution, final Throwable exception)
	{}
	
	@Override
	public void trace (final ExceptionResolution resolution, final Throwable exception, final String message)
	{}
	
	@Override
	public void trace (final ExceptionResolution resolution, final Throwable exception, final String format, final Object ... tokens)
	{}
	
	public static final NullExceptionTracer defaultInstance = new NullExceptionTracer ();
}