package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.Arrays;
import java.util.List;

public class StandardVersioning {
    public static final String VERSION = "4.2.0";

    public static final List<String> supportedVersions = Arrays.asList("", "4.0.0", "4.1.0", VERSION);

    public static boolean checkVersionSupported(String version) {
        return supportedVersions.contains(version);
    }
}
