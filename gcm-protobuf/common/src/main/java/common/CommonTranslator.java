package common;

import common.translators.PropertyDefinitionTranslator;

public class CommonTranslator extends Translator {

    protected CommonTranslator(Data data) {
        super(data);
    }

    protected static class Data extends Translator.Data {
        protected Data() {
            super();
        }
    }

    public static class Builder extends Translator.Builder {
        protected Builder(Data data) {
            super(data);
            this.addCustomTranslator(new PropertyDefinitionTranslator());
        }
    }

    public static Builder builder() {
        return new Builder(new Data());
    }
}
