package mosaic.core.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import mosaic.core.exceptions.ExceptionTracer;

/**
 * This class implements a configuration handler for project configurations
 * based on property files.
 * 
 * @author CiprianCraciun, Georgiana Macariu
 * 
 */
public final class PropertyTypeConfiguration implements IConfiguration {

	private final Properties properties;

	private final ConfigurationIdentifier root;

	private PropertyTypeConfiguration(Properties properties,
			ConfigurationIdentifier root) {
		super();
		this.properties = properties;
		this.root = root;
	}

	private PropertyTypeConfiguration(Properties properties) {
		super();
		this.properties = properties;
		this.root = ConfigurationIdentifier.root;
	}

	/**
	 * Creates a configuration object and loads the configuration parameters
	 * from the specified resource file using a specific class loader.
	 * 
	 * @param classLoader
	 *            the class loader used for loading the configuration file
	 * @param resource
	 *            the name of the configuration file
	 * @return the configuration object
	 */
	public final static PropertyTypeConfiguration create(
			ClassLoader classLoader, String resource) {
		final InputStream stream = classLoader.getResourceAsStream(resource);
		PropertyTypeConfiguration configuration = null;
		if (stream != null) {
			final Properties properties = new Properties(System.getProperties());
			try {
				properties.load(stream);
				stream.close();
				configuration = new PropertyTypeConfiguration(properties);
			} catch (final IOException exception) {
				ExceptionTracer.traceIgnored(exception);
			}
		}
		return configuration;
	}

	/**
	 * Creates a configuration object and loads the configuration parameters
	 * from the specified input stream.
	 * 
	 * @param stream
	 *            the input stream from where to load configuration parameters
	 * @return the configuration object
	 * @throws IOException
	 *             if an error occurred when reading from the input stream
	 */
	public final static PropertyTypeConfiguration create(InputStream stream)
			throws IOException {
		final Properties properties = new Properties(System.getProperties());
		if (stream != null) {
			properties.load(stream);
			stream.close();
		}
		PropertyTypeConfiguration configuration = new PropertyTypeConfiguration(
				properties);
		return configuration;
	}

	@Override
	public final <T extends Object> PropertyTypeParameter<T> getParameter(
			final ConfigurationIdentifier identifier, final Class<T> valueClass) {
		return (new PropertyTypeParameter<T>(identifier, valueClass));
	}

	@Override
	public final PropertyTypeConfiguration spliceConfiguration(
			final ConfigurationIdentifier relative) {
		final ConfigurationIdentifier root;
		if (relative.isAbsolute()) {
			root = relative;
		} else {
			root = this.root.resolve(relative);
		}
		return (new PropertyTypeConfiguration(this.properties, root));
	}

	private <T extends Object> T decodeValue(Class<T> valueClass,
			String encodedValue) {
		T value;
		if (valueClass == String.class) {
			value = valueClass.cast(encodedValue);
		} else if (valueClass == Boolean.class) {
			value = valueClass.cast(Boolean.parseBoolean(encodedValue));
		} else if (valueClass == Integer.class) {
			value = valueClass.cast(Integer.parseInt(encodedValue));
		} else if (valueClass == Long.class) {
			value = valueClass.cast(Long.parseLong(encodedValue));
		} else if (valueClass == Double.class) {
			value = valueClass.cast(Double.parseDouble(encodedValue));
		} else if (valueClass == Float.class) {
			value = valueClass.cast(Float.parseFloat(encodedValue));
		} else if (valueClass == Character.class) {
			if (encodedValue.length() != 1)
				throw (new IllegalArgumentException());
			value = valueClass.cast(encodedValue.charAt(0));
		} else
			throw (new IllegalAccessError());
		return (value);
	}

	private final String selectEncodedValue(
			final ConfigurationIdentifier identifier) {
		final String key_;
		if (identifier.isAbsolute())
			if (identifier == ConfigurationIdentifier.root) {
				key_ = "";
			} else {
				key_ = identifier.getIdentifier();
			}
		else {
			key_ = this.root.resolve(identifier).getIdentifier();
		}
		final String key = key_.substring(1).replace('/', '.');
		synchronized (this) {
			final String encodedValue = this.properties.getProperty(key, null);
			return (encodedValue);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object config) {
		if (config == null)
			return false;
		if (!(config instanceof PropertyTypeConfiguration))
			return false;
		PropertyTypeConfiguration otherConfig = (PropertyTypeConfiguration) config;
		if ((this.root != null) && !this.root.equals(otherConfig.root))
			return false;
		if ((this.root == null) && (otherConfig.root != null))
			return false;
		if ((this.properties != null)
				&& !this.properties.equals(otherConfig.properties))
			return false;
		if ((this.properties == null) && (otherConfig.properties != null))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Object key : this.properties.keySet()) {
			builder.append(key.toString() + ": "
					+ this.properties.getProperty(key.toString()) + "\n");
		}
		return builder.toString();
	}

	/**
	 * Implements the configuration parameter in property style configuration
	 * object.
	 * 
	 * @author Ciprian Craciun, Georgiana Macariu
	 * 
	 * @param <T>
	 *            the type of the value of the parameter
	 */
	public final class PropertyTypeParameter<T> implements
			IConfigurationParameter<T> {
		private final ConfigurationIdentifier identifier;
		private T value;
		private final Class<T> valueClass;

		public PropertyTypeParameter(ConfigurationIdentifier identifier,
				Class<T> valueClass) {
			super();
			this.identifier = identifier;
			this.valueClass = valueClass;
		}

		@Override
		public final ConfigurationIdentifier getIdentifier() {
			return (this.identifier);
		}

		@Override
		public final T getValue(final T defaultValue) {
			final T value;

			if (this.value == null) {
				if (this.valueClass == IConfiguration.class) {
					this.value = this.valueClass
							.cast(PropertyTypeConfiguration.this
									.spliceConfiguration(this.identifier));
				} else {
					final String encodedValue = PropertyTypeConfiguration.this
							.selectEncodedValue(this.identifier);
					if (encodedValue != null) {
						this.value = PropertyTypeConfiguration.this
								.decodeValue(this.valueClass, encodedValue);
					}
				}
			}

			if (this.value != null) {
				value = this.value;
			} else {
				value = defaultValue;
			}
			return (value);
		}

		@Override
		public final Class<T> getValueClass() {
			return (this.valueClass);
		}

	}

	@Override
	public <T> void addParameter(ConfigurationIdentifier identifier, T value) {
		String property = identifier.getIdentifier();
		this.properties.put(property, value);

	}

	@Override
	public <T> void addParameter(String property, T value) {
		this.properties.put(property, value);
	}

	@Override
	public ConfigurationIdentifier getRootIdentifier() {
		return this.root;
	}
}
