package common;

import com.google.gson.JsonObject;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

public interface ITranslator {
    Parser getJsonParser();
    Printer getJsonPrinter();
    void printJson(Message message);
    <T extends Message, U extends Message.Builder> T parseJson(JsonObject inputJson, U builder);
    CommonTranslator getCommonTranslator();
}
