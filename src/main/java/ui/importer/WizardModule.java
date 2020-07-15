package ui.importer;

import com.google.inject.AbstractModule;
import domain.importing.WizardData;

/**
 * Created by carl on 4/30/16.
 */
public class WizardModule extends AbstractModule {
    @Override
    protected void configure() {
        WizardData model = new WizardData();
        bind(WizardData.class).toInstance(model);
    }
}
