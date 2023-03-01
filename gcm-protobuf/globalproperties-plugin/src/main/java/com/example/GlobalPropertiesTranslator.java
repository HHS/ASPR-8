package com.example;

import common.CommonTranslator;

public class GlobalPropertiesTranslator extends CommonTranslator {
    private GlobalPropertiesTranslator(Data data) {
        super(data);
    }

    protected static class Data extends CommonTranslator.Data {
        protected Data() {
            super();
        }
    }

    public static class Builder extends CommonTranslator.Builder {
        private Builder(Data data) {
            super(data);
            this.addCustomTranslator(new GlobalPropertiesPluginDataTranslator());
        }
    }

    public static Builder builder() {
        return new Builder(new Data());
    }

}
