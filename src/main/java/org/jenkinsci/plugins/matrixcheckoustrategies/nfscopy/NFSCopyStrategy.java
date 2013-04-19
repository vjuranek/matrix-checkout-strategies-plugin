package org.jenkinsci.plugins.matrixcheckoustrategies.nfscopy;

import hudson.Extension;
import hudson.FilePath;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild.AbstractBuildExecution;
import hudson.model.AbstractProject;

import java.io.IOException;

import jenkins.scm.SCMCheckoutStrategyDescriptor;
import jenkins.scm.SCMCheckoutStrategy;

import org.kohsuke.stapler.DataBoundConstructor;

public class NFSCopyStrategy extends SCMCheckoutStrategy {
    
    @DataBoundConstructor
    public NFSCopyStrategy() {
        
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void checkout(AbstractBuildExecution execution) throws IOException, InterruptedException {
        
        if(execution instanceof MatrixBuild.MatrixBuildExecution) {
            execution.defaultCheckout();
        } else {
            MatrixProject mp = (MatrixProject)execution.getProject().getParent();
            FilePath ws = ((MatrixRun)execution.getBuild()).getWorkspace();
            FilePath parentWs = ((MatrixRun)execution.getBuild()).getParentBuild().getWorkspace();
            MatrixBuild parentBuild = ((MatrixRun)execution.getBuild()).getParentBuild();
            
            FilePath[] scmRoots = mp.getScm().getModuleRoots(parentWs, parentBuild);
            for(FilePath fp : scmRoots) {
                execution.getListener().getLogger().println("Coping " + fp.getRemote() + " to " + ws.getRemote());
                FilePath scRoot = ws;
                if(!"workspace".equals(fp.getBaseName())) //TODO custom workspace
                    scRoot = new FilePath(ws, fp.getBaseName());
                if(scRoot.exists())
                    scRoot.deleteContents();
                fp.copyRecursiveTo(scRoot);
                //TODO fallback to default if NFS dir doesn't exists or there's some problem
            }
        }
    }
    
    @Extension
    public static class NFSCopyStrategyDescriptor extends SCMCheckoutStrategyDescriptor {
        
        @SuppressWarnings("rawtypes")
        public boolean isApplicable(AbstractProject project) {
            if(project instanceof MatrixProject)
                return true;
            return false;
        }

        public String getDisplayName() {
            return "NFS copy checkout strategy";
        }

    }


}
