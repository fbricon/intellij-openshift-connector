/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.URLNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;
import org.jboss.tools.intellij.openshift.utils.odo.URL;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpenInBrowserAction extends OdoAction {
  public OpenInBrowserAction() {
    super(ComponentNode.class, URLNode.class);
  }

  @Override
  public boolean isVisible(Object selected) {
    boolean visible = super.isVisible(selected);
    if (visible && selected instanceof ComponentNode) {
      Component component = (Component) ((ComponentNode)selected).getUserObject();
      visible = component.getState() == ComponentState.PUSHED;
    }
    return visible;
  }

  private void openURL(List<URL> urls) {
    String url = getURL(urls.get(0));
    BrowserUtil.open(url);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    if(selected instanceof ComponentNode) {
      openFromComponent((ComponentNode) selected, odo);
    } else {
      BrowserUtil.open(getURL(((URL)((URLNode)selected).getUserObject())));
    }
  }

  private void openFromComponent(ComponentNode componentNode, Odo odo) {
    Component component = (Component) componentNode.getUserObject();
    LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) componentNode.getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        List<URL> urls = odo.listURLs(projectNode.toString(), applicationNode.toString(), component.getPath(), component.getName());
        if (urls.isEmpty()) {
          if (UIHelper.executeInUI(() -> JOptionPane.showConfirmDialog(null, "No URL for component " + componentNode.toString() + ", do you want to create one ?", "Create URL", JOptionPane.OK_CANCEL_OPTION)) == JOptionPane.OK_OPTION) {
            final OpenShiftClient client = ((ApplicationsRootNode)componentNode.getRoot()).getClient();
            if (CreateURLAction.createURL(odo, client, projectNode.toString(), applicationNode.toString(), component.getPath(), component.getName())) {
              urls = odo.listURLs(projectNode.toString(), applicationNode.toString(), component.getPath(), component.getName());
              openURL(urls);
            }
          }
        } else {
          openURL(urls);
        }
      } catch (KubernetesClientException | IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Open in Brower", JOptionPane.ERROR_MESSAGE));
      }
    });
  }


  protected String getURL(URL url) {
    return url.getProtocol() + "://" + url.getHost();
  }
}
