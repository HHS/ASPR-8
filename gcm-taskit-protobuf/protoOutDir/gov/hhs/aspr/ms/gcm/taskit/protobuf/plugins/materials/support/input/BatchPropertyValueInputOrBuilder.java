// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: materials/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input;

public interface BatchPropertyValueInputOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.BatchPropertyValueInput)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.BatchIdInput batchId = 1;</code>
   * @return Whether the batchId field is set.
   */
  boolean hasBatchId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.BatchIdInput batchId = 1;</code>
   * @return The batchId.
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.BatchIdInput getBatchId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.BatchIdInput batchId = 1;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.BatchIdInputOrBuilder getBatchIdOrBuilder();

  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyValueMapInput propertyValues = 2;</code>
   */
  java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInput> 
      getPropertyValuesList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyValueMapInput propertyValues = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInput getPropertyValues(int index);
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyValueMapInput propertyValues = 2;</code>
   */
  int getPropertyValuesCount();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyValueMapInput propertyValues = 2;</code>
   */
  java.util.List<? extends gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInputOrBuilder> 
      getPropertyValuesOrBuilderList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyValueMapInput propertyValues = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInputOrBuilder getPropertyValuesOrBuilder(
      int index);
}