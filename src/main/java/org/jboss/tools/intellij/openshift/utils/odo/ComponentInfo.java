package org.jboss.tools.intellij.openshift.utils.odo;

public interface ComponentInfo {
    ComponentSourceType getSourceType();
    String getComponentTypeName();
    String getComponentTypeVersion();
    String getRepositoryURL();
    String getRepositoryReference();
    String getBinaryURL();

    public class Builder {
        private ComponentSourceType sourceType;
        private String componentTypeName;
        private String componentTypeVersion;
        private String repositoryURL;
        private String repositoryReference;
        private String binaryURL;

        public Builder withSourceType(ComponentSourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public Builder withComponentTypeName(String componentTypeName) {
            this.componentTypeName= componentTypeName;
            return this;
        }

        public Builder withComponentTypeVersion(String componentTypeVersion) {
            this.componentTypeVersion = componentTypeVersion;
            return this;
        }

        public Builder withRepositoryURL(String repositoryURL) {
            this.repositoryURL = repositoryURL;
            return this;
        }

        public Builder withRepositoryReference(String repositoryReference) {
            this.repositoryReference = repositoryReference;
            return this;
        }

        public Builder withBinaryURL(String binaryURL) {
            this.binaryURL = binaryURL;
            return this;
        }

        public ComponentInfo build() {
            return new ComponentInfo() {
                @Override
                public ComponentSourceType getSourceType() {
                    return sourceType;
                }

                @Override
                public String getComponentTypeName() {
                    return componentTypeName;
                }

                @Override
                public String getComponentTypeVersion() {
                    return componentTypeVersion;
                }

                @Override
                public String getRepositoryURL() {
                    return repositoryURL;
                }

                @Override
                public String getRepositoryReference() {
                    return repositoryReference;
                }

                @Override
                public String getBinaryURL() {
                    return binaryURL;
                }
            };
        }
    }
}
