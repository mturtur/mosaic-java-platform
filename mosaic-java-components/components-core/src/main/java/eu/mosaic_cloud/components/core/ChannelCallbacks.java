
package eu.mosaic_cloud.components.core;


import eu.mosaic_cloud.callbacks.core.CallbackReference;
import eu.mosaic_cloud.callbacks.core.Callbacks;


public interface ChannelCallbacks
		extends
			Callbacks
{
	public abstract CallbackReference closed (final Channel channel, final ChannelFlow flow);
	
	public abstract CallbackReference failed (final Channel channel, final Throwable exception);
	
	public abstract CallbackReference initialized (final Channel channel);
	
	public abstract CallbackReference received (final Channel channel, final ChannelMessage message);
	
	public abstract CallbackReference terminated (final Channel channel);
}