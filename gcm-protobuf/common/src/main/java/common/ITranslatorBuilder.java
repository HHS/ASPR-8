package common;

import com.google.protobuf.Message;

public interface ITranslatorBuilder {
    ITranslator build();
    ITranslatorBuilder addDescriptor(Message message);
    ITranslatorBuilder setIgnoringUnknownFields(boolean ignoringUnknownFields);
    ITranslatorBuilder setIncludingDefaultValueFields(boolean includingDefaultValueFields);
}
