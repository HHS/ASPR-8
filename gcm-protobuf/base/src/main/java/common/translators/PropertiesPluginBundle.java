package common.translators;

import base.PluginBundle;
import base.TranslatorContext;

public class PropertiesPluginBundle implements PluginBundle {

    public void init(TranslatorContext translatorContext) {
        translatorContext.addTranslator(new PropertyDefinitionTranslator());
    }

}
