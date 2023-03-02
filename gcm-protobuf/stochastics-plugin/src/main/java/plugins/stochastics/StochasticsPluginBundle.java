package plugins.stochastics;

import base.PluginBundle;
import base.TranslatorContext;
import plugins.stochastics.translators.StochasticsPluginDataTranslator;

public class StochasticsPluginBundle implements PluginBundle {
    public void init(TranslatorContext translatorContext) {
        translatorContext.addTranslator(new StochasticsPluginDataTranslator());
    }
}
