package com.eden.orchid.impl.themes.components;

import com.eden.orchid.api.options.annotations.BooleanDefault;
import com.eden.orchid.api.options.annotations.Description;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.api.theme.components.OrchidComponent;

import javax.inject.Inject;

@Description(value = "Compile and render the default page content. This is added to all pages by default if no other components have been added by the user.", name = "Page Content")
public final class PageContentComponent extends OrchidComponent {

    @Option
    @BooleanDefault(true)
    @Description("Whether to include a wrapper around the normal page content template.")
    protected boolean noWrapper;

    @Inject
    public PageContentComponent() {
        super("pageContent", 100);
    }

    public boolean isNoWrapper() {
        return this.noWrapper;
    }

    public void setNoWrapper(final boolean noWrapper) {
        this.noWrapper = noWrapper;
    }
}
