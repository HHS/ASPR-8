package gov.hhs.aspr.gcm.translation.core;

import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;

public interface ITranslator {
    void init(MasterTranslator translator);

    <T> T convert(Message message);

    <T> T convert(ProtocolMessageEnum messageEnum);

    <T> T convert(Object obj);
}
