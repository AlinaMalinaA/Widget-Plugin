package org.widgetplugin;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;

public class WidgetRecorder extends Recorder{

	/**
     * Constructs a {@link WidgetRecorder}
     */
    @DataBoundConstructor
    public WidgetRecorder() {
    }

    /**
     * Gets the WidgetAction as the project action. This is applicable for
     * each job and only when there's at least one build in the job.
     * @param project
     *            the project
     * @return the project action
     */
    @Override
    public final Action getProjectAction(final AbstractProject<?, ?> project) {
        Action action = null;
        if (project.getLastBuild() != null) {
            action = new WidgetAction();
        }
        return action;
    }

    /**
     * Adds WidgetAction to the build actions. This is applicable for each
     * build.
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @param listener
     *            the listener
     * @return true
     * @throws InterruptedException
     *             when there's an interruption
     * @throws IOException
     *             when there's an IO error
     */
    @Override
    public final boolean perform(final AbstractBuild<?, ?> build,
            final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        build.getActions().add(new WidgetAction());
        return true;
    }

    /**
     * Gets the required monitor service.
     * @return the BuildStepMonitor
     */
    public final BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
	
}
