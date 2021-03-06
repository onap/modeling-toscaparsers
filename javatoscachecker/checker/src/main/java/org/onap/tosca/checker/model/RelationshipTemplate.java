/*
 * Copyright (c) 2017 <AT&T>.  All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed 
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */
package org.onap.tosca.checker.model;

import java.util.List;

/**
 */
public interface RelationshipTemplate extends TOSCAObject<RelationshipTemplate> {

	public String name();

	public String type();
	
	public String description();
	
	public String copy();

	public default Metadata metadata() {
		return (Metadata)proxy("metadata", Metadata.class);
	}

	public default PropertiesAssignments properties() {
		return (PropertiesAssignments)proxy("properties", PropertiesAssignments.class);
	}
	
	public default AttributesAssignments attributes() {
		return (AttributesAssignments)proxy("attributes", AttributesAssignments.class);
	}
	
	public default TemplateInterfaces interfaces() {
		return (TemplateInterfaces)proxy("interfaces", TemplateInterfaces.class);
	}

}
