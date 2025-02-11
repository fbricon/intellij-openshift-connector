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
package org.jboss.tools.intellij.openshift.utils.odo;

import com.redhat.devtools.intellij.common.utils.ExecHelper;
import org.fest.util.Files;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.jboss.tools.intellij.openshift.Constants.DebugStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class OdoCliComponentTest extends OdoCliTest {
    private ComponentFeature feature;
    private String project;
    private String component;
    private String service;

    public OdoCliComponentTest(ComponentFeature feature) {
        this.feature = feature;
    }

    @Parameterized.Parameters(name = "feature: {0}")
    public static Iterable<? extends Object> data() {
        return Arrays.asList(null, ComponentFeature.DEV);
    }

    @Before
    public void initTestEnv() {
        project = PROJECT_PREFIX + random.nextInt();
        component = COMPONENT_PREFIX + random.nextInt();
        service = SERVICE_PREFIX + random.nextInt();
    }

    @Test
    public void checkCreateComponent() throws IOException, ExecutionException, InterruptedException {
        try {
            createComponent(project, component, feature);
            List<Component> components = odo.getComponents(project);
            assertNotNull(components);
            assertEquals(feature == ComponentFeature.DEV? 1 : 0, components.size());
        } finally {
            odo.deleteProject(project);
        }
    }


    @Test
    public void checkCreateAndDiscoverComponent() throws IOException, ExecutionException, InterruptedException {
        try {
            createComponent(project, component, feature);
            List<ComponentDescriptor> components = odo.discover(COMPONENT_PATH);
            assertNotNull(components);
            assertEquals(1, components.size());
            assertEquals(new File(COMPONENT_PATH).getAbsolutePath(), components.get(0).getPath());
            assertEquals(component, components.get(0).getName());
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateAndDeleteComponent() throws IOException, ExecutionException, InterruptedException {
        try {
            createComponent(project, component, feature);
            odo.deleteComponent(project, COMPONENT_PATH, component, ComponentKind.DEVFILE);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    @Ignore
    public void checkCreateComponentAndLinkService() throws IOException, ExecutionException, InterruptedException {
        Assume.assumeTrue(feature != null);
        try {
            createComponent(project, component, feature);
            ServiceTemplate serviceTemplate = getServiceTemplate();
            OperatorCRD crd = getOperatorCRD(serviceTemplate);
            odo.createService(project, serviceTemplate, crd, service, null, true);
            List<Service> deployedServices = odo.getServices(project);
            assertNotNull(deployedServices);
            assertEquals(1, deployedServices.size());
            Service deployedService = deployedServices.get(0);
            assertNotNull(deployedService);
            odo.link(project, COMPONENT_PATH, component, deployedService.getKind()+"/"+deployedService.getName());
            odo.start(project, COMPONENT_PATH, component, ComponentFeature.DEV, null);
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentAndListURLs() throws IOException, ExecutionException, InterruptedException {
        Assume.assumeTrue(feature != null);
        try {
            createComponent(project, component, feature);
            List<URL> urls = odo.listURLs(project, COMPONENT_PATH, component);
            assertEquals(1, urls.size());
        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    @Ignore
    public void checkCreateComponentAndDebug() throws IOException, ExecutionException, InterruptedException {
        Assume.assumeTrue(feature != null);
        try {
            createComponent(project, component, feature);
            odo.start(project, COMPONENT_PATH, component, ComponentFeature.DEV, null);
            List<URL> urls = odo.listURLs(project, COMPONENT_PATH, component);
            assertEquals(odo.isOpenShift() ? 2 : 1, urls.size());
            int debugPort;
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                debugPort = serverSocket.getLocalPort();
            }
            ExecHelper.submit(() -> {
                try {
                    odo.debug(project, COMPONENT_PATH, component, debugPort);
                    DebugStatus status = odo.debugStatus(project, COMPONENT_PATH, component);
                    assertEquals(DebugStatus.RUNNING, status);
                } catch (IOException e) {
                    fail("Should not raise Exception");
                }
            });

        } finally {
            odo.deleteProject(project);
        }
    }

    @Test
    public void checkCreateComponentStarter() throws IOException, ExecutionException, InterruptedException {
        try {
            createProject(project);
            odo.createComponent(project, "java-springboot", REGISTRY_NAME, component,
                    Files.newTemporaryFolder().getAbsolutePath(), null, "springbootproject");
            List<Component> components = odo.getComponents(project);
            assertNotNull(components);
            assertEquals(0, components.size());
        } finally {
            odo.deleteProject(project);
        }
    }
}
