// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: groups/reports.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.reports.input;

public interface GroupPropertyReportPropertyMapOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupPropertyReportPropertyMap)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupTypeIdInput groupTypeId = 1;</code>
   * @return Whether the groupTypeId field is set.
   */
  boolean hasGroupTypeId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupTypeIdInput groupTypeId = 1;</code>
   * @return The groupTypeId.
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupTypeIdInput getGroupTypeId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupTypeIdInput groupTypeId = 1;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupTypeIdInputOrBuilder getGroupTypeIdOrBuilder();

  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupPropertyIdInput groupProperties = 2;</code>
   */
  java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupPropertyIdInput> 
      getGroupPropertiesList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupPropertyIdInput groupProperties = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupPropertyIdInput getGroupProperties(int index);
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupPropertyIdInput groupProperties = 2;</code>
   */
  int getGroupPropertiesCount();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupPropertyIdInput groupProperties = 2;</code>
   */
  java.util.List<? extends gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupPropertyIdInputOrBuilder> 
      getGroupPropertiesOrBuilderList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupPropertyIdInput groupProperties = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupPropertyIdInputOrBuilder getGroupPropertiesOrBuilder(
      int index);
}
