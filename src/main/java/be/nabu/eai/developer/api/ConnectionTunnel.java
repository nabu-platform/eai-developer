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

package be.nabu.eai.developer.api;

public interface ConnectionTunnel {
	public ConnectionTarget getTarget();
	public String getRemoteHost();
	public String getLocalHost();
	public int getRemotePort();
	public int getLocalPort();
	// can be used to set up initial connection or reconnect
	public void connect();
	public void disconnect();
	public boolean isConnected();
}
