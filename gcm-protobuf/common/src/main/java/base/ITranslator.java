package base;

import com.google.protobuf.Message;

import common.Translator;

public interface ITranslator {
    void init(Translator translator);

    Object convert(Message message);

    Message convert(Object obj);
}
