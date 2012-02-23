/*
 * #%L
 * mosaic-cloudlets
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

package eu.mosaic_cloud.cloudlets.connectors.kvstore;

import java.util.List;

import eu.mosaic_cloud.cloudlets.connectors.core.IConnector;
import eu.mosaic_cloud.tools.callbacks.core.CallbackCompletion;

/**
 * Basic interface for cloudlets to access key-value storages.
 * 
 * @author Georgiana Macariu
 * 
 * @param <Context>
 *            the type of the context of the cloudlet
 */
public interface IKvStoreConnector<Context, Value, Extra> extends IConnector<Context>,
        eu.mosaic_cloud.connectors.kvstore.IKvStoreConnector<Value> {

    /**
     * Stores the given data and associates it with the specified key.
     * 
     * @param key
     *            the key under which this data should be stored
     * @param data
     *            the data
     * @param extra
     *            some application specific data
     * @return a result handle for the operation
     */
    CallbackCompletion<Boolean> set(String key, Value value, Extra extra);

    /**
     * Gets data associated with a single key.
     * 
     * @param key
     *            the key
     * @param extra
     *            some application specific data
     * @return a result handle for the operation
     */
    CallbackCompletion<Value> get(String key, Extra extra);

    /**
     * Deletes the given key.
     * 
     * @param key
     *            the key to delete
     * @param extra
     *            some application specific data
     * @return a result handle for the operation
     */
    CallbackCompletion<Boolean> delete(String key, Extra extra);

    /**
     * Lists the keys in the bucket associated with the accessor.
     * 
     * @param extra
     *            some application specific data
     * @return a result handle for the operation
     */
    CallbackCompletion<List<String>> list(Extra extra);
}
