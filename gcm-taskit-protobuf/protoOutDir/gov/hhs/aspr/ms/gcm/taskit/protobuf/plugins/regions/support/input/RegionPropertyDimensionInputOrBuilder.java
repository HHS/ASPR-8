// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: regions/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input;

public interface RegionPropertyDimensionInputOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.RegionPropertyDimensionInput)
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
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.RegionPropertyIdInput regionPropertyId = 2;</code>
   * @return Whether the regionPropertyId field is set.
   */
  boolean hasRegionPropertyId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.RegionPropertyIdInput regionPropertyId = 2;</code>
   * @return The regionPropertyId.
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionPropertyIdInput getRegionPropertyId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.RegionPropertyIdInput regionPropertyId = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionPropertyIdInputOrBuilder getRegionPropertyIdOrBuilder();

  /**
   * <code>repeated .google.protobuf.Any values = 3;</code>
   */
  java.util.List<com.google.protobuf.Any> 
      getValuesList();
  /**
   * <code>repeated .google.protobuf.Any values = 3;</code>
   */
  com.google.protobuf.Any getValues(int index);
  /**
   * <code>repeated .google.protobuf.Any values = 3;</code>
   */
  int getValuesCount();
  /**
   * <code>repeated .google.protobuf.Any values = 3;</code>
   */
  java.util.List<? extends com.google.protobuf.AnyOrBuilder> 
      getValuesOrBuilderList();
  /**
   * <code>repeated .google.protobuf.Any values = 3;</code>
   */
  com.google.protobuf.AnyOrBuilder getValuesOrBuilder(
      int index);
}
