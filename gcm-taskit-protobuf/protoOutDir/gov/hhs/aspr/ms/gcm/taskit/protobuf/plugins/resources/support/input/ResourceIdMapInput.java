// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: resources/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input;

/**
 * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdMapInput}
 */
public final class ResourceIdMapInput extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdMapInput)
    ResourceIdMapInputOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ResourceIdMapInput.newBuilder() to construct.
  private ResourceIdMapInput(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ResourceIdMapInput() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ResourceIdMapInput();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourceIdMapInput_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourceIdMapInput_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput.Builder.class);
  }

  public static final int RESOURCEID_FIELD_NUMBER = 1;
  private gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput resourceId_;
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
   * @return Whether the resourceId field is set.
   */
  @java.lang.Override
  public boolean hasResourceId() {
    return resourceId_ != null;
  }
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
   * @return The resourceId.
   */
  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput getResourceId() {
    return resourceId_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput.getDefaultInstance() : resourceId_;
  }
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
   */
  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInputOrBuilder getResourceIdOrBuilder() {
    return resourceId_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput.getDefaultInstance() : resourceId_;
  }

  public static final int RESOURCETIME_FIELD_NUMBER = 2;
  private double resourceTime_ = 0D;
  /**
   * <code>double resourceTime = 2;</code>
   * @return The resourceTime.
   */
  @java.lang.Override
  public double getResourceTime() {
    return resourceTime_;
  }

  public static final int RESOURCETIMETRACKINGPOLICY_FIELD_NUMBER = 3;
  private boolean resourceTimeTrackingPolicy_ = false;
  /**
   * <code>bool resourceTimeTrackingPolicy = 3;</code>
   * @return The resourceTimeTrackingPolicy.
   */
  @java.lang.Override
  public boolean getResourceTimeTrackingPolicy() {
    return resourceTimeTrackingPolicy_;
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
    if (resourceId_ != null) {
      output.writeMessage(1, getResourceId());
    }
    if (java.lang.Double.doubleToRawLongBits(resourceTime_) != 0) {
      output.writeDouble(2, resourceTime_);
    }
    if (resourceTimeTrackingPolicy_ != false) {
      output.writeBool(3, resourceTimeTrackingPolicy_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (resourceId_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getResourceId());
    }
    if (java.lang.Double.doubleToRawLongBits(resourceTime_) != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeDoubleSize(2, resourceTime_);
    }
    if (resourceTimeTrackingPolicy_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(3, resourceTimeTrackingPolicy_);
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
    if (!(obj instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput)) {
      return super.equals(obj);
    }
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput other = (gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput) obj;

    if (hasResourceId() != other.hasResourceId()) return false;
    if (hasResourceId()) {
      if (!getResourceId()
          .equals(other.getResourceId())) return false;
    }
    if (java.lang.Double.doubleToLongBits(getResourceTime())
        != java.lang.Double.doubleToLongBits(
            other.getResourceTime())) return false;
    if (getResourceTimeTrackingPolicy()
        != other.getResourceTimeTrackingPolicy()) return false;
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
    if (hasResourceId()) {
      hash = (37 * hash) + RESOURCEID_FIELD_NUMBER;
      hash = (53 * hash) + getResourceId().hashCode();
    }
    hash = (37 * hash) + RESOURCETIME_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        java.lang.Double.doubleToLongBits(getResourceTime()));
    hash = (37 * hash) + RESOURCETIMETRACKINGPOLICY_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getResourceTimeTrackingPolicy());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput parseFrom(
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
  public static Builder newBuilder(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput prototype) {
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
   * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdMapInput}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdMapInput)
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInputOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourceIdMapInput_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourceIdMapInput_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput.Builder.class);
    }

    // Construct using gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput.newBuilder()
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
      resourceId_ = null;
      if (resourceIdBuilder_ != null) {
        resourceIdBuilder_.dispose();
        resourceIdBuilder_ = null;
      }
      resourceTime_ = 0D;
      resourceTimeTrackingPolicy_ = false;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourceIdMapInput_descriptor;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput getDefaultInstanceForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput.getDefaultInstance();
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput build() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput buildPartial() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput result = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.resourceId_ = resourceIdBuilder_ == null
            ? resourceId_
            : resourceIdBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.resourceTime_ = resourceTime_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.resourceTimeTrackingPolicy_ = resourceTimeTrackingPolicy_;
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
      if (other instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput) {
        return mergeFrom((gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput other) {
      if (other == gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput.getDefaultInstance()) return this;
      if (other.hasResourceId()) {
        mergeResourceId(other.getResourceId());
      }
      if (other.getResourceTime() != 0D) {
        setResourceTime(other.getResourceTime());
      }
      if (other.getResourceTimeTrackingPolicy() != false) {
        setResourceTimeTrackingPolicy(other.getResourceTimeTrackingPolicy());
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
                  getResourceIdFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 17: {
              resourceTime_ = input.readDouble();
              bitField0_ |= 0x00000002;
              break;
            } // case 17
            case 24: {
              resourceTimeTrackingPolicy_ = input.readBool();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
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

    private gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput resourceId_;
    private com.google.protobuf.SingleFieldBuilderV3<
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInputOrBuilder> resourceIdBuilder_;
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
     * @return Whether the resourceId field is set.
     */
    public boolean hasResourceId() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
     * @return The resourceId.
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput getResourceId() {
      if (resourceIdBuilder_ == null) {
        return resourceId_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput.getDefaultInstance() : resourceId_;
      } else {
        return resourceIdBuilder_.getMessage();
      }
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
     */
    public Builder setResourceId(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput value) {
      if (resourceIdBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        resourceId_ = value;
      } else {
        resourceIdBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
     */
    public Builder setResourceId(
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput.Builder builderForValue) {
      if (resourceIdBuilder_ == null) {
        resourceId_ = builderForValue.build();
      } else {
        resourceIdBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
     */
    public Builder mergeResourceId(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput value) {
      if (resourceIdBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          resourceId_ != null &&
          resourceId_ != gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput.getDefaultInstance()) {
          getResourceIdBuilder().mergeFrom(value);
        } else {
          resourceId_ = value;
        }
      } else {
        resourceIdBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
     */
    public Builder clearResourceId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      resourceId_ = null;
      if (resourceIdBuilder_ != null) {
        resourceIdBuilder_.dispose();
        resourceIdBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput.Builder getResourceIdBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getResourceIdFieldBuilder().getBuilder();
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInputOrBuilder getResourceIdOrBuilder() {
      if (resourceIdBuilder_ != null) {
        return resourceIdBuilder_.getMessageOrBuilder();
      } else {
        return resourceId_ == null ?
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput.getDefaultInstance() : resourceId_;
      }
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdInput resourceId = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInputOrBuilder> 
        getResourceIdFieldBuilder() {
      if (resourceIdBuilder_ == null) {
        resourceIdBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInputOrBuilder>(
                getResourceId(),
                getParentForChildren(),
                isClean());
        resourceId_ = null;
      }
      return resourceIdBuilder_;
    }

    private double resourceTime_ ;
    /**
     * <code>double resourceTime = 2;</code>
     * @return The resourceTime.
     */
    @java.lang.Override
    public double getResourceTime() {
      return resourceTime_;
    }
    /**
     * <code>double resourceTime = 2;</code>
     * @param value The resourceTime to set.
     * @return This builder for chaining.
     */
    public Builder setResourceTime(double value) {
      
      resourceTime_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>double resourceTime = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearResourceTime() {
      bitField0_ = (bitField0_ & ~0x00000002);
      resourceTime_ = 0D;
      onChanged();
      return this;
    }

    private boolean resourceTimeTrackingPolicy_ ;
    /**
     * <code>bool resourceTimeTrackingPolicy = 3;</code>
     * @return The resourceTimeTrackingPolicy.
     */
    @java.lang.Override
    public boolean getResourceTimeTrackingPolicy() {
      return resourceTimeTrackingPolicy_;
    }
    /**
     * <code>bool resourceTimeTrackingPolicy = 3;</code>
     * @param value The resourceTimeTrackingPolicy to set.
     * @return This builder for chaining.
     */
    public Builder setResourceTimeTrackingPolicy(boolean value) {
      
      resourceTimeTrackingPolicy_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>bool resourceTimeTrackingPolicy = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearResourceTimeTrackingPolicy() {
      bitField0_ = (bitField0_ & ~0x00000004);
      resourceTimeTrackingPolicy_ = false;
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


    // @@protoc_insertion_point(builder_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdMapInput)
  }

  // @@protoc_insertion_point(class_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourceIdMapInput)
  private static final gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput();
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ResourceIdMapInput>
      PARSER = new com.google.protobuf.AbstractParser<ResourceIdMapInput>() {
    @java.lang.Override
    public ResourceIdMapInput parsePartialFrom(
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

  public static com.google.protobuf.Parser<ResourceIdMapInput> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ResourceIdMapInput> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

