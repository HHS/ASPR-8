package common;

import com.google.protobuf.Message;

public interface MessageBuilderCB {
    Message makeMessage(Object value);
}
