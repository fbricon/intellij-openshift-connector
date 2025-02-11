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

public interface ComponentInfo {

    String getComponentTypeName();

    String getLanguage();

    boolean isMigrated();

    ComponentKind getComponentKind();

    ComponentFeatures getFeatures();

    class Builder {
        private String componentTypeName;
        private boolean migrated;

        private ComponentKind kind;

        private ComponentFeatures features = new ComponentFeatures();

        private String language;

        public Builder withComponentTypeName(String componentTypeName) {
            this.componentTypeName = componentTypeName;
            return this;
        }

        public Builder withMigrated(boolean migrated) {
            this.migrated = migrated;
            return this;
        }

        public Builder withComponentKind(ComponentKind kind) {
            this.kind = kind;
            return this;
        }

        public Builder withFeatures(ComponentFeatures features) {
            this.features = features;
            return this;
        }

        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }

        public ComponentInfo build() {
            return new ComponentInfo() {

                @Override
                public String getComponentTypeName() {
                    return componentTypeName;
                }

                @Override
                public String getLanguage() {
                    return language;
                }

                @Override
                public boolean isMigrated() {
                    return migrated;
                }

                @Override
                public ComponentKind getComponentKind() {
                    return kind;
                }

                @Override
                public ComponentFeatures getFeatures() {
                    return features;
                }
            };
        }
    }
}
