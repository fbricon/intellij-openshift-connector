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
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public class LinkComponentAction extends OdoAction {

  public LinkComponentAction() {
    super(ComponentNode.class);
  }

  @Override
  protected String getTelemetryActionName() { return "link component to component"; }
  
  @Override
  public boolean isVisible(Object selected) {
    boolean visible = super.isVisible(selected);
    return visible;
  }

  protected String getSelectedTargetComponent(Odo odo, String project, String component) throws IOException {
    String targetComponent = null;

    List<Component> components = odo.getComponents(project)
            .stream().filter(comp -> !comp.getName().equals(component)).collect(Collectors.toList());
    if (!components.isEmpty()) {
      if (components.size() == 1) {
        targetComponent = components.get(0).getName();
      } else {
        String[] componentsArray = components.stream().map(Component::getName).toArray(String[]::new);
        targetComponent = UIHelper.executeInUI(() -> Messages.showEditableChooseDialog("Select component", "Link component", Messages.getQuestionIcon(), componentsArray, componentsArray[0], null));
      }
    }
    return targetComponent;
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    Component sourceComponent = componentNode.getComponent();
    NamespaceNode namespaceNode = componentNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        String targetComponent = getSelectedTargetComponent(odo, namespaceNode.getName(), sourceComponent.getName());
          if (targetComponent != null) {
              Notification notification = new Notification(Constants.GROUP_DISPLAY_ID, "Link component", "Linking component to " + targetComponent,
                      NotificationType.INFORMATION);
              Notifications.Bus.notify(notification);
              odo.link(namespaceNode.getName(), sourceComponent.getPath(), sourceComponent.getName(), targetComponent);
              notification.expire();
              Notifications.Bus.notify(new Notification(Constants.GROUP_DISPLAY_ID, "Link component", "Component linked to " + targetComponent,
                      NotificationType.INFORMATION));
              sendTelemetryResults(TelemetryResult.SUCCESS);
          } else {
            String message = "No components to link to";
            sendTelemetryError(message);
            UIHelper.executeInUI(() -> Messages.showWarningDialog(message, "Link component"));
          }
      } catch (IOException e) {
        sendTelemetryError(e);
        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Link component"));
      }
    });
  }
}
