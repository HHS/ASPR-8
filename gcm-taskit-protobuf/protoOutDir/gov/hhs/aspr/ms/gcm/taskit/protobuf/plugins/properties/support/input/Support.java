// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: properties/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input;

public final class Support {
  private Support() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyValueMapInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyValueMapInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyDefinitionMapInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyDefinitionMapInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyDefinitionInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyDefinitionInput_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\030properties/support.proto\022+gov.hhs.aspr" +
      ".ms.gcm.taskit.protobuf.plugins\032\031google/" +
      "protobuf/any.proto\"\244\001\n\025PropertyValueMapI" +
      "nput\022(\n\npropertyId\030\001 \001(\0132\024.google.protob" +
      "uf.Any\022+\n\rpropertyValue\030\002 \001(\0132\024.google.p" +
      "rotobuf.Any\022\036\n\021propertyValueTime\030\003 \001(\001H\000" +
      "\210\001\001B\024\n\022_propertyValueTime\"\350\001\n\032PropertyDe" +
      "finitionMapInput\022(\n\npropertyId\030\001 \001(\0132\024.g" +
      "oogle.protobuf.Any\022`\n\022propertyDefinition" +
      "\030\002 \001(\0132D.gov.hhs.aspr.ms.gcm.taskit.prot" +
      "obuf.plugins.PropertyDefinitionInput\022\036\n\026" +
      "propertyDefinitionTime\030\003 \001(\001\022\036\n\026property" +
      "TrackingPolicy\030\004 \001(\010\"\213\001\n\027PropertyDefinit" +
      "ionInput\022\014\n\004type\030\001 \001(\t\022 \n\030propertyValues" +
      "AreMutable\030\002 \001(\010\022/\n\014defaultValue\030\003 \001(\0132\024" +
      ".google.protobuf.AnyH\000\210\001\001B\017\n\r_defaultVal" +
      "ueBH\nDgov.hhs.aspr.ms.gcm.taskit.protobu" +
      "f.plugins.properties.support.inputP\001b\006pr" +
      "oto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.AnyProto.getDescriptor(),
        });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyValueMapInput_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyValueMapInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyValueMapInput_descriptor,
        new java.lang.String[] { "PropertyId", "PropertyValue", "PropertyValueTime", "PropertyValueTime", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyDefinitionMapInput_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyDefinitionMapInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyDefinitionMapInput_descriptor,
        new java.lang.String[] { "PropertyId", "PropertyDefinition", "PropertyDefinitionTime", "PropertyTrackingPolicy", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyDefinitionInput_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyDefinitionInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PropertyDefinitionInput_descriptor,
        new java.lang.String[] { "Type", "PropertyValuesAreMutable", "DefaultValue", "DefaultValue", });
    com.google.protobuf.AnyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}