// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: regions/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input;

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
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionIdInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionIdInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleRegionIdInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleRegionIdInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyIdInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyIdInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleRegionPropertyIdInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleRegionPropertyIdInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_RegionPersonInfo_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_RegionPersonInfo_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyValueMapInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyValueMapInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyDimensionInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyDimensionInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionFilterInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionFilterInput_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\025regions/support.proto\022+gov.hhs.aspr.ms" +
      ".gcm.taskit.protobuf.plugins\032\030properties" +
      "/support.proto\032\031google/protobuf/any.prot" +
      "o\"1\n\rRegionIdInput\022 \n\002id\030\001 \001(\0132\024.google." +
      "protobuf.Any\":\n\023SimpleRegionIdInput\022#\n\005v" +
      "alue\030\001 \001(\0132\024.google.protobuf.Any\"9\n\025Regi" +
      "onPropertyIdInput\022 \n\002id\030\001 \001(\0132\024.google.p" +
      "rotobuf.Any\"B\n\033SimpleRegionPropertyIdInp" +
      "ut\022#\n\005value\030\001 \001(\0132\024.google.protobuf.Any\"" +
      "\232\002\n\025RegionMembershipInput\022L\n\010regionId\030\001 " +
      "\001(\0132:.gov.hhs.aspr.ms.gcm.taskit.protobu" +
      "f.plugins.RegionIdInput\022c\n\006people\030\002 \003(\0132" +
      "S.gov.hhs.aspr.ms.gcm.taskit.protobuf.pl" +
      "ugins.RegionMembershipInput.RegionPerson" +
      "Info\032N\n\020RegionPersonInfo\022\020\n\010personId\030\001 \001" +
      "(\005\022\030\n\013arrivalTime\030\003 \001(\001H\000\210\001\001B\016\n\014_arrival" +
      "Time\"\311\001\n\033RegionPropertyValueMapInput\022L\n\010" +
      "regionId\030\001 \001(\0132:.gov.hhs.aspr.ms.gcm.tas" +
      "kit.protobuf.plugins.RegionIdInput\022\\\n\020pr" +
      "opertyValueMap\030\002 \003(\0132B.gov.hhs.aspr.ms.g" +
      "cm.taskit.protobuf.plugins.PropertyValue" +
      "MapInput\"\360\001\n\034RegionPropertyDimensionInpu" +
      "t\022L\n\010regionId\030\001 \001(\0132:.gov.hhs.aspr.ms.gc" +
      "m.taskit.protobuf.plugins.RegionIdInput\022" +
      "\\\n\020regionPropertyId\030\002 \001(\0132B.gov.hhs.aspr" +
      ".ms.gcm.taskit.protobuf.plugins.RegionPr" +
      "opertyIdInput\022$\n\006values\030\003 \003(\0132\024.google.p" +
      "rotobuf.Any\"b\n\021RegionFilterInput\022M\n\tregi" +
      "onIds\030\001 \003(\0132:.gov.hhs.aspr.ms.gcm.taskit" +
      ".protobuf.plugins.RegionIdInputBE\nAgov.h" +
      "hs.aspr.ms.gcm.taskit.protobuf.plugins.r" +
      "egions.support.inputP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.Support.getDescriptor(),
          com.google.protobuf.AnyProto.getDescriptor(),
        });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionIdInput_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionIdInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionIdInput_descriptor,
        new java.lang.String[] { "Id", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleRegionIdInput_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleRegionIdInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleRegionIdInput_descriptor,
        new java.lang.String[] { "Value", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyIdInput_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyIdInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyIdInput_descriptor,
        new java.lang.String[] { "Id", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleRegionPropertyIdInput_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleRegionPropertyIdInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleRegionPropertyIdInput_descriptor,
        new java.lang.String[] { "Value", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_descriptor,
        new java.lang.String[] { "RegionId", "People", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_RegionPersonInfo_descriptor =
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_descriptor.getNestedTypes().get(0);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_RegionPersonInfo_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionMembershipInput_RegionPersonInfo_descriptor,
        new java.lang.String[] { "PersonId", "ArrivalTime", "ArrivalTime", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyValueMapInput_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyValueMapInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyValueMapInput_descriptor,
        new java.lang.String[] { "RegionId", "PropertyValueMap", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyDimensionInput_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyDimensionInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionPropertyDimensionInput_descriptor,
        new java.lang.String[] { "RegionId", "RegionPropertyId", "Values", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionFilterInput_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionFilterInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_RegionFilterInput_descriptor,
        new java.lang.String[] { "RegionIds", });
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.Support.getDescriptor();
    com.google.protobuf.AnyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
