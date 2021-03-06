
package eu.mosaic_cloud.platform.v2.cloudlets.core;


import com.google.common.base.Preconditions;


public abstract class CloudletFailedArguments
			extends CloudletCallbackArguments
{
	protected CloudletFailedArguments (final CloudletController<?> cloudlet, final Throwable error) {
		super (cloudlet);
		Preconditions.checkNotNull (error);
		this.error = error;
	}
	
	public final Throwable error;
}
