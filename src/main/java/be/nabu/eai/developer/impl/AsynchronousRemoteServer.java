package be.nabu.eai.developer.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import javafx.application.Platform;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.server.RemoteServer;

public class AsynchronousRemoteServer {
	
	private RemoteServer server;
	private ForkJoinPool pool;
	
	public AsynchronousRemoteServer(RemoteServer server) {
		this.server = server;
		this.pool = new ForkJoinPool(1);
	}
	
	@SuppressWarnings("rawtypes")
	public void reload(String id) {
		this.pool.submit(new Action("Reload: " + id, new Callable() {
			@Override
			public Object call() throws Exception {
				server.reload(id);
				return null;
			}
		}));
	}
	
	@SuppressWarnings("rawtypes")
	public void reloadAll() {
		this.pool.submit(new Action("Reloading everything", new Callable() {
			@Override
			public Object call() throws Exception {
				server.reloadAll();
				return null;
			}
		}));
	}
	
	@SuppressWarnings("rawtypes")
	public void unload(String id) {
		this.pool.submit(new Action("Unload: " + id, new Callable() {
			@Override
			public Object call() throws Exception {
				server.unload(id);
				return null;
			}
		}));
	}
	
	public static class Action implements Runnable {
		
		private String message;
		private Callable<?> action;

		public Action(String message, Callable<?> action) {
			this.message = message;
			this.action = action;
		}
		
		@Override
		public void run() {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					MainController.getInstance().remoteServerMessageProperty().set(message);
				}
			});
			try {
				action.call();
			}
			catch (Exception e){
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						MainController.getInstance().notify(e);
					}
				});
			}
			finally {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						MainController.getInstance().remoteServerMessageProperty().set(null);
					}
				});
			}
		}
		
	}

	public ForkJoinPool getPool() {
		return pool;
	}
	
}
