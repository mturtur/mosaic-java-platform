/*
 * #%L
 * mosaic-cloudlet
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
package eu.mosaic_cloud.cloudlet.core;

import eu.mosaic_cloud.core.log.MosaicLogger;

/**
 * Default callback class.
 * 
 * @author Georgiana Macariu
 * 
 * @param <S>
 *            the type of the state of the cloudlet using this callback
 */
public class DefaultCallback<S> implements ICallback {
	/**
	 * Handles any unhandled callback.
	 * 
	 * @param arguments
	 *            the arguments of the callback
	 * @param callbackType
	 *            a string describing the type of callback (e.g. initialize)
	 * @param positive
	 *            <code>true</code> if callback corresponds to successful
	 *            termination of the operation
	 * @param couldDestroy
	 *            <code>true</code> if cloudlet can be destroyed here
	 */
	protected void handleUnhandledCallback(CallbackArguments<S> arguments,
			String callbackType, boolean positive, boolean couldDestroy) {
		this.traceUnhandledCallback(arguments, callbackType, positive);
		if (!positive && couldDestroy) {
			arguments.getCloudlet().destroy();
		}
	}

	/**
	 * Traces unhandled callbacks.
	 * 
	 * @param arguments
	 *            the arguments of the callback
	 * @param callbackType
	 *            a string describing the type of callback (e.g. initialize)
	 * @param positive
	 *            <code>true</code> if callback corresponds to successful
	 *            termination of the operation
	 */
	protected void traceUnhandledCallback(CallbackArguments<S> arguments,
			String callbackType, boolean positive) {
		MosaicLogger.getLogger().info(
				"unhandled cloudlet callback: `" + this.getClass().getName()
						+ "`@`" + callbackType + "` "
						+ (positive ? "Succeeded" : "Failed"));
	}
}