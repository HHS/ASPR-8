// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: personproperties/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input;

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
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyIdInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyIdInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyValueInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyValueInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyValueMapInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyValueMapInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyTimeInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyTimeInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyTimeMapInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyTimeMapInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyDimensionInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyDimensionInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyFilterInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyFilterInput_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\036personproperties/support.proto\022+gov.hh" +
      "s.aspr.ms.gcm.taskit.protobuf.plugins\032\030p" +
      "artitions/support.proto\032\031google/protobuf" +
      "/any.proto\"9\n\025PersonPropertyIdInput\022 \n\002i" +
      "d\030\001 \001(\0132\024.google.protobuf.Any\"[\n\030PersonP" +
      "ropertyValueInput\022\013\n\003pId\030\001 \001(\005\022(\n\005value\030" +
      "\002 \001(\0132\024.google.protobuf.AnyH\000\210\001\001B\010\n\006_val" +
      "ue\"\332\001\n\033PersonPropertyValueMapInput\022\\\n\020pe" +
      "rsonPropertyId\030\001 \001(\0132B.gov.hhs.aspr.ms.g" +
      "cm.taskit.protobuf.plugins.PersonPropert" +
      "yIdInput\022]\n\016propertyValues\030\002 \003(\0132E.gov.h" +
      "hs.aspr.ms.gcm.taskit.protobuf.plugins.P" +
      "ersonPropertyValueInput\"A\n\027PersonPropert" +
      "yTimeInput\022\013\n\003pId\030\001 \001(\005\022\031\n\021propertyValue" +
      "Time\030\002 \001(\001\"\327\001\n\032PersonPropertyTimeMapInpu" +
      "t\022\\\n\020personPropertyId\030\001 \001(\0132B.gov.hhs.as" +
      "pr.ms.gcm.taskit.protobuf.plugins.Person" +
      "PropertyIdInput\022[\n\rpropertyTimes\030\002 \003(\0132D" +
      ".gov.hhs.aspr.ms.gcm.taskit.protobuf.plu" +
      "gins.PersonPropertyTimeInput\"\266\001\n\034PersonP" +
      "ropertyDimensionInput\022\\\n\020personPropertyI" +
      "d\030\001 \001(\0132B.gov.hhs.aspr.ms.gcm.taskit.pro" +
      "tobuf.plugins.PersonPropertyIdInput\022\022\n\nt" +
      "rackTimes\030\002 \001(\010\022$\n\006values\030\003 \003(\0132\024.google" +
      ".protobuf.Any\"\372\001\n\031PersonPropertyFilterIn" +
      "put\022\\\n\020personPropertyId\030\001 \001(\0132B.gov.hhs." +
      "aspr.ms.gcm.taskit.protobuf.plugins.Pers" +
      "onPropertyIdInput\022L\n\010equality\030\002 \001(\0162:.go" +
      "v.hhs.aspr.ms.gcm.taskit.protobuf.plugin" +
      "s.EqualityInput\0221\n\023personPropertyValue\030\003" +
      " \001(\0132\024.google.protobuf.AnyBN\nJgov.hhs.as" +
      "pr.ms.gcm.taskit.protobuf.plugins.person" +
      "properties.support.inputP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.Support.getDescriptor(),
          com.google.protobuf.AnyProto.getDescriptor(),
        });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyIdInput_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyIdInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyIdInput_descriptor,
        new java.lang.String[] { "Id", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyValueInput_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyValueInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyValueInput_descriptor,
        new java.lang.String[] { "PId", "Value", "Value", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyValueMapInput_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyValueMapInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyValueMapInput_descriptor,
        new java.lang.String[] { "PersonPropertyId", "PropertyValues", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyTimeInput_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyTimeInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyTimeInput_descriptor,
        new java.lang.String[] { "PId", "PropertyValueTime", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyTimeMapInput_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyTimeMapInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyTimeMapInput_descriptor,
        new java.lang.String[] { "PersonPropertyId", "PropertyTimes", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyDimensionInput_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyDimensionInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyDimensionInput_descriptor,
        new java.lang.String[] { "PersonPropertyId", "TrackTimes", "Values", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyFilterInput_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyFilterInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonPropertyFilterInput_descriptor,
        new java.lang.String[] { "PersonPropertyId", "Equality", "PersonPropertyValue", });
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.Support.getDescriptor();
    com.google.protobuf.AnyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}