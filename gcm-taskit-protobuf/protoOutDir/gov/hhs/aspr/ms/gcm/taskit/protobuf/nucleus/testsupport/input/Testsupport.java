// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: nucleus/testsupport.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input;

public final class Testsupport {
  private Testsupport() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExamplePlanDataInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExamplePlanDataInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExampleDimensionInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExampleDimensionInput_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\031nucleus/testsupport.proto\022+gov.hhs.asp" +
      "r.ms.gcm.taskit.protobuf.nucleus\"(\n\024Exam" +
      "plePlanDataInput\022\020\n\010planTime\030\001 \001(\001\"*\n\025Ex" +
      "ampleDimensionInput\022\021\n\tlevelName\030\001 \001(\tBA" +
      "\n=gov.hhs.aspr.ms.gcm.taskit.protobuf.nu" +
      "cleus.testsupport.inputP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExamplePlanDataInput_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExamplePlanDataInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExamplePlanDataInput_descriptor,
        new java.lang.String[] { "PlanTime", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExampleDimensionInput_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExampleDimensionInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExampleDimensionInput_descriptor,
        new java.lang.String[] { "LevelName", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}