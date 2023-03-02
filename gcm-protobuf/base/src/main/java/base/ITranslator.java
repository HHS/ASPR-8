package base;

import com.google.protobuf.Message;

public interface ITranslator {
    void init(MasterTranslator translator);

    Object convert(Message message);

    Message convert(Object obj);
}
