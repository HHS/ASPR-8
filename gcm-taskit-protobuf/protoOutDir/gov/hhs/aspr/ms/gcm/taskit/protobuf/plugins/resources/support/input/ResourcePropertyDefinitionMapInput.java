// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: resources/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input;

/**
 * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourcePropertyDefinitionMapInput}
 */
public final class ResourcePropertyDefinitionMapInput extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourcePropertyDefinitionMapInput)
    ResourcePropertyDefinitionMapInputOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ResourcePropertyDefinitionMapInput.newBuilder() to construct.
  private ResourcePropertyDefinitionMapInput(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ResourcePropertyDefinitionMapInput() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ResourcePropertyDefinitionMapInput();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourcePropertyDefinitionMapInput_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourcePropertyDefinitionMapInput_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput.Builder.class);
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

  public static final int RESOURCEPROPERTYDEFINITIONMAP_FIELD_NUMBER = 2;
  private gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput resourcePropertyDefinitionMap_;
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
   * @return Whether the resourcePropertyDefinitionMap field is set.
   */
  @java.lang.Override
  public boolean hasResourcePropertyDefinitionMap() {
    return resourcePropertyDefinitionMap_ != null;
  }
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
   * @return The resourcePropertyDefinitionMap.
   */
  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput getResourcePropertyDefinitionMap() {
    return resourcePropertyDefinitionMap_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput.getDefaultInstance() : resourcePropertyDefinitionMap_;
  }
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
   */
  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInputOrBuilder getResourcePropertyDefinitionMapOrBuilder() {
    return resourcePropertyDefinitionMap_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput.getDefaultInstance() : resourcePropertyDefinitionMap_;
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
    if (resourcePropertyDefinitionMap_ != null) {
      output.writeMessage(2, getResourcePropertyDefinitionMap());
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
    if (resourcePropertyDefinitionMap_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getResourcePropertyDefinitionMap());
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
    if (!(obj instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput)) {
      return super.equals(obj);
    }
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput other = (gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput) obj;

    if (hasResourceId() != other.hasResourceId()) return false;
    if (hasResourceId()) {
      if (!getResourceId()
          .equals(other.getResourceId())) return false;
    }
    if (hasResourcePropertyDefinitionMap() != other.hasResourcePropertyDefinitionMap()) return false;
    if (hasResourcePropertyDefinitionMap()) {
      if (!getResourcePropertyDefinitionMap()
          .equals(other.getResourcePropertyDefinitionMap())) return false;
    }
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
    if (hasResourcePropertyDefinitionMap()) {
      hash = (37 * hash) + RESOURCEPROPERTYDEFINITIONMAP_FIELD_NUMBER;
      hash = (53 * hash) + getResourcePropertyDefinitionMap().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput parseFrom(
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
  public static Builder newBuilder(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput prototype) {
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
   * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourcePropertyDefinitionMapInput}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourcePropertyDefinitionMapInput)
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInputOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourcePropertyDefinitionMapInput_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourcePropertyDefinitionMapInput_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput.Builder.class);
    }

    // Construct using gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput.newBuilder()
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
      resourcePropertyDefinitionMap_ = null;
      if (resourcePropertyDefinitionMapBuilder_ != null) {
        resourcePropertyDefinitionMapBuilder_.dispose();
        resourcePropertyDefinitionMapBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_ResourcePropertyDefinitionMapInput_descriptor;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput getDefaultInstanceForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput.getDefaultInstance();
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput build() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput buildPartial() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput result = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.resourceId_ = resourceIdBuilder_ == null
            ? resourceId_
            : resourceIdBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.resourcePropertyDefinitionMap_ = resourcePropertyDefinitionMapBuilder_ == null
            ? resourcePropertyDefinitionMap_
            : resourcePropertyDefinitionMapBuilder_.build();
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
      if (other instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput) {
        return mergeFrom((gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput other) {
      if (other == gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput.getDefaultInstance()) return this;
      if (other.hasResourceId()) {
        mergeResourceId(other.getResourceId());
      }
      if (other.hasResourcePropertyDefinitionMap()) {
        mergeResourcePropertyDefinitionMap(other.getResourcePropertyDefinitionMap());
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
            case 18: {
              input.readMessage(
                  getResourcePropertyDefinitionMapFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000002;
              break;
            } // case 18
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

    private gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput resourcePropertyDefinitionMap_;
    private com.google.protobuf.SingleFieldBuilderV3<
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInputOrBuilder> resourcePropertyDefinitionMapBuilder_;
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
     * @return Whether the resourcePropertyDefinitionMap field is set.
     */
    public boolean hasResourcePropertyDefinitionMap() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
     * @return The resourcePropertyDefinitionMap.
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput getResourcePropertyDefinitionMap() {
      if (resourcePropertyDefinitionMapBuilder_ == null) {
        return resourcePropertyDefinitionMap_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput.getDefaultInstance() : resourcePropertyDefinitionMap_;
      } else {
        return resourcePropertyDefinitionMapBuilder_.getMessage();
      }
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
     */
    public Builder setResourcePropertyDefinitionMap(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput value) {
      if (resourcePropertyDefinitionMapBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        resourcePropertyDefinitionMap_ = value;
      } else {
        resourcePropertyDefinitionMapBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
     */
    public Builder setResourcePropertyDefinitionMap(
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput.Builder builderForValue) {
      if (resourcePropertyDefinitionMapBuilder_ == null) {
        resourcePropertyDefinitionMap_ = builderForValue.build();
      } else {
        resourcePropertyDefinitionMapBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
     */
    public Builder mergeResourcePropertyDefinitionMap(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput value) {
      if (resourcePropertyDefinitionMapBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          resourcePropertyDefinitionMap_ != null &&
          resourcePropertyDefinitionMap_ != gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput.getDefaultInstance()) {
          getResourcePropertyDefinitionMapBuilder().mergeFrom(value);
        } else {
          resourcePropertyDefinitionMap_ = value;
        }
      } else {
        resourcePropertyDefinitionMapBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
     */
    public Builder clearResourcePropertyDefinitionMap() {
      bitField0_ = (bitField0_ & ~0x00000002);
      resourcePropertyDefinitionMap_ = null;
      if (resourcePropertyDefinitionMapBuilder_ != null) {
        resourcePropertyDefinitionMapBuilder_.dispose();
        resourcePropertyDefinitionMapBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput.Builder getResourcePropertyDefinitionMapBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getResourcePropertyDefinitionMapFieldBuilder().getBuilder();
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInputOrBuilder getResourcePropertyDefinitionMapOrBuilder() {
      if (resourcePropertyDefinitionMapBuilder_ != null) {
        return resourcePropertyDefinitionMapBuilder_.getMessageOrBuilder();
      } else {
        return resourcePropertyDefinitionMap_ == null ?
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput.getDefaultInstance() : resourcePropertyDefinitionMap_;
      }
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PropertyDefinitionMapInput resourcePropertyDefinitionMap = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInputOrBuilder> 
        getResourcePropertyDefinitionMapFieldBuilder() {
      if (resourcePropertyDefinitionMapBuilder_ == null) {
        resourcePropertyDefinitionMapBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInputOrBuilder>(
                getResourcePropertyDefinitionMap(),
                getParentForChildren(),
                isClean());
        resourcePropertyDefinitionMap_ = null;
      }
      return resourcePropertyDefinitionMapBuilder_;
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


    // @@protoc_insertion_point(builder_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourcePropertyDefinitionMapInput)
  }

  // @@protoc_insertion_point(class_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.ResourcePropertyDefinitionMapInput)
  private static final gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput();
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ResourcePropertyDefinitionMapInput>
      PARSER = new com.google.protobuf.AbstractParser<ResourcePropertyDefinitionMapInput>() {
    @java.lang.Override
    public ResourcePropertyDefinitionMapInput parsePartialFrom(
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

  public static com.google.protobuf.Parser<ResourcePropertyDefinitionMapInput> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ResourcePropertyDefinitionMapInput> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

