/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import org.jboss.tools.intellij.openshift.utils.odo.Component;

public class ComponentNode extends ParentableNode<NamespaceNode> {
  private final Component component;

  public ComponentNode(NamespaceNode parent, Component component) {
    super(parent.getRoot(), parent, component.getName());
    this.component = component;
  }

  public Component getComponent() {
    return component;
  }
}
