package org.jenkinsci.plugins.matrixcheckoustrategies.skipparent;

import hudson.Extension;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild.AbstractBuildExecution;
import hudson.model.AbstractProject;

import java.io.IOException;

import jenkins.scm.SCMCheckoutStrategyDescriptor;
import jenkins.scm.SCMCheckoutStrategy;

import org.kohsuke.stapler.DataBoundConstructor;

public class SkipParentStrategy extends SCMCheckoutStrategy {
    
    @DataBoundConstructor
    public SkipParentStrategy() {
        
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void checkout(AbstractBuildExecution execution) throws IOException, InterruptedException {
        if(execution instanceof MatrixBuild.MatrixBuildExecution) {
            execution.getListener().getLogger().println("Skipping SCM checkout on matrix parent");
        } else {
            execution.defaultCheckout();
        }
    }
    
    @Extension
    public static class SkipParentStrategyDescriptor extends SCMCheckoutStrategyDescriptor {
        
        @SuppressWarnings("rawtypes")
        public boolean isApplicable(AbstractProject project) {
            if(project instanceof MatrixProject)
                return true;
            return false;
        }

        public String getDisplayName() {
            return "Skip checkout on matrix parent strategy";
        }

    }


}
