// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: plugins/partitions/attributes.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.attributes.input;

public interface AttributeFilterInputOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.AttributeFilterInput)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.AttributeIdInput attributeId = 1;</code>
   * @return Whether the attributeId field is set.
   */
  boolean hasAttributeId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.AttributeIdInput attributeId = 1;</code>
   * @return The attributeId.
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.attributes.input.AttributeIdInput getAttributeId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.AttributeIdInput attributeId = 1;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.attributes.input.AttributeIdInputOrBuilder getAttributeIdOrBuilder();

  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.EqualityInput equality = 2;</code>
   * @return The enum numeric value on the wire for equality.
   */
  int getEqualityValue();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.EqualityInput equality = 2;</code>
   * @return The equality.
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.EqualityInput getEquality();

  /**
   * <code>.google.protobuf.Any value = 3;</code>
   * @return Whether the value field is set.
   */
  boolean hasValue();
  /**
   * <code>.google.protobuf.Any value = 3;</code>
   * @return The value.
   */
  com.google.protobuf.Any getValue();
  /**
   * <code>.google.protobuf.Any value = 3;</code>
   */
  com.google.protobuf.AnyOrBuilder getValueOrBuilder();
}