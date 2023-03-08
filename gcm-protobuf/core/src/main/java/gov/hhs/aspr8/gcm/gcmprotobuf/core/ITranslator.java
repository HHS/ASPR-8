package gov.hhs.aspr8.gcm.gcmprotobuf.core;

import com.google.protobuf.Message;

public interface ITranslator {
    void init(MasterTranslator translator);

    <T> T convert(Message message);

    <T> T convert(Object obj);
}
