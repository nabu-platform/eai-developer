package be.nabu.eai.developer.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.CRUDArtifactGUIInstance;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.Notification;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.logger.NabuLogMessage;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.eai.server.CollaborationListener.CollaborationMessage;
import be.nabu.eai.server.CollaborationListener.CollaborationMessageType;
import be.nabu.eai.server.CollaborationListener.User;
import be.nabu.eai.server.CollaborationListener.UserList;
import be.nabu.eai.server.ServerConnection;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.events.api.EventHandler;
import be.nabu.libs.http.api.HTTPResponse;
import be.nabu.libs.http.api.client.HTTPClient;
import be.nabu.libs.http.client.nio.NIOHTTPClientImpl;
import be.nabu.libs.http.server.nio.MemoryMessageDataProvider;
import be.nabu.libs.http.server.websockets.WebAuthorizationType;
import be.nabu.libs.http.server.websockets.WebSocketUtils;
import be.nabu.libs.http.server.websockets.api.WebSocketMessage;
import be.nabu.libs.http.server.websockets.api.WebSocketRequest;
import be.nabu.libs.http.server.websockets.impl.WebSocketRequestParserFactory;
import be.nabu.libs.nio.api.StandardizedMessagePipeline;
import be.nabu.libs.nio.api.events.ConnectionEvent;
import be.nabu.libs.nio.api.events.ConnectionEvent.ConnectionState;
import be.nabu.libs.resources.api.features.CacheableResource;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.xml.XMLBinding;
import be.nabu.libs.types.java.BeanResolver;
import be.nabu.libs.validator.api.ValidationMessage.Severity;
import be.nabu.utils.cep.api.ComplexEvent;
import be.nabu.utils.cep.api.HTTPComplexEvent;
import be.nabu.utils.io.IOUtils;

public class CollaborationClient {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private BooleanProperty connected = new SimpleBooleanProperty(false);
	
