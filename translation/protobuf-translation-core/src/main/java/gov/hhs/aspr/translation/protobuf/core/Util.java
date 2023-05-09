package gov.hhs.aspr.translation.protobuf.core;

public class Util {
    /* public JsonObject deepMerge(JsonObject source, JsonObject target) {
        for (String key : source.keySet()) {
            JsonElement value = source.get(key);
            if (!target.has(key)) {
                // new value for "key":
                target.add(key, value);
            } else {
                // existing value for "key" - recursively deep merge:
                if (value.isJsonObject()) {
                    JsonObject valueJson = value.getAsJsonObject();
                    deepMerge(valueJson, target.getAsJsonObject(key));
                } else if (value.isJsonArray()) {
                    JsonArray valueArray = value.getAsJsonArray();
                    JsonArray targetArray = target.getAsJsonArray(key);
                    targetArray.addAll(valueArray);
                } else {
                    target.add(key, value);
                }
            }
        }
        return target;
    } */

    /* protected Class<?> getClassFromInfo(FileDescriptor fileDescriptor, String typeName) {
        boolean javaMultFiles = fileDescriptor.getOptions().getJavaMultipleFiles();
        String javaPackage = fileDescriptor.getOptions().getJavaPackage();
        String javaOuterClassName = fileDescriptor.getOptions().getJavaOuterClassname();
        String protoName = fileDescriptor.getName().split("\\.")[0];

        StringBuilder sb = new StringBuilder();

        sb.append(javaPackage);

        if (!(javaOuterClassName.equals(""))) {
            sb.append(".")
                    .append(javaOuterClassName)
                    .append("$");
        } else if (!javaMultFiles) {
            sb.append(".")
                    .append(protoName.substring(0, 1).toUpperCase())
                    .append(protoName.substring(1))
                    .append("$");
        } else {
            sb.append(".");
        }

        sb.append(typeName);

        String finalClassName = sb.toString();

        try {
            return Class.forName(finalClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    } */
}
