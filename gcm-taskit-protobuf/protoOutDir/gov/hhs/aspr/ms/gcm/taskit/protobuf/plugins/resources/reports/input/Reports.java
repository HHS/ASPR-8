// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: plugins/resources/reports.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.reports.input;

public final class Reports {
  private Reports() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonResourceReportPluginDataInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonResourceReportPluginDataInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourcePropertyReportPluginDataInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourcePropertyReportPluginDataInput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourceReportPluginDataInput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourceReportPluginDataInput_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\037plugins/resources/reports.proto\022+gov.h" +
      "hs.aspr.ms.gcm.taskit.protobuf.plugins\032\035" +
      "plugins/reports/support.proto\032\037plugins/r" +
      "esources/support.proto\"\243\003\n#PersonResourc" +
      "eReportPluginDataInput\022R\n\013reportLabel\030\001 " +
      "\001(\0132=.gov.hhs.aspr.ms.gcm.taskit.protobu" +
      "f.plugins.ReportLabelInput\022T\n\014reportPeri" +
      "od\030\002 \001(\0162>.gov.hhs.aspr.ms.gcm.taskit.pr" +
      "otobuf.plugins.ReportPeriodInput\022X\n\022incl" +
      "udedProperties\030\003 \003(\0132<.gov.hhs.aspr.ms.g" +
      "cm.taskit.protobuf.plugins.ResourceIdInp" +
      "ut\022X\n\022excludedProperties\030\004 \003(\0132<.gov.hhs" +
      ".aspr.ms.gcm.taskit.protobuf.plugins.Res" +
      "ourceIdInput\022\036\n\026defaultInclusionPolicy\030\005" +
      " \001(\010\"{\n%ResourcePropertyReportPluginData" +
      "Input\022R\n\013reportLabel\030\001 \001(\0132=.gov.hhs.asp" +
      "r.ms.gcm.taskit.protobuf.plugins.ReportL" +
      "abelInput\"\235\003\n\035ResourceReportPluginDataIn" +
      "put\022R\n\013reportLabel\030\001 \001(\0132=.gov.hhs.aspr." +
      "ms.gcm.taskit.protobuf.plugins.ReportLab" +
      "elInput\022T\n\014reportPeriod\030\002 \001(\0162>.gov.hhs." +
      "aspr.ms.gcm.taskit.protobuf.plugins.Repo" +
      "rtPeriodInput\022X\n\022includedProperties\030\003 \003(" +
      "\0132<.gov.hhs.aspr.ms.gcm.taskit.protobuf." +
      "plugins.ResourceIdInput\022X\n\022excludedPrope" +
      "rties\030\004 \003(\0132<.gov.hhs.aspr.ms.gcm.taskit" +
      ".protobuf.plugins.ResourceIdInput\022\036\n\026def" +
      "aultInclusionPolicy\030\005 \001(\010BG\nCgov.hhs.asp" +
      "r.ms.gcm.taskit.protobuf.plugins.resourc" +
      "es.reports.inputP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.Support.getDescriptor(),
          gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.getDescriptor(),
        });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonResourceReportPluginDataInput_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonResourceReportPluginDataInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonResourceReportPluginDataInput_descriptor,
        new java.lang.String[] { "ReportLabel", "ReportPeriod", "IncludedProperties", "ExcludedProperties", "DefaultInclusionPolicy", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourcePropertyReportPluginDataInput_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourcePropertyReportPluginDataInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourcePropertyReportPluginDataInput_descriptor,
        new java.lang.String[] { "ReportLabel", });
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourceReportPluginDataInput_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourceReportPluginDataInput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourceReportPluginDataInput_descriptor,
        new java.lang.String[] { "ReportLabel", "ReportPeriod", "IncludedProperties", "ExcludedProperties", "DefaultInclusionPolicy", });
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.Support.getDescriptor();
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
