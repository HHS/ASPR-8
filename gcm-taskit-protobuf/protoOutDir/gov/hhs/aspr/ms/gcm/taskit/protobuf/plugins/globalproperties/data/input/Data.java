// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: plugins/globalproperties/data.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.data.input;

public final class Data {
  private Data() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_GlobalPropertiesPluginDataInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_GlobalPropertiesPluginDataInput_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n#plugins/globalproperties/data.proto\022+g" +
      "ov.hhs.aspr.ms.gcm.taskit.protobuf.plugi" +
      "ns\032 plugins/properties/support.proto\"\361\001\n" +
      "\037GlobalPropertiesPluginDataInput\022l\n\033glob" +
      "alPropertyDefinitinions\030\001 \003(\0132G.gov.hhs." +
      "aspr.ms.gcm.taskit.protobuf.plugins.Prop" +
      "ertyDefinitionMapInput\022`\n\024globalProperty" +
      "Values\030\002 \003(\0132B.gov.hhs.aspr.ms.gcm.taski" +
      "t.protobuf.plugins.PropertyValueMapInput" +
      "BK\nGgov.hhs.aspr.ms.gcm.taskit.protobuf." +
      "plugins.globalproperties.data.inputP\001b\006p" +
      "roto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.Support.getDescriptor(),
        });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_GlobalPropertiesPluginDataInput_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_GlobalPropertiesPluginDataInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_GlobalPropertiesPluginDataInput_descriptor,
        new java.lang.String[] { "GlobalPropertyDefinitinions", "GlobalPropertyValues", });
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.Support.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
