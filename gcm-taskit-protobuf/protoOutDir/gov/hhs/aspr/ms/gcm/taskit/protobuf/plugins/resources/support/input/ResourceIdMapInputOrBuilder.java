// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: resources/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input;

public interface ResourceIdMapInputOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdMapInput)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
   * @return Whether the resourceId field is set.
   */
  boolean hasResourceId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
   * @return The resourceId.
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput getResourceId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInputOrBuilder getResourceIdOrBuilder();

  /**
   * <code>double resourceTime = 2;</code>
   * @return The resourceTime.
   */
  double getResourceTime();

  /**
   * <code>bool resourceTimeTrackingPolicy = 3;</code>
   * @return The resourceTimeTrackingPolicy.
   */
  boolean getResourceTimeTrackingPolicy();
}
