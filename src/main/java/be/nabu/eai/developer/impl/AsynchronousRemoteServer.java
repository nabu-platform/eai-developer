/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
	
	public void reload(String id) {
		reload(id, null);
	}
	
	@SuppressWarnings("rawtypes")
	public void reload(String id, Boolean recursive) {
		this.pool.submit(new Action("Reload: " + id, new Callable() {
			@Override
			public Object call() throws Exception {
				server.reload(id, recursive);
				return null;
			}
		}));
	}
	
	@SuppressWarnings("rawtypes")
	public void refresh(String id, Boolean recursive) {
		this.pool.submit(new Action("Refresh: " + id, new Callable() {
			@Override
			public Object call() throws Exception {
				server.refresh(id, recursive);
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
