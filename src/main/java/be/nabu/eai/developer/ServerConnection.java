package be.nabu.eai.developer;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedHashSet;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import be.nabu.eai.developer.managers.JDBCServiceGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.server.RemoteServer;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.utils.http.CustomCookieStore;
import be.nabu.utils.http.SPIAuthenticationHandler;
import be.nabu.utils.http.api.client.HTTPClient;
import be.nabu.utils.http.client.DefaultHTTPClient;
import be.nabu.utils.http.connections.PooledConnectionHandler;
import be.nabu.utils.mime.impl.FormatException;

public class ServerConnection {
	
	private HTTPClient client;
	private RemoteServer remote;
	private String host;
	private Integer port;
	
	ServerConnection(String host, Integer port) {
		this.host = host;
		this.port = port;
	}
	
	public static void draw(MainController controller) {
		SimpleProperty<String> serverProperty = new SimpleProperty<String>("server", String.class, true);
		SimpleProperty<Integer> portProperty = new SimpleProperty<Integer>("port", Integer.class, true);
		final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(serverProperty, portProperty)), 
			new ValueImpl<String>(serverProperty, "localhost"),
			new ValueImpl<Integer>(portProperty, 5555)
		);
		JDBCServiceGUIManager.buildPopup(controller, updater, "Connect", new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				String host = updater.getValue("server");
				Integer port = updater.getValue("port");
				controller.connect(new ServerConnection(host, port));
			}
		});
	}
	
	public URI getMavenRoot() throws IOException {
		try {
			return getRemote().getMavenRoot();
		}
		catch (FormatException e) {
			throw new RuntimeException(e);
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public URI getRepositoryRoot() throws IOException {
		try {
			return getRemote().getRepositoryRoot();
		}
		catch (FormatException e) {
			throw new RuntimeException(e);
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	HTTPClient getClient() {
		if (client == null) {
			synchronized(this) {
				client = new DefaultHTTPClient(new PooledConnectionHandler(null, 5), new SPIAuthenticationHandler(), new CookieManager(new CustomCookieStore(), CookiePolicy.ACCEPT_ALL), false);
			}
		}
		return client;
	}
	
	public RemoteServer getRemote() {
		if (remote == null) {
			synchronized(this) {
				try {
					remote = new RemoteServer(getClient(), new URI("http://" + host + ":" + port), null, Charset.forName("UTF-8"));
				}
				catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return remote;
	}
}
