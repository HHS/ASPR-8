// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: regions/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input;

public interface RegionPropertyValueMapInputOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.RegionPropertyValueMapInput)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.RegionIdInput regionId = 1;</code>
   * @return Whether the regionId field is set.
   */
  boolean hasRegionId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.RegionIdInput regionId = 1;</code>
   * @return The regionId.
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionIdInput getRegionId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.RegionIdInput regionId = 1;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionIdInputOrBuilder getRegionIdOrBuilder();

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
