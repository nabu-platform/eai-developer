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

package be.nabu.eai.developer;

public enum ProjectType {
	// a utility project that is meant to support other projects
	// it can contain frameworks, reusable technical bits
	// the nabu package is a typical utility project
	UTILITY,
	// an integration project
	INTEGRATION,
	// a business application focuses on business logic, branding,...
	// it can contain workflows, data models, api's,...
	// a typical example is a microservice which encapsulates business logic
	BUSINESS,
	// the default type: a (web) application
	APPLICATION,
	// a test project
	TESTING
}
