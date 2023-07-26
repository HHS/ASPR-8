// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: materials/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input;

/**
 * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageInput}
 */
public final class StageInput extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageInput)
    StageInputOrBuilder {
private static final long serialVersionUID = 0L;
  // Use StageInput.newBuilder() to construct.
  private StageInput(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private StageInput() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new StageInput();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_StageInput_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_StageInput_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput.Builder.class);
  }

  public static final int STAGEID_FIELD_NUMBER = 1;
  private gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput stageId_;
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
   * @return Whether the stageId field is set.
   */
  @java.lang.Override
  public boolean hasStageId() {
    return stageId_ != null;
  }
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
   * @return The stageId.
   */
  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput getStageId() {
    return stageId_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput.getDefaultInstance() : stageId_;
  }
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
   */
  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInputOrBuilder getStageIdOrBuilder() {
    return stageId_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput.getDefaultInstance() : stageId_;
  }

  public static final int OFFERED_FIELD_NUMBER = 2;
  private boolean offered_ = false;
  /**
   * <code>bool offered = 2;</code>
   * @return The offered.
   */
  @java.lang.Override
  public boolean getOffered() {
    return offered_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (stageId_ != null) {
      output.writeMessage(1, getStageId());
    }
    if (offered_ != false) {
      output.writeBool(2, offered_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (stageId_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getStageId());
    }
    if (offered_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(2, offered_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput)) {
      return super.equals(obj);
    }
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput other = (gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput) obj;

    if (hasStageId() != other.hasStageId()) return false;
    if (hasStageId()) {
      if (!getStageId()
          .equals(other.getStageId())) return false;
    }
    if (getOffered()
        != other.getOffered()) return false;
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasStageId()) {
      hash = (37 * hash) + STAGEID_FIELD_NUMBER;
      hash = (53 * hash) + getStageId().hashCode();
    }
    hash = (37 * hash) + OFFERED_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getOffered());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageInput}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageInput)
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInputOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_StageInput_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_StageInput_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput.Builder.class);
    }

    // Construct using gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      stageId_ = null;
      if (stageIdBuilder_ != null) {
        stageIdBuilder_.dispose();
        stageIdBuilder_ = null;
      }
      offered_ = false;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_StageInput_descriptor;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput getDefaultInstanceForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput.getDefaultInstance();
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput build() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput buildPartial() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput result = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.stageId_ = stageIdBuilder_ == null
            ? stageId_
            : stageIdBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.offered_ = offered_;
      }
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput) {
        return mergeFrom((gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput other) {
      if (other == gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput.getDefaultInstance()) return this;
      if (other.hasStageId()) {
        mergeStageId(other.getStageId());
      }
      if (other.getOffered() != false) {
        setOffered(other.getOffered());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 10: {
              input.readMessage(
                  getStageIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 16: {
              offered_ = input.readBool();
              bitField0_ |= 0x00000002;
              break;
            } // case 16
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput stageId_;
    private com.google.protobuf.SingleFieldBuilderV3<
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInputOrBuilder> stageIdBuilder_;
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
     * @return Whether the stageId field is set.
     */
    public boolean hasStageId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
     * @return The stageId.
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput getStageId() {
      if (stageIdBuilder_ == null) {
        return stageId_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput.getDefaultInstance() : stageId_;
      } else {
        return stageIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
     */
    public Builder setStageId(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput value) {
      if (stageIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        stageId_ = value;
      } else {
        stageIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
     */
    public Builder setStageId(
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput.Builder builderForValue) {
      if (stageIdBuilder_ == null) {
        stageId_ = builderForValue.build();
      } else {
        stageIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
     */
    public Builder mergeStageId(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput value) {
      if (stageIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          stageId_ != null &&
          stageId_ != gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput.getDefaultInstance()) {
          getStageIdBuilder().mergeFrom(value);
        } else {
          stageId_ = value;
        }
      } else {
        stageIdBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
     */
    public Builder clearStageId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      stageId_ = null;
      if (stageIdBuilder_ != null) {
        stageIdBuilder_.dispose();
        stageIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput.Builder getStageIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getStageIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInputOrBuilder getStageIdOrBuilder() {
      if (stageIdBuilder_ != null) {
        return stageIdBuilder_.getMessageOrBuilder();
      } else {
        return stageId_ == null ?
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput.getDefaultInstance() : stageId_;
      }
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageIdInput stageId = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInputOrBuilder> 
        getStageIdFieldBuilder() {
      if (stageIdBuilder_ == null) {
        stageIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInputOrBuilder>(
                getStageId(),
                getParentForChildren(),
                isClean());
        stageId_ = null;
      }
      return stageIdBuilder_;
    }

    private boolean offered_ ;
    /**
     * <code>bool offered = 2;</code>
     * @return The offered.
     */
    @java.lang.Override
    public boolean getOffered() {
      return offered_;
    }
    /**
     * <code>bool offered = 2;</code>
     * @param value The offered to set.
     * @return This builder for chaining.
     */
    public Builder setOffered(boolean value) {
      
      offered_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>bool offered = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearOffered() {
      bitField0_ = (bitField0_ & ~0x00000002);
      offered_ = false;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageInput)
  }

  // @@protoc_insertion_point(class_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.StageInput)
  private static final gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput();
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<StageInput>
      PARSER = new com.google.protobuf.AbstractParser<StageInput>() {
    @java.lang.Override
    public StageInput parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<StageInput> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<StageInput> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageInput getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
