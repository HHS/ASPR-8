// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: groups/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input;

public interface GroupPropertyValueMapInputOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupPropertyValueMapInput)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupIdInput groupId = 1;</code>
   * @return Whether the groupId field is set.
   */
  boolean hasGroupId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupIdInput groupId = 1;</code>
   * @return The groupId.
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupIdInput getGroupId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.GroupIdInput groupId = 1;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupIdInputOrBuilder getGroupIdOrBuilder();

  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyValueMapInput propertyValueMap = 2;</code>
   */
  java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInput> 
      getPropertyValueMapList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyValueMapInput propertyValueMap = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInput getPropertyValueMap(int index);
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyValueMapInput propertyValueMap = 2;</code>
   */
  int getPropertyValueMapCount();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyValueMapInput propertyValueMap = 2;</code>
   */
  java.util.List<? extends gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInputOrBuilder> 
      getPropertyValueMapOrBuilderList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyValueMapInput propertyValueMap = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInputOrBuilder getPropertyValueMapOrBuilder(
      int index);
}