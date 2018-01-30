package be.nabu.eai.developer.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.eai.server.CollaborationListener.User;
import be.nabu.eai.server.ServerConnection;
import be.nabu.eai.server.CollaborationListener.CollaborationMessage;
import be.nabu.eai.server.CollaborationListener.CollaborationMessageType;
import be.nabu.eai.server.CollaborationListener.UserList;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.events.api.EventHandler;
import be.nabu.libs.http.api.HTTPResponse;
import be.nabu.libs.http.api.client.HTTPClient;
import be.nabu.libs.http.client.nio.NIOHTTPClientImpl;
import be.nabu.libs.http.server.nio.MemoryMessageDataProvider;
import be.nabu.libs.http.server.websockets.WebSocketUtils;
import be.nabu.libs.http.server.websockets.api.WebSocketMessage;
import be.nabu.libs.http.server.websockets.api.WebSocketRequest;
import be.nabu.libs.http.server.websockets.impl.WebSocketRequestParserFactory;
import be.nabu.libs.nio.api.StandardizedMessagePipeline;
import be.nabu.libs.nio.api.events.ConnectionEvent;
import be.nabu.libs.nio.api.events.ConnectionEvent.ConnectionState;
import be.nabu.libs.resources.api.features.CacheableResource;

public class CollaborationClient {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void start() {
		HTTPClient client = MainController.getInstance().getServer().getClient();
		if (client instanceof NIOHTTPClientImpl) {
			// unlimited lifetime
			((NIOHTTPClientImpl) client).getNIOClient().setMaxLifeTime(0l);
			// register websocket upgrader
			WebSocketUtils.allowWebsockets((NIOHTTPClientImpl) client, new MemoryMessageDataProvider());
			
			((NIOHTTPClientImpl) client).getDispatcher().subscribe(ConnectionEvent.class, new EventHandler<ConnectionEvent, Void>() {
				@Override
				public Void handle(ConnectionEvent event) {
					WebSocketRequestParserFactory parserFactory = WebSocketUtils.getParserFactory(event.getPipeline());
					if (parserFactory != null) {
						if (ConnectionState.CLOSED.equals(event.getState())) {
							logger.warn("Collaboration connection closed, reconnecting...");
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									MainController.getInstance().connectedProperty().set(false);
								}
							});
							retryConnect();
						}
						else if (ConnectionState.UPGRADED.equals(event.getState())) {
							logger.info("Collaboration connection set up");
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									MainController.getInstance().connectedProperty().set(true);
								}
							});
						}
					}
					return null;
				}
			});
			((NIOHTTPClientImpl) client).getDispatcher().subscribe(WebSocketRequest.class, new EventHandler<WebSocketRequest, WebSocketMessage>() {
				@Override
				public WebSocketMessage handle(WebSocketRequest event) {
					CollaborationMessage message = unmarshal(event.getData(), CollaborationMessage.class);
					logger.info("Received: " + message.getType());
					switch(message.getType()) {
						case USERS:
							final UserList unmarshal = unmarshal(new ByteArrayInputStream(message.getContent().getBytes(Charset.forName("UTF-8"))), UserList.class);
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									MainController.getInstance().getUsers().clear();
									if (unmarshal != null && unmarshal.getUsers() != null) {
										MainController.getInstance().getUsers().addAll(unmarshal.getUsers());
									}
								}
							});
						break;
						case CREATE:
						case DELETE:
							log(message);
							// refresh the parent
							refresh(message.getId().replaceAll("\\.[^.]+$", ""));
						break;
						case UPDATE:
							log(message);
							refresh(message.getId());
						break;
						case LOCK:
							log(message);
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									MainController.getInstance().lock(message.getId()).set(message.getAlias());
								}
							});
						break;
						case UNLOCK:
							log(message);
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									MainController.getInstance().lock(message.getId()).set(null);
								}
							});
						break;
						case LEAVE:
							log(message);
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									User toRemove = null;
									for (User user : MainController.getInstance().getUsers()) {
										if (user.getAlias().equals(message.getAlias())) {
											toRemove = user;
											break;
										}
									}
									if (toRemove != null) {
										MainController.getInstance().getUsers().remove(toRemove);
									}
								}
							});
						break;
						case JOIN:
							log(message);
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									User user = new User();
									user.setAlias(message.getAlias());
									MainController.getInstance().getUsers().add(user);
								}
							});
						break;
					}
					return null;
				}

			});
			
			connect();
			
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					while(!Thread.interrupted()) {
						try {
							Thread.sleep(60000);
						}
						catch (InterruptedException e) {
							// do nothing
						}
						send(new CollaborationMessage(CollaborationMessageType.PING));
					}
				}
			});
			thread.setDaemon(true);
			thread.start();
		}
	}
	
	private void log(CollaborationMessage message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, HH:mm:ss");
				MainController.getInstance().logText(formatter.format(new Date()) + " - " + message.getAlias() + " [" + message.getType() + "] " + (message.getAlias() == null ? "" : "(" + message.getAlias() + ") ") + (message.getContent() == null ? "" : message.getContent()));
			}
		});
	}

	private void refresh(String id) {
		EAIResourceRepository repository = MainController.getInstance().getRepository();
		Entry entry = repository.getEntry(id);
		if (entry instanceof RepositoryEntry) {
			((RepositoryEntry) entry).refresh(true, true);
		}
		// reload the filesystem to see the changes
		else if (entry instanceof ResourceEntry) {
			if (((ResourceEntry) entry).getContainer() instanceof CacheableResource) {
				try {
					((CacheableResource) ((ResourceEntry) entry).getContainer()).resetCache();
				}
				catch (IOException e) {
					logger.warn("Can not refresh: " + id, e);
				}
			}
		}
		// reload the repository
		repository.reload(id);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// reload the tree
				TreeItem<Entry> resolved = MainController.getInstance().getTree().resolve(id.replace(".", "/"));
				MainController.getInstance().getTree().getTreeCell(resolved).refresh();
				MainController.getInstance().refresh(id);
			}
		});
	}
	
	public void created(String id, String message) {
		send(new CollaborationMessage(CollaborationMessageType.CREATE, message, id));
	}
	public void updated(String id, String message) {
		send(new CollaborationMessage(CollaborationMessageType.UPDATE, message, id));
	}
	public void deleted(String id, String message) {
		send(new CollaborationMessage(CollaborationMessageType.DELETE, message, id));
	}
	public void lock(String id, String message) {
		send(new CollaborationMessage(CollaborationMessageType.LOCK, message, id));
	}
	public void unlock(String id, String message) {
		send(new CollaborationMessage(CollaborationMessageType.UNLOCK, message, id));
	}
	
	public static <T> byte [] marshal(T content) {
		try {
			Marshaller marshaller = JAXBContext.newInstance(content.getClass()).createMarshaller();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			marshaller.marshal(content, output);
			return output.toByteArray();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T unmarshal(InputStream input, Class<T> clazz) {
		try {
			Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller();
			return (T) unmarshaller.unmarshal(input);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void connect() {
		try {
			MainController controller = MainController.getInstance();
			ServerConnection server = controller.getServer();
			
			HTTPResponse upgrade = WebSocketUtils.upgrade(
				server.getClient(), 
				server.getContext(), 
				server.getHost(), 
				server.getPort(), 
				"/collaborate", 
				(Token) server.getPrincipal(), 
				new MemoryMessageDataProvider(), 
				((NIOHTTPClientImpl) server.getClient()).getDispatcher(), 
				new ArrayList<String>());

			if (upgrade.getCode() >= 100 && upgrade.getCode() < 300) {
				logger.info("Sending HELLO");
				send(new CollaborationMessage(CollaborationMessageType.HELLO));
			}
			else {
				logger.warn("Websockets not available: " + upgrade.getCode());
			}
		}
		catch (Exception e) {
			logger.warn("Could not connect websocket, retrying...", e);
			retryConnect();
		}
	}

	private void send(CollaborationMessage message) {
		List<StandardizedMessagePipeline<WebSocketRequest, WebSocketMessage>> pipelines = WebSocketUtils.getWebsocketPipelines(((NIOHTTPClientImpl) MainController.getInstance().getServer().getClient()).getNIOClient(), "/collaborate");
		if (pipelines != null && pipelines.size() > 0) {
			WebSocketMessage webSocketMessage = WebSocketUtils.newMessage(marshal(message));
			pipelines.get(0).getResponseQueue().add(webSocketMessage);
		}
	}
	
	private void retryConnect() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				}
				catch (InterruptedException e) {
					// do nothing
				}
				connect();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
