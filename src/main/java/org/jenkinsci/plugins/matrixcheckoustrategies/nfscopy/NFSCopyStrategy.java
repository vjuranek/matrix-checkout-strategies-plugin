package org.jenkinsci.plugins.matrixcheckoustrategies.nfscopy;

import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractBuild.AbstractBuildExecution;
import hudson.model.AbstractProject;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import jenkins.scm.SCMCheckoutStrategyDescriptor;
import jenkins.scm.SCMCheckoutStrategy;

import org.kohsuke.stapler.DataBoundConstructor;

public class NFSCopyStrategy extends SCMCheckoutStrategy {
    
    private String nfsLocation;
    
    @DataBoundConstructor
    public NFSCopyStrategy(String nfsLocation) {
        this.nfsLocation = nfsLocation;
    }
    
    public String getNfsLocation() {
        return nfsLocation;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void checkout(AbstractBuildExecution execution) throws IOException, InterruptedException {
        
        if(execution instanceof MatrixBuild.MatrixBuildExecution) {
            execution.defaultCheckout();
            if(Util.nullify(nfsLocation) != null) {
                copySources(getScmRoots(execution), new FilePath(new File(nfsLocation)), execution.getListener().getLogger());
            }
        } else {
            FilePath ws = ((MatrixRun)execution.getBuild()).getWorkspace();
            if(Util.nullify(nfsLocation) != null) {
                FilePath[] nfsPath = {new FilePath(new File(nfsLocation))};
                copySources(nfsPath, ws, execution.getListener().getLogger());
            } else {
                copySources(getScmRoots(execution), ws, execution.getListener().getLogger());
            }
        }
    }
    
    private FilePath[] getScmRoots(AbstractBuildExecution execution) {
        MatrixProject mp;
        AbstractBuild mb;
        if(execution instanceof MatrixBuild.MatrixBuildExecution) {
            mp = (MatrixProject)execution.getProject();
            mb = (MatrixBuild)execution.getBuild();
        } else {
            mp = (MatrixProject)execution.getProject().getParent();
            mb = ((MatrixRun)execution.getBuild()).getParentBuild();
        } 
        return mp.getScm().getModuleRoots(mb.getWorkspace(), mb);
    }
    
    private void copySources(FilePath[] scmRoots, FilePath ws, PrintStream logger) throws IOException, InterruptedException {
        for(FilePath fp : scmRoots) {
            logger.println("Coping " + fp.getRemote() + " to " + ws.getRemote());
            FilePath scRoot = ws;
            if(!"workspace".equals(fp.getBaseName())) //TODO custom workspace
                scRoot = new FilePath(ws, fp.getBaseName());
            if(scRoot.exists())
                scRoot.deleteContents();
            fp.copyRecursiveTo(scRoot);
            //TODO fallback to default if NFS dir doesn't exists or there's some problem
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