	public void start() {
		// asynchronously push it to the main connected property which may trigger javafx changes and has to be done on the gui thread
		connected.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						MainController.getInstance().connectedProperty().set(true);
					}
				});
			}
		});
		// empty out the runnable queue when we reconnect
		MainController.getInstance().connectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if (arg2 != null && arg2) {
					synchronized(queued) {
						while (!queued.isEmpty()) {
							Runnable poll = queued.poll();
							try {
								poll.run();
							}
							catch (Exception e) {
								MainController.getInstance().notify(e);
							}
						}
					}
				}
			}
		});
		
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
					if (parserFactory != null && "/collaborate".equals(parserFactory.getPath())) {
						if (ConnectionState.CLOSED.equals(event.getState())) {
							logger.warn("Collaboration connection closed, reconnecting...");
							connected.set(false);
							retryConnect();
						}
						else if (ConnectionState.UPGRADED.equals(event.getState())) {
							logger.info("Collaboration connection set up");
							connected.set(true);
						}
					}
					return null;
				}
			});
			((NIOHTTPClientImpl) client).getDispatcher().subscribe(WebSocketRequest.class, new EventHandler<WebSocketRequest, WebSocketMessage>() {
				@Override
				public WebSocketMessage handle(WebSocketRequest event) {
					if ("/collaborate".equals(event.getPath())) {
						try {
							CollaborationMessage message = unmarshal(event.getData(), CollaborationMessage.class);
							if (message.getType() == null) {
								logger.warn("Invalid collaboration message from: " + getAlias(message));
								logger.info("Content of collaboration message: " + new String(IOUtils.toBytes(IOUtils.wrap(event.getData()))));
							}
							else {
								String id = message.getId();
								String subPart = null;
								if (id != null && id.contains(":")) {
									subPart = id.substring(id.indexOf(':') + 1);
									id = id.substring(0, id.indexOf(':'));
								}
								String parentId = id == null ? null : id.replaceAll("\\.[^.]+$", "");
								
								switch(message.getType()) {
									case EVENT: 
										ComplexContent unmarshalComplex = unmarshalComplex(message.getContent().getBytes(Charset.forName("UTF-8")), (ComplexType) BeanResolver.getInstance().resolve(HTTPComplexEvent.class));
										HTTPComplexEvent bean = TypeUtils.getAsBean(unmarshalComplex, HTTPComplexEvent.class);
										// TODO: there are multiple types of events, what is the best way to capture them all? just stick to the core?
									break;
									case NOTIFICATION:
										final Notification notification = unmarshal(new ByteArrayInputStream(message.getContent().getBytes(Charset.forName("UTF-8"))), Notification.class);
										Platform.runLater(new Runnable() {
											public void run() {
												MainController.getInstance().logNotification(notification);
											}
										});
									break;
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
										log(message);
										// refresh the parent
										refresh(parentId);
										if (subPart != null) {
											ArtifactGUIInstance instance = MainController.getInstance().getArtifactInstance(id);
											if (instance instanceof CRUDArtifactGUIInstance) {
												((CRUDArtifactGUIInstance) instance).created(subPart, null, message.getContent());
											}
										}
									break;
									case DELETE:
										log(message);
										// refresh the parent
										refresh(parentId);
										if (subPart != null) {
											ArtifactGUIInstance instance = MainController.getInstance().getArtifactInstance(id);
											if (instance instanceof CRUDArtifactGUIInstance) {
												((CRUDArtifactGUIInstance) instance).deleted(subPart, null);
											}
										}
									break;
									case UPDATE:
										log(message);
										refresh(id);
										if (subPart != null) {
											ArtifactGUIInstance instance = MainController.getInstance().getArtifactInstance(id);
											if (instance instanceof CRUDArtifactGUIInstance) {
												((CRUDArtifactGUIInstance) instance).updated(subPart, null, message.getContent());
											}
										}
									break;
									case LOCK:
										log(message);
										Platform.runLater(new Runnable() {
											@Override
											public void run() {
												MainController.getInstance().lock(message.getId()).set(getAlias(message));
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
													if (getAlias(message).equals(user.getAlias())) {
														toRemove = user;
														break;
													}
												}
												if (toRemove != null) {
													MainController.getInstance().getUsers().remove(toRemove);
												}
												MainController.getInstance().showNotification(Severity.INFO, "User Left", getAlias(message) + " has disconnected from the server");
												MainController.getInstance().unlockFor(getAlias(message));
											}
										});
									break;
									case JOIN:
										log(message);
										// inform him of the held locks
										sendLocks();
										Platform.runLater(new Runnable() {
											@Override
											public void run() {
												User user = new User();
												user.setAlias(getAlias(message));
												MainController.getInstance().getUsers().add(user);
												MainController.getInstance().showNotification(Severity.INFO, "User Joined", getAlias(message) + " has connected to the server");
											}
										});
									break;
									case LOG:
										NabuLogMessage log = unmarshal(message.getContent(), NabuLogMessage.class);
										Platform.runLater(new Runnable() {
											@Override
											public void run() {
												MainController.getInstance().logServerText(log);
											}
										});
									break;
									case LOCKS:
										Locks locks = unmarshal(message.getContent(), Locks.class);
										if (locks.getIds() != null) {
											Platform.runLater(new Runnable() {
												@Override
												public void run() {
													boolean hasNotified = false;
													for (String id : locks.getIds()) {
														StringProperty lock = MainController.getInstance().lock(id);
														// if there is no one who holds the lock or he already does, it's ok
														if (lock.get() == null || lock.get().equals(getAlias(message))) {
															lock.set(getAlias(message));
														}
														else {
															MainController.getInstance().logDeveloperText("Lock conflict with " + getAlias(message) + " for: " + id);
															if (!hasNotified) {
																hasNotified = true;
																MainController.getInstance().showNotification(Severity.ERROR, "Lock conflict", "There is at least one lock conflict with user: " + getAlias(message));
															}
														}
													}
												}
											});
										}
									break;
									case REQUEST_LOCK:
										log(message);
										if (MainController.getInstance().hasLock(message.getId()).get()) {
											Platform.runLater(new Runnable() {
												private boolean cancelled;
												private Stage stage;
												@Override
												public void run() {
													new Thread(new Runnable() {
														@Override
														public void run() {
															try {
																Thread.sleep(1000*15);
															}
															catch (Exception e) {
																// do nothing
															}
															if (!cancelled) {
																unlock(message.getId(), "Unlock request timed out");
																if (stage != null) {
																	Platform.runLater(new Runnable() {
																		@Override
																		public void run() {
																			stage.hide();
																		}
																	});
																}
															}
														}
													}).start();
													stage = Confirm.confirm(ConfirmType.QUESTION, "Unlock: " + message.getId(), message.getContent(), new javafx.event.EventHandler<ActionEvent>() {
														@Override
														public void handle(ActionEvent arg0) {
															cancelled = true;
															unlock(message.getId(), "Unlock request granted");
														}
													}, new javafx.event.EventHandler<ActionEvent>() {
														@Override
														public void handle(ActionEvent arg0) {
															cancelled = true;
														}
													});
												}
											});
										}
									break;
								}
							}
						}
						catch (Exception e) {
							logger.error("Error occurred while processing message", e);
							throw new RuntimeException(e);
						}
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
				MainController.getInstance().logDeveloperText(formatter.format(new Date()) + " - " + getAlias(message) + " [" + message.getType() + "] " + (message.getId() == null ? "" : message.getId()));
			}
		});
	}
	
	private String getAlias(CollaborationMessage message) {
		return message.getAlias() == null ? "$anonymous" : message.getAlias();
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
		run(new Runnable() {
			@Override
			public void run() {
				send(new CollaborationMessage(CollaborationMessageType.CREATE, message, id));
			}
		});
	}
	public void updated(String id, String message) {
		run(new Runnable() {
			@Override
			public void run() {
				send(new CollaborationMessage(CollaborationMessageType.UPDATE, message, id));
			}
		});
	}
	public void requestLock(String id) {
		// before we request the lock, we register a listener to automatically take the lock once it becomes available
		StringProperty lock = MainController.getInstance().lock(id);
		final ChangeListener<String> changeListener = new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (arg2 == null) {
					MainController.getInstance().getCollaborationClient().lock(id, "Locked");
					lock.removeListener(this);
				}
			}
		};
		lock.addListener(changeListener);
		// now request the lock
		run(new Runnable() {
			@Override
			public void run() {
				send(new CollaborationMessage(CollaborationMessageType.REQUEST_LOCK, "User '" + (MainController.getInstance().getServer().getPrincipal() == null ? "anonymous" : MainController.getInstance().getServer().getPrincipal().getName()) + "' is requesting the lock", id));
			}
		});
	}
	public void deleted(String id, String message) {
		run(new Runnable() {
			@Override
			public void run() {
				send(new CollaborationMessage(CollaborationMessageType.DELETE, message, id));
			}
		});
	}
	public void lock(String id, String message) {
		run(new Runnable() {
			public void run() {
				send(new CollaborationMessage(CollaborationMessageType.LOCK, message, id));
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						MainController.getInstance().lock(id).set("$self");
					}
				});
			}
		});
	}
	public void unlock(String id, String message) {
		run(new Runnable() {
			@Override
			public void run() {
				StringProperty lock = MainController.getInstance().lock(id);
				if ("$self".equals(lock.get())) {
					send(new CollaborationMessage(CollaborationMessageType.UNLOCK, message, id));
					List<StringProperty> childLocks = new ArrayList<StringProperty>();
					for (String potential : MainController.getInstance().getOwnLocks()) {
						// it is a child lock, if we release the parent, we also release this lock?
						if (potential.startsWith(id + ":")) {
							StringProperty potentialLock = MainController.getInstance().lock(potential);
							if ("$self".equals(potentialLock.get())) {
								send(new CollaborationMessage(CollaborationMessageType.UNLOCK, message, potential));
								childLocks.add(potentialLock);
							}
						}
					}
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							lock.set(null);
							for (StringProperty childLock : childLocks) {
								childLock.set(null);
							}
						}
					});
				}
			}
		});
	}
	
	private Deque<Runnable> queued = new ArrayDeque<Runnable>();
	
	private void run(Runnable run) {
		if (connected.get()) {
			run.run();
		}
		else {
			synchronized(queued) {
				queued.push(run);
			}
		}
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
	public static byte [] marshalComplex(ComplexContent content) {
		try {
			XMLBinding binding = new XMLBinding(content.getType(), Charset.forName("UTF-8"));
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			binding.marshal(output, content);
			return output.toByteArray();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static ComplexContent unmarshalComplex(byte [] bytes, ComplexType type) {
		try {
			XMLBinding binding = new XMLBinding(type, Charset.forName("UTF-8"));
			return binding.unmarshal(new ByteArrayInputStream(bytes), new Window[0]);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static <T> T unmarshal(String input, Class<T> clazz) {
		return unmarshal(new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8"))), clazz);
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
	
	private void sendLocks() {
		Locks locks = new Locks();
		locks.setIds(MainController.getInstance().getOwnLocks());
		if (!locks.getIds().isEmpty()) {
			send(new CollaborationMessage(CollaborationMessageType.LOCKS, marshal(locks)));
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
				new ArrayList<String>(),
				WebAuthorizationType.BASIC);

			if (upgrade.getCode() >= 100 && upgrade.getCode() < 300) {
				logger.info("Sending HELLO");
				send(new CollaborationMessage(CollaborationMessageType.HELLO));
				sendLocks();
			}
			else if (upgrade.getCode() == 503) {
				logger.warn("Server temporarily unavailable, retrying...");
				retryConnect();
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
		if (connected.get()) {
			List<StandardizedMessagePipeline<WebSocketRequest, WebSocketMessage>> pipelines = WebSocketUtils.getWebsocketPipelines(((NIOHTTPClientImpl) MainController.getInstance().getServer().getClient()).getNIOClient(), "/collaborate");
			if (pipelines != null && pipelines.size() > 0) {
				WebSocketMessage webSocketMessage = WebSocketUtils.newMessage(marshal(message));
				pipelines.get(0).getResponseQueue().add(webSocketMessage);
			}
			else {
				logger.warn("Could not send collaboration message because no appropriate websockets were found");
			}
		}
		else {
			logger.warn("Could not send collaboration message because we are disconnected from the server");
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
	
	@XmlRootElement
	public static class Locks {
		private List<String> ids;

		public List<String> getIds() {
			return ids;
		}

		public void setIds(List<String> ids) {
			this.ids = ids;
		}
	}
}
