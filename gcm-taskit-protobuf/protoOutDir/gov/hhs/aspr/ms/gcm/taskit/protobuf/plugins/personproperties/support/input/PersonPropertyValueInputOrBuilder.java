// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: personproperties/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input;

public interface PersonPropertyValueInputOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonPropertyValueInput)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 pId = 1;</code>
   * @return The pId.
   */
  int getPId();

  /**
   * <code>optional .google.protobuf.Any value = 2;</code>
   * @return Whether the value field is set.
   */
  boolean hasValue();
  /**
   * <code>optional .google.protobuf.Any value = 2;</code>
   * @return The value.
   */
  com.google.protobuf.Any getValue();
  /**
   * <code>optional .google.protobuf.Any value = 2;</code>
   */
  com.google.protobuf.AnyOrBuilder getValueOrBuilder();
}
