// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: partitions/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input;

/**
 * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionMapInput}
 */
public final class PartitionMapInput extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionMapInput)
    PartitionMapInputOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PartitionMapInput.newBuilder() to construct.
  private PartitionMapInput(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PartitionMapInput() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new PartitionMapInput();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PartitionMapInput_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PartitionMapInput_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput.Builder.class);
  }

  public static final int KEY_FIELD_NUMBER = 1;
  private com.google.protobuf.Any key_;
  /**
   * <code>.google.protobuf.Any key = 1;</code>
   * @return Whether the key field is set.
   */
  @java.lang.Override
  public boolean hasKey() {
    return key_ != null;
  }
  /**
   * <code>.google.protobuf.Any key = 1;</code>
   * @return The key.
   */
  @java.lang.Override
  public com.google.protobuf.Any getKey() {
    return key_ == null ? com.google.protobuf.Any.getDefaultInstance() : key_;
  }
  /**
   * <code>.google.protobuf.Any key = 1;</code>
   */
  @java.lang.Override
  public com.google.protobuf.AnyOrBuilder getKeyOrBuilder() {
    return key_ == null ? com.google.protobuf.Any.getDefaultInstance() : key_;
  }

  public static final int PARTITION_FIELD_NUMBER = 2;
  private gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput partition_;
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
   * @return Whether the partition field is set.
   */
  @java.lang.Override
  public boolean hasPartition() {
    return partition_ != null;
  }
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
   * @return The partition.
   */
  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput getPartition() {
    return partition_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput.getDefaultInstance() : partition_;
  }
  /**
   * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
   */
  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInputOrBuilder getPartitionOrBuilder() {
    return partition_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput.getDefaultInstance() : partition_;
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
    if (key_ != null) {
      output.writeMessage(1, getKey());
    }
    if (partition_ != null) {
      output.writeMessage(2, getPartition());
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (key_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getKey());
    }
    if (partition_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getPartition());
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
    if (!(obj instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput)) {
      return super.equals(obj);
    }
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput other = (gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput) obj;

    if (hasKey() != other.hasKey()) return false;
    if (hasKey()) {
      if (!getKey()
          .equals(other.getKey())) return false;
    }
    if (hasPartition() != other.hasPartition()) return false;
    if (hasPartition()) {
      if (!getPartition()
          .equals(other.getPartition())) return false;
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
    if (hasKey()) {
      hash = (37 * hash) + KEY_FIELD_NUMBER;
      hash = (53 * hash) + getKey().hashCode();
    }
    if (hasPartition()) {
      hash = (37 * hash) + PARTITION_FIELD_NUMBER;
      hash = (53 * hash) + getPartition().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput parseFrom(
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
  public static Builder newBuilder(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput prototype) {
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
   * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionMapInput}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionMapInput)
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInputOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PartitionMapInput_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PartitionMapInput_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput.Builder.class);
    }

    // Construct using gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput.newBuilder()
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
      key_ = null;
      if (keyBuilder_ != null) {
        keyBuilder_.dispose();
        keyBuilder_ = null;
      }
      partition_ = null;
      if (partitionBuilder_ != null) {
        partitionBuilder_.dispose();
        partitionBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PartitionMapInput_descriptor;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput getDefaultInstanceForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput.getDefaultInstance();
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput build() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput buildPartial() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput result = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.key_ = keyBuilder_ == null
            ? key_
            : keyBuilder_.build();
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.partition_ = partitionBuilder_ == null
            ? partition_
            : partitionBuilder_.build();
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
      if (other instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput) {
        return mergeFrom((gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput other) {
      if (other == gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput.getDefaultInstance()) return this;
      if (other.hasKey()) {
        mergeKey(other.getKey());
      }
      if (other.hasPartition()) {
        mergePartition(other.getPartition());
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
                  getKeyFieldBuilder().getBuilder(),
                  extensionRegistry);
              bitField0_ |= 0x00000001;
              break;
            } // case 10
            case 18: {
              input.readMessage(
                  getPartitionFieldBuilder().getBuilder(),
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

    private com.google.protobuf.Any key_;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> keyBuilder_;
    /**
     * <code>.google.protobuf.Any key = 1;</code>
     * @return Whether the key field is set.
     */
    public boolean hasKey() {
      return ((bitField0_ & 0x00000001) != 0);
    }
    /**
     * <code>.google.protobuf.Any key = 1;</code>
     * @return The key.
     */
    public com.google.protobuf.Any getKey() {
      if (keyBuilder_ == null) {
        return key_ == null ? com.google.protobuf.Any.getDefaultInstance() : key_;
      } else {
        return keyBuilder_.getMessage();
      }
    }
    /**
     * <code>.google.protobuf.Any key = 1;</code>
     */
    public Builder setKey(com.google.protobuf.Any value) {
      if (keyBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        key_ = value;
      } else {
        keyBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Any key = 1;</code>
     */
    public Builder setKey(
        com.google.protobuf.Any.Builder builderForValue) {
      if (keyBuilder_ == null) {
        key_ = builderForValue.build();
      } else {
        keyBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Any key = 1;</code>
     */
    public Builder mergeKey(com.google.protobuf.Any value) {
      if (keyBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0) &&
          key_ != null &&
          key_ != com.google.protobuf.Any.getDefaultInstance()) {
          getKeyBuilder().mergeFrom(value);
        } else {
          key_ = value;
        }
      } else {
        keyBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Any key = 1;</code>
     */
    public Builder clearKey() {
      bitField0_ = (bitField0_ & ~0x00000001);
      key_ = null;
      if (keyBuilder_ != null) {
        keyBuilder_.dispose();
        keyBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.google.protobuf.Any key = 1;</code>
     */
    public com.google.protobuf.Any.Builder getKeyBuilder() {
      bitField0_ |= 0x00000001;
      onChanged();
      return getKeyFieldBuilder().getBuilder();
    }
    /**
     * <code>.google.protobuf.Any key = 1;</code>
     */
    public com.google.protobuf.AnyOrBuilder getKeyOrBuilder() {
      if (keyBuilder_ != null) {
        return keyBuilder_.getMessageOrBuilder();
      } else {
        return key_ == null ?
            com.google.protobuf.Any.getDefaultInstance() : key_;
      }
    }
    /**
     * <code>.google.protobuf.Any key = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> 
        getKeyFieldBuilder() {
      if (keyBuilder_ == null) {
        keyBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder>(
                getKey(),
                getParentForChildren(),
                isClean());
        key_ = null;
      }
      return keyBuilder_;
    }

    private gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput partition_;
    private com.google.protobuf.SingleFieldBuilderV3<
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInputOrBuilder> partitionBuilder_;
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
     * @return Whether the partition field is set.
     */
    public boolean hasPartition() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
     * @return The partition.
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput getPartition() {
      if (partitionBuilder_ == null) {
        return partition_ == null ? gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput.getDefaultInstance() : partition_;
      } else {
        return partitionBuilder_.getMessage();
      }
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
     */
    public Builder setPartition(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput value) {
      if (partitionBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        partition_ = value;
      } else {
        partitionBuilder_.setMessage(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
     */
    public Builder setPartition(
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput.Builder builderForValue) {
      if (partitionBuilder_ == null) {
        partition_ = builderForValue.build();
      } else {
        partitionBuilder_.setMessage(builderForValue.build());
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
     */
    public Builder mergePartition(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput value) {
      if (partitionBuilder_ == null) {
        if (((bitField0_ & 0x00000002) != 0) &&
          partition_ != null &&
          partition_ != gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput.getDefaultInstance()) {
          getPartitionBuilder().mergeFrom(value);
        } else {
          partition_ = value;
        }
      } else {
        partitionBuilder_.mergeFrom(value);
      }
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
     */
    public Builder clearPartition() {
      bitField0_ = (bitField0_ & ~0x00000002);
      partition_ = null;
      if (partitionBuilder_ != null) {
        partitionBuilder_.dispose();
        partitionBuilder_ = null;
      }
      onChanged();
      return this;
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput.Builder getPartitionBuilder() {
      bitField0_ |= 0x00000002;
      onChanged();
      return getPartitionFieldBuilder().getBuilder();
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInputOrBuilder getPartitionOrBuilder() {
      if (partitionBuilder_ != null) {
        return partitionBuilder_.getMessageOrBuilder();
      } else {
        return partition_ == null ?
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput.getDefaultInstance() : partition_;
      }
    }
    /**
     * <code>.gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionInput partition = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInputOrBuilder> 
        getPartitionFieldBuilder() {
      if (partitionBuilder_ == null) {
        partitionBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInputOrBuilder>(
                getPartition(),
                getParentForChildren(),
                isClean());
        partition_ = null;
      }
      return partitionBuilder_;
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


    // @@protoc_insertion_point(builder_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionMapInput)
  }

  // @@protoc_insertion_point(class_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PartitionMapInput)
  private static final gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput();
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PartitionMapInput>
      PARSER = new com.google.protobuf.AbstractParser<PartitionMapInput>() {
    @java.lang.Override
    public PartitionMapInput parsePartialFrom(
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

  public static com.google.protobuf.Parser<PartitionMapInput> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PartitionMapInput> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionMapInput getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
