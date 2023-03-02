package plugins.globalproperties;

import base.PluginBundle;
import base.TranslatorContext;
import plugins.globalproperties.translators.GlobalPropertiesPluginDataTranslator;

public class GlobalPropertiesPluginBundle implements PluginBundle {

    public void init(TranslatorContext translatorContext) {
        translatorContext.addTranslator(new GlobalPropertiesPluginDataTranslator());
    }

}
