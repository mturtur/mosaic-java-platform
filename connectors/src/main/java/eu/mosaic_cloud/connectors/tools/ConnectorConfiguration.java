
package eu.mosaic_cloud.connectors.tools;


import eu.mosaic_cloud.interoperability.core.Channel;
import eu.mosaic_cloud.interoperability.core.ResolverCallbacks;
import eu.mosaic_cloud.platform.core.configuration.ConfigUtils;
import eu.mosaic_cloud.platform.core.configuration.IConfiguration;

import com.google.common.base.Preconditions;


public final class ConnectorConfiguration
{
	private ConnectorConfiguration (final IConfiguration configuration, final ConnectorEnvironment environment)
	{
		super ();
		Preconditions.checkNotNull (configuration);
		Preconditions.checkNotNull (environment);
		this.configuration = configuration;
		this.environment = environment;
	}
	
	public Channel getCommunicationChannel ()
	{
		return this.environment.getCommunicationChannel ();
	}
	
	public <T extends Object> T getConfigParameter (final String identifier, final Class<T> valueClass, final T defaultValue)
	{
		return ConfigUtils.resolveParameter (this.configuration, identifier, valueClass, defaultValue);
	}
	
	public void resolveChannel (final String driverTarget, final ResolverCallbacks resolverCallbacks)
	{
		this.environment.resolveChannel (driverTarget, resolverCallbacks);
	}
	
	public static final ConnectorConfiguration create (final IConfiguration configuration, final ConnectorEnvironment environment)
	{
		return (new ConnectorConfiguration (configuration, environment));
	}
	
	private final IConfiguration configuration;
	private final ConnectorEnvironment environment;
}