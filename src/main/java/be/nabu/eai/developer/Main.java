package be.nabu.eai.developer;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.managers.util.EnumeratedSimpleProperty;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.Confirm;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.server.ServerConnection;
import be.nabu.libs.authentication.api.Token;
import be.nabu.libs.authentication.impl.BasicPrincipalImpl;
import be.nabu.libs.http.HTTPException;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.utils.security.EncryptionXmlAdapter;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Main extends Application {

	private MainController controller;
	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String...args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		setUserAgentStylesheet(STYLESHEET_MODENA);
		
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Thread.currentThread().getContextClassLoader().getResource("developer.fxml"));
		loader.load();
		
		controller = loader.getController();
		controller.setStage(stage);
		Parent root = loader.getRoot();
		Scene scene = new Scene(root);
		
		scene.addEventHandler(KeyEvent.KEY_PRESSED,
			new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.isAltDown()) {
						event.consume();
					}
				}
			}
		);
	
		stage.initStyle(StageStyle.DECORATED);
		scene.getStylesheets().add(Thread.currentThread().getContextClassLoader().getResource("style.css").toExternalForm());
		stage.setScene(scene);
		stage.setTitle("Nabu Developer");
		stage.setMaximized(true);
		stage.setResizable(true);
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("icon.png");
		try {
			stage.getIcons().add(new Image(stream));
		}
		finally {
			stream.close();
		}
		stage.show();
		
		draw(controller);
	}
	
	@Override
	public void stop() throws Exception {
		controller.close();
		super.stop();
	}

	@XmlRootElement
	public static class Developer {
		private List<ServerProfile> profiles;
		private String lastProfile;

		public List<ServerProfile> getProfiles() {
			return profiles;
		}

		public void setProfiles(List<ServerProfile> profiles) {
			this.profiles = profiles;
		}

		public String getLastProfile() {
			return lastProfile;
		}

		public void setLastProfile(String lastProfile) {
			this.lastProfile = lastProfile;
		}
	}
	public enum Protocol {
		HTTP, SSH
	}
	public static class ServerTunnel {
		private String id;
		private Integer localPort;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public Integer getLocalPort() {
			return localPort;
		}
		public void setLocalPort(Integer localPort) {
			this.localPort = localPort;
		}
	}
	public static class ServerProfile {
		private Protocol protocol;
		private boolean secure;
		private String ip, sshIp, username, sshUsername, name, sshKey, password, sshPassword;
		private Integer port, sshPort;
		private List<ServerTunnel> tunnels;
		public List<ServerTunnel> getTunnels() {
			return tunnels;
		}
		public void setTunnels(List<ServerTunnel> tunnels) {
			this.tunnels = tunnels;
		}
		public Protocol getProtocol() {
			return protocol;
		}
		public void setProtocol(Protocol protocol) {
			this.protocol = protocol;
		}
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public String getSshIp() {
			return sshIp;
		}
		public void setSshIp(String sshIp) {
			this.sshIp = sshIp;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public Integer getPort() {
			return port;
		}
		public void setPort(Integer port) {
			this.port = port;
		}
		public Integer getSshPort() {
			return sshPort;
		}
		public void setSshPort(Integer sshPort) {
			this.sshPort = sshPort;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getSshUsername() {
			return sshUsername;
		}
		public void setSshUsername(String sshUsername) {
			this.sshUsername = sshUsername;
		}
		public String getSshKey() {
			return sshKey;
		}
		public void setSshKey(String sshKey) {
			this.sshKey = sshKey;
		}
		@XmlJavaTypeAdapter(value=EncryptionXmlAdapter.class)
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		@XmlJavaTypeAdapter(value=EncryptionXmlAdapter.class)
		public String getSshPassword() {
			return sshPassword;
		}
		public void setSshPassword(String sshPassword) {
			this.sshPassword = sshPassword;
		}
		public boolean isSecure() {
			return secure;
		}
		public void setSecure(boolean secure) {
			this.secure = secure;
		}
	}
	
	private static ServerProfile getProfileByName(String name, List<ServerProfile> profiles) {
		for (ServerProfile profile : profiles) {
			if (name.equals(profile.getName())) {
				return profile;
			}
		}
		return null;
	}
	
	public static void draw(MainController controller) {
		Developer configuration = MainController.getConfiguration();
		
		EnumeratedSimpleProperty<String> profilesProperty = new EnumeratedSimpleProperty<String>("Profile", String.class, true);
		ServerProfile lastProfile = null;
		// if we have profiles, prompt you to select one
		if (configuration != null && configuration.getProfiles() != null && !configuration.getProfiles().isEmpty()) {
			Collections.sort(configuration.getProfiles(), new Comparator<ServerProfile>() {
				@Override
				public int compare(ServerProfile o1, ServerProfile o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (ServerProfile profile : configuration.getProfiles()) {
				profilesProperty.addAll(profile.getName());
			}
			if (configuration.getLastProfile() != null) {
				lastProfile = getProfileByName(configuration.getLastProfile(), configuration.getProfiles());
			}
			if (lastProfile == null) {
				lastProfile = configuration.getProfiles().get(0);
			}
		}
		
		VBox box = new VBox();
		box.setPadding(new Insets(10));
		
		VBox popup = new VBox();
		popup.setPadding(new Insets(10));
		
		HBox buttons = new HBox();
		buttons.setPadding(new Insets(10));
		buttons.setAlignment(Pos.CENTER);
		
		box.getChildren().addAll(popup, buttons);
		
		StringProperty selectedProfile = new SimpleStringProperty();
		Button createProfile = new Button("New Profile");
		selectedProfile.addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				if (arg2 == null) {
					createProfile.setText("New Profile");
				}
				else {
					createProfile.setText("Edit Profile");
				}
			}
		});
		
		Button connect = new Button("Connect");
		buttons.getChildren().addAll(createProfile, connect);
		
		selectedProfile.set(lastProfile == null ? null : lastProfile.getName());
		
		final SimplePropertyUpdater profileUpdater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(profilesProperty)),
				new ValueImpl<String>(profilesProperty, selectedProfile.get())) {
			@Override
			public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
				selectedProfile.set(value == null ? null : value.toString());
				return super.updateProperty(property, value);
			}
		};
		controller.showProperties(profileUpdater, popup, true);
		
		createProfile.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				String profileName = selectedProfile.get();
				ServerProfile profile = profileName == null ? new ServerProfile() : getProfileByName(profileName, configuration.getProfiles());
				
				SimpleProperty<String> profileNameProperty = new SimpleProperty<String>("Profile Name", String.class, true);
				SimplePropertyUpdater profileNameUpdater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(profileNameProperty)),
					new ValueImpl<String>(profileNameProperty, profileName));
				
				EAIDeveloperUtils.buildPopup(controller, profileNameUpdater, "Profile Name", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						String newName = profileNameUpdater.getValue("Profile Name");
						profile.setName(newName);
						if (configuration.getProfiles() == null) {
							configuration.setProfiles(new ArrayList<ServerProfile>());
						}
						if (!configuration.getProfiles().contains(profile)) {
							configuration.getProfiles().add(profile);
						}
						File sshKeyFile = profile.getSshKey() == null ? null : new File(profile.getSshKey());
						
						VBox box = new VBox();
						box.setPadding(new Insets(10));
						
						VBox sshBox = new VBox();
						sshBox.setPadding(new Insets(10));
						SimpleProperty<String> sshServerProperty = new SimpleProperty<String>("SSH Server", String.class, false);
						SimpleProperty<Integer> sshPortProperty = new SimpleProperty<Integer>("SSH Port", Integer.class, false);
						SimpleProperty<String> sshUserProperty = new SimpleProperty<String>("SSH Username", String.class, false);
						SimpleProperty<File> sshFileProperty = new SimpleProperty<File>("SSH Key File", File.class, false);
						sshFileProperty.setInput(true);
						SimpleProperty<String> sshPasswordProperty = new SimpleProperty<String>("SSH Password", String.class, false);
						sshPasswordProperty.setPassword(true);
						SimplePropertyUpdater sshUpdater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(sshServerProperty, sshPortProperty, sshUserProperty, sshFileProperty, sshPasswordProperty)),
								new ValueImpl<String>(sshServerProperty, profile == null ? null : profile.getSshIp()),
								new ValueImpl<Integer>(sshPortProperty, profile == null ? null : profile.getSshPort()),
								new ValueImpl<String>(sshUserProperty, profile == null ? null : profile.getSshUsername()),
								new ValueImpl<String>(sshPasswordProperty, profile == null ? null : profile.getSshPassword()),
								new ValueImpl<File>(sshFileProperty, sshKeyFile != null && sshKeyFile.exists() ? sshKeyFile : null));
						MainController.getInstance().showProperties(sshUpdater, sshBox, false);
						
						// set initial visibility correctly
						sshBox.setVisible(profile != null && Protocol.SSH.equals(profile.getProtocol()));
						
						VBox protocolBox = new VBox();
						protocolBox.setPadding(new Insets(10));
						EnumeratedSimpleProperty<Protocol> protocol = new EnumeratedSimpleProperty<Protocol>("Protocol", Protocol.class, false);
						protocol.addAll(Protocol.HTTP, Protocol.SSH);
						SimplePropertyUpdater protocolUpdater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(protocol)),
								new ValueImpl<Protocol>(protocol, profile == null || profile.getProtocol() == null ? Protocol.HTTP : profile.getProtocol())) {
							public java.util.List<ValidationMessage> updateProperty(be.nabu.libs.property.api.Property<?> property, Object value) {
								sshBox.setVisible(value != null && value.equals(Protocol.SSH));
								return super.updateProperty(property, value);
							};
						};
						MainController.getInstance().showProperties(protocolUpdater, protocolBox, false);
						
						SimpleProperty<String> serverProperty = new SimpleProperty<String>("Server", String.class, true);
						SimpleProperty<Integer> portProperty = new SimpleProperty<Integer>("Port", Integer.class, false);
						SimpleProperty<String> usernameProperty = new SimpleProperty<String>("Username", String.class, false);
						SimpleProperty<String> passwordProperty = new SimpleProperty<String>("Password", String.class, false);
						SimpleProperty<Boolean> secureProperty = new SimpleProperty<Boolean>("Secure", Boolean.class, false);
						passwordProperty.setPassword(true);
						final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(serverProperty, portProperty, usernameProperty, passwordProperty, secureProperty)), 
							new ValueImpl<String>(serverProperty, profile.getIp()),
							new ValueImpl<Integer>(portProperty, profile.getPort()),
							new ValueImpl<String>(usernameProperty, profile.getUsername()),
							new ValueImpl<String>(passwordProperty, profile.getPassword()),
							new ValueImpl<Boolean>(secureProperty, profile.isSecure())
						);
						VBox propertiesBox = new VBox();
						propertiesBox.setPadding(new Insets(10));
						MainController.getInstance().showProperties(updater, propertiesBox, false);
						
						HBox buttons = new HBox();
						buttons.setPadding(new Insets(10));
						buttons.setAlignment(Pos.CENTER);
						
						Button save = new Button("Save");
						
						Button cancel = new Button("Cancel");
						
						buttons.getChildren().addAll(cancel, save);
						box.getChildren().addAll(propertiesBox, protocolBox, sshBox, buttons);
						
						Stage stage = EAIDeveloperUtils.buildPopup("Profile", box);
						
						cancel.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								stage.close();
							}
						});
						
						save.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								Protocol protocol = protocolUpdater.getValue("Protocol");
								if (!Protocol.SSH.equals(protocol)) {
									profile.setSshIp(null);
									profile.setSshKey(null);
									profile.setSshPassword(null);
									profile.setSshPort(null);
									profile.setSshUsername(null);
									profile.setProtocol(Protocol.HTTP);
								}
								else {
									profile.setSshIp(sshUpdater.getValue("SSH Server"));
									profile.setSshPassword(sshUpdater.getValue("SSH Password"));
									profile.setSshPort(sshUpdater.getValue("SSH Port"));
									profile.setSshUsername(sshUpdater.getValue("SSH Username"));
									profile.setProtocol(Protocol.SSH);
									
									File sshKeyFile = sshUpdater.getValue("SSH Key File");
									profile.setSshKey(sshKeyFile != null && sshKeyFile.exists() ? sshKeyFile.getAbsolutePath() : null);
								}
								
								profile.setIp(updater.getValue("Server"));
								profile.setPort(updater.getValue("Port"));
								profile.setUsername(updater.getValue("Username"));
								profile.setPassword(updater.getValue("Password"));
								Boolean value = updater.getValue("Secure");
								profile.setSecure(value != null && value);
								
								MainController.saveConfiguration();
								
								// add the new name
								if (!profilesProperty.getEnumerations().contains(newName)) {
									profilesProperty.addAll(newName);
								}
								// remove the old name if you changed it
								if (profileName != null && !profileName.equals(newName)) {
									profilesProperty.getEnumerations().remove(profileName);
								}
								stage.close();
								// reshow the selector so we see the new profile
								controller.showProperties(profileUpdater, popup, true);
							}
						});
					}
				});
				
			}
		});
		
		Stage stage = EAIDeveloperUtils.buildPopup("Connect", box);
		
		connect.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					String profileName = selectedProfile.get();
					ServerProfile profile = profileName == null ? null : getProfileByName(profileName, configuration.getProfiles());
					
					if (profile != null) {
						Token principal = new BasicPrincipalImpl(
							profile.getUsername() == null ? InetAddress.getLocalHost().getHostName() : profile.getUsername(),
							profile.getPassword());
						stage.close();
						
						if ("$self".equals(principal.getName())) {
							throw new IllegalStateException("Can not connect with username $self");
						}
						
						if (Protocol.SSH.equals(profile.getProtocol())) {
							// take a random high port so you can mostly run multiple developers at the same time without conflict
							int localPort = 20000 + new Random().nextInt(10000);
							String remoteHost = profile.getIp();
							// if the host is filled in but the ssh host is not, we assume you want to connect to a server on that ssh server
							if (remoteHost == null || profile.getSshIp() == null) {
								remoteHost = "localhost";
							}
							int remotePort = profile.getPort() == null ? 5555 : profile.getPort();
							Session session = openTunnel(controller, profile, remoteHost, remotePort, localPort);
							controller.setReconnector(new Reconnector(session, controller, profile, remoteHost, remotePort, localPort));
							controller.connect(profile, new ServerConnection(null, principal, "localhost", localPort, profile.isSecure()));
						}
						else {
							controller.connect(profile, new ServerConnection(null, principal, profile.getIp(), profile.getPort(), profile.isSecure()));
						}
						configuration.setLastProfile(profile.getName());
						MainController.saveConfiguration();
					}
				}
				catch (HTTPException e) {
					if (e.getCode() == 401) {
						Confirm.confirm(ConfirmType.WARNING, "Authentication Required", "You need to authenticate to connect to this server", new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								draw(controller);
							}
						});
					}
					else {
						throw new RuntimeException(e);
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	public static Session openTunnel(MainController controller, ServerProfile profile, String remoteHost, int remotePort, int localPort) {
		try {
			JSch jsch = new JSch();
			if (profile.getSshKey() != null) {
				jsch.addIdentity(profile.getSshKey());
			}
			Session session = jsch.getSession(
					profile.getSshUsername(), 
					profile.getSshIp() == null ? profile.getIp() : profile.getSshIp(), 
					profile.getSshPort() == null ? 22 : profile.getSshPort());
			
			if (profile.getSshPassword() != null) {
				session.setPassword(profile.getSshPassword());
			}
	        session.setConfig("StrictHostKeyChecking", "no");

	        logger.info("Creating ssh connection: " + profile.getSshUsername() + "@" + (profile.getSshIp() == null ? profile.getIp() : profile.getSshIp()) + ":" + (profile.getSshPort() == null ? 22 : profile.getSshPort()));
			session.connect();

			// set to 5 minutes, the connection was dropping when unused for a while
			session.setServerAliveInterval(300000);
			
			logger.info("Creating ssh tunnel from port " + localPort + " to " + remoteHost + ":" + remotePort);
			int assignedPort = session.setPortForwardingL(localPort, remoteHost, remotePort);
			if (assignedPort != localPort) {
				logger.warn("Tunnel created on different local port: " + assignedPort);
			}
			controller.getStage().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					if (session.isConnected()) {
						session.disconnect();
					}
				}
			});
			return session;
		}
		catch (Exception e) {
			logger.error("Could not set up ssh tunnel", e);
			return null;
		}
	}
	
	public static class Reconnector {
		private Session session;
		private ServerProfile profile;
		private String remoteHost;
		private int remotePort;
		private int localPort;
		private MainController controller;
		public Reconnector(Session session, MainController controller, ServerProfile profile, String remoteHost, int remotePort, int localPort) {
			this.session = session;
			this.controller = controller;
			this.profile = profile;
			this.remoteHost = remoteHost;
			this.remotePort = remotePort;
			this.localPort = localPort;
		}
		public void reconnect() {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
			session = openTunnel(controller, profile, remoteHost, remotePort, localPort);
		}
	}
}
