// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: personproperties/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input;

public interface PersonPropertyTimeMapInputOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonPropertyTimeMapInput)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonPropertyIdInput personPropertyId = 1;</code>
   * @return Whether the personPropertyId field is set.
   */
  boolean hasPersonPropertyId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonPropertyIdInput personPropertyId = 1;</code>
   * @return The personPropertyId.
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyIdInput getPersonPropertyId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonPropertyIdInput personPropertyId = 1;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyIdInputOrBuilder getPersonPropertyIdOrBuilder();

  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonPropertyTimeInput propertyTimes = 2;</code>
   */
  java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyTimeInput> 
      getPropertyTimesList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonPropertyTimeInput propertyTimes = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyTimeInput getPropertyTimes(int index);
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonPropertyTimeInput propertyTimes = 2;</code>
   */
  int getPropertyTimesCount();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonPropertyTimeInput propertyTimes = 2;</code>
   */
  java.util.List<? extends gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyTimeInputOrBuilder> 
      getPropertyTimesOrBuilderList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonPropertyTimeInput propertyTimes = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyTimeInputOrBuilder getPropertyTimesOrBuilder(
      int index);
}
