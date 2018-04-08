package org.widgetplugin;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

@Extension
public class WidgetDescriptor extends BuildStepDescriptor<Publisher> {

	/**
     * Constructs a {@link WidgetDescriptor}.
     */
    public WidgetDescriptor() {
    	
        super(WidgetRecorder.class);
    }

    /**
     * Gets the descriptor display name, used in the post step checkbox
     * description.
     * @return the descriptor display name
     */
    @Override
    public final String getDisplayName() {
        return "Activate Widget Plugin";
    }

    /**
     * Checks whether this descriptor is applicable.
     * @param clazz
     *            the class
     * @return true - of course the beard is applicable
     */
    @Override
    public final boolean isApplicable(
            final Class<? extends AbstractProject> clazz) {
        return true;
    }
	
}
