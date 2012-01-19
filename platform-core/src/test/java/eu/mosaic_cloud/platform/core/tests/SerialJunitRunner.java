/*
 * #%L
 * mosaic-platform-core
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
package eu.mosaic_cloud.platform.core.tests;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import eu.mosaic_cloud.platform.core.exceptions.ExceptionTracer;
import eu.mosaic_cloud.tools.threading.core.ThreadingContext;
import eu.mosaic_cloud.tools.threading.core.ThreadingContext.ThreadConfiguration;
import eu.mosaic_cloud.tools.threading.tools.Threading;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;


public class SerialJunitRunner extends BlockJUnit4ClassRunner {

	public SerialJunitRunner(final Class<?> klass) throws InitializationError {
		super(klass);
		setScheduler(new RunnerScheduler() {

			ThreadingContext threading = Threading.sequezeThreadingContextOutOfDryRock();
			ExecutorService executorService = threading.newFixedThreadPool(new ThreadConfiguration(this, "tests"), 1);
			CompletionService<Void> completionService = new ExecutorCompletionService<Void>(
					this.executorService);
			Queue<Future<Void>> tasks = new LinkedList<Future<Void>>();

			@Override
			public void schedule(final Runnable childStatement) {
				this.tasks.offer(this.completionService.submit(childStatement,
						null));
			}

			@Override
			public void finished() {
				try {
					while (!this.tasks.isEmpty()) {
						this.tasks.remove(this.completionService.take());
					}
				} catch (InterruptedException e) {
					ExceptionTracer.traceIgnored(e);
				} finally {
					while (!this.tasks.isEmpty()) {
						this.tasks.poll().cancel(true);
					}
					this.executorService.shutdownNow();
				}
			}
		});
	}

}
