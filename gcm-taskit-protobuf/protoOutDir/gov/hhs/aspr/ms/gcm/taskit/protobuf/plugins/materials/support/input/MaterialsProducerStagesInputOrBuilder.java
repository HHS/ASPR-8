// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: materials/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input;

public interface MaterialsProducerStagesInputOrBuilder extends
    // @@protoc_insertion_point(interface_extends:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.MaterialsProducerStagesInput)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.MaterialsProducerIdInput materialsProducerId = 1;</code>
   * @return Whether the materialsProducerId field is set.
   */
  boolean hasMaterialsProducerId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.MaterialsProducerIdInput materialsProducerId = 1;</code>
   * @return The materialsProducerId.
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.MaterialsProducerIdInput getMaterialsProducerId();
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.MaterialsProducerIdInput materialsProducerId = 1;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.MaterialsProducerIdInputOrBuilder getMaterialsProducerIdOrBuilder();

  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stages = 2;</code>
   */
  java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput> 
      getStagesList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stages = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput getStages(int index);
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stages = 2;</code>
   */
  int getStagesCount();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stages = 2;</code>
   */
  java.util.List<? extends gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInputOrBuilder> 
      getStagesOrBuilderList();
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stages = 2;</code>
   */
  gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInputOrBuilder getStagesOrBuilder(
      int index);
}
