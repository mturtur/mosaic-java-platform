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
package eu.mosaic_cloud.cloudlet.resources;

/**
 * Interface for all resource accessors used by cloudlets.
 * 
 * @author Georgiana Macariu
 * 
 * @param <S>
 *            the type of the cloudlet state
 */
public interface IResourceAccessor<S> {
	/**
	 * Initialize the accessor.
	 * 
	 * @param callback
	 *            handler for callbacks received from the resource
	 * @param state
	 *            cloudlet state
	 */
	void initialize(IResourceAccessorCallback<S> callback, S state);

	/**
	 * Destroys the accessor.
	 * 
	 * @param callback
	 *            handler for callbacks received from the resource
	 */
	void destroy(IResourceAccessorCallback<S> callback);

	/**
	 * Returns the current status of the accessor.
	 * 
	 * @return the current status of the accessor
	 */
	ResourceStatus getStatus();
}