
package eu.mosaic_cloud.components.implementations.basic;


import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;

import com.google.common.base.Preconditions;
import eu.mosaic_cloud.callbacks.implementations.basic.BasicCallbackReactor;
import eu.mosaic_cloud.components.core.ComponentCallbacks;
import eu.mosaic_cloud.components.tools.DefaultJsonMessageCoder;
import eu.mosaic_cloud.exceptions.core.ExceptionResolution;
import eu.mosaic_cloud.exceptions.core.ExceptionTracer;
import eu.mosaic_cloud.exceptions.tools.AbortingExceptionTracer;
import eu.mosaic_cloud.transcript.core.Transcript;
import eu.mosaic_cloud.transcript.tools.TranscriptExceptionTracer;


public final class BasicComponentContainerMain
		extends Object
{
	private BasicComponentContainerMain ()
	{
		super ();
		throw (new UnsupportedOperationException ());
	}
	
	public static final void main (final ComponentCallbacks callbacks, final ExceptionTracer exceptions)
	{
		final Pipe inputPipe;
		final Pipe outputPipe;
		try {
			inputPipe = Pipe.open ();
			outputPipe = Pipe.open ();
		} catch (final IOException exception) {
			exceptions.trace (ExceptionResolution.Deferred, exception);
			throw (new Error (exception));
		}
		final Piper inputPiper = new Piper (Channels.newChannel (BasicComponentContainerPreMain.stdin), inputPipe.sink (), exceptions);
		final Piper outputPiper = new Piper (outputPipe.source (), Channels.newChannel (BasicComponentContainerPreMain.stdout), exceptions);
		final DefaultJsonMessageCoder coder = DefaultJsonMessageCoder.create ();
		final BasicCallbackReactor reactor = BasicCallbackReactor.create (exceptions);
		final BasicChannel channel = BasicChannel.create (inputPipe.source (), outputPipe.sink (), coder, reactor, exceptions);
		final BasicComponent component = BasicComponent.create (channel, reactor, exceptions);
		reactor.initialize ();
		channel.initialize ();
		component.initialize ();
		component.assign (callbacks);
		while (true) {
			if (!component.isActive ())
				break;
			try {
				Thread.sleep (BasicComponentContainerMain.sleepTimeout);
			} catch (final InterruptedException exception) {
				exceptions.trace (ExceptionResolution.Handled, exception);
				break;
			}
		}
		component.terminate ();
		channel.terminate ();
		reactor.terminate ();
		try {
			inputPiper.join ();
		} catch (final InterruptedException exception) {
			exceptions.trace (ExceptionResolution.Ignored, exception);
		}
		try {
			outputPiper.join ();
		} catch (final InterruptedException exception) {
			exceptions.trace (ExceptionResolution.Ignored, exception);
		}
	}
	
	public static final void main (final String componentArgument, final String classpathArgument)
	{
		Preconditions.checkNotNull (componentArgument);
		final ClassLoader classLoader;
		if (classpathArgument != null) {
			final LinkedList<URL> classLoaderUrls = new LinkedList<URL> ();
			for (final String classpathPart : classpathArgument.split ("\\|"))
				if (classpathPart.length () > 0) {
					final URL classpathUrl;
					if (classpathPart.startsWith ("http:") || classpathPart.startsWith ("file:"))
						try {
							classpathUrl = new URL (classpathPart);
						} catch (final Exception exception) {
							throw (new IllegalArgumentException (String.format ("invalid class-path URL `%s`", classpathPart), exception));
						}
					else
						throw (new IllegalArgumentException (String.format ("invalid class-path URL `%s`", classpathPart)));
					classLoaderUrls.add (classpathUrl);
				}
			classLoader = new URLClassLoader (classLoaderUrls.toArray (new URL[0]), BasicComponentContainerMain.class.getClassLoader ());
		} else
			classLoader = ClassLoader.getSystemClassLoader ();
		final Class<?> componentClass;
		try {
			componentClass = classLoader.loadClass (componentArgument);
		} catch (final Exception exception) {
			throw (new IllegalArgumentException (String.format ("invalid component class `%s` (error encountered while resolving)", componentArgument), exception));
		}
		Preconditions.checkArgument (ComponentCallbacks.class.isAssignableFrom (componentClass), "invalid component class `%s` (not an instance of `ComponentCallbacks`)", componentClass.getName ());
		final ComponentCallbacks callbacks;
		try {
			callbacks = (ComponentCallbacks) componentClass.newInstance ();
		} catch (final Exception exception) {
			throw (new IllegalArgumentException (String.format ("invalid component class `%s` (error encountered while instantiating)", componentClass.getName ()), exception));
		}
		BasicComponentContainerMain.main (callbacks, AbortingExceptionTracer.defaultInstance);
	}
	
	public static final void main (final String[] arguments)
	{
		Preconditions.checkNotNull (arguments);
		Preconditions.checkArgument ((arguments.length >= 1) && (arguments.length <= 2), "invalid arguments; aborting! (expected `<component-callbacks-class-name> <class-path-urls>`");
		final String componentArgument;
		final String classpathArgument;
		if (arguments.length == 1) {
			componentArgument = arguments[0];
			classpathArgument = null;
		} else if (arguments.length == 2) {
			componentArgument = arguments[0];
			classpathArgument = arguments[1];
		} else
			throw (new AssertionError ());
		BasicComponentContainerMain.main (componentArgument, classpathArgument);
	}
	
	private static final long sleepTimeout = 100;
	
	private static final class Piper
			extends Thread
			implements
				UncaughtExceptionHandler
	{
		Piper (final ReadableByteChannel source, final WritableByteChannel sink, final ExceptionTracer exceptions)
		{
			super ();
			this.transcript = Transcript.create (this);
			this.exceptions = TranscriptExceptionTracer.create (this.transcript, exceptions);
			this.source = source;
			this.sink = sink;
			this.setDaemon (true);
			this.setName (String.format ("Piper#%08x", Integer.valueOf (System.identityHashCode (this))));
			this.setUncaughtExceptionHandler (this);
			this.start ();
		}
		
		@Override
		public final void run ()
		{
			final ByteBuffer buffer = ByteBuffer.allocateDirect (1024 * 1024);
			loop : while (true) {
				if ((!this.source.isOpen () && (buffer.remaining () == 0)) || !this.sink.isOpen ())
					break;
				buffer.position (0);
				buffer.limit (buffer.capacity ());
				if (this.source.isOpen ()) {
					this.transcript.traceDebugging ("accessing source...");
					try {
						final int outcome = this.source.read (buffer);
						if (outcome == -1)
							this.source.close ();
					} catch (final IOException exception) {
						this.exceptions.traceHandledException (exception, "i/o error encountered while reading from the source channel; aborting!");
						break loop;
					}
					buffer.flip ();
				}
				while (this.sink.isOpen ()) {
					this.transcript.traceDebugging ("accessing sink...");
					try {
						this.sink.write (buffer);
					} catch (final IOException exception) {
						this.exceptions.traceHandledException (exception, "i/o error encountered while writing to the sink channel; aborting!");
						break loop;
					}
					if (buffer.remaining () == 0)
						break;
				}
			}
			this.close ();
		}
		
		@Override
		public void uncaughtException (final Thread thread, final Throwable exception)
		{
			Preconditions.checkArgument (this == thread);
			this.exceptions.traceIgnoredException (exception);
			this.close ();
		}
		
		private final void close ()
		{
			this.transcript.traceDebugging ("closing source...");
			try {
				this.source.close ();
			} catch (final Throwable exception) {
				this.exceptions.traceIgnoredException (exception);
			}
			this.transcript.traceDebugging ("closing sink...");
			try {
				this.sink.close ();
			} catch (final Throwable exception) {
				this.exceptions.traceIgnoredException (exception);
			}
		}
		
		private final TranscriptExceptionTracer exceptions;
		private final WritableByteChannel sink;
		private final ReadableByteChannel source;
		private final Transcript transcript;
	}
}
