// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: resources/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input;

public interface PersonResourceLevelMapInputOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonResourceLevelMapInput)
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
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonResourceLevelInput personResourceLevels = 2;</code>
   */
  java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.PersonResourceLevelInput> 
      getPersonResourceLevelsList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonResourceLevelInput personResourceLevels = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.PersonResourceLevelInput getPersonResourceLevels(int index);
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonResourceLevelInput personResourceLevels = 2;</code>
   */
  int getPersonResourceLevelsCount();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonResourceLevelInput personResourceLevels = 2;</code>
   */
  java.util.List<? extends gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.PersonResourceLevelInputOrBuilder> 
      getPersonResourceLevelsOrBuilderList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonResourceLevelInput personResourceLevels = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.PersonResourceLevelInputOrBuilder getPersonResourceLevelsOrBuilder(
      int index);
}
