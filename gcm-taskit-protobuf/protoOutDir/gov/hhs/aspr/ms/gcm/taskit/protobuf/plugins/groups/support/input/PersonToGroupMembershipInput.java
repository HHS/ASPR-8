// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: groups/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input;

/**
 * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonToGroupMembershipInput}
 */
public final class PersonToGroupMembershipInput extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonToGroupMembershipInput)
    PersonToGroupMembershipInputOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PersonToGroupMembershipInput.newBuilder() to construct.
  private PersonToGroupMembershipInput(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PersonToGroupMembershipInput() {
    groupIds_ = emptyIntList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new PersonToGroupMembershipInput();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonToGroupMembershipInput_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonToGroupMembershipInput_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput.Builder.class);
  }

  public static final int PERSONID_FIELD_NUMBER = 1;
  private int personId_ = 0;
  /**
   * <code>int32 personId = 1;</code>
   * @return The personId.
   */
  @java.lang.Override
  public int getPersonId() {
    return personId_;
  }

  public static final int GROUPIDS_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private com.google.protobuf.Internal.IntList groupIds_;
  /**
   * <code>repeated int32 groupIds = 2;</code>
   * @return A list containing the groupIds.
   */
  @java.lang.Override
  public java.util.List<java.lang.Integer>
      getGroupIdsList() {
    return groupIds_;
  }
  /**
   * <code>repeated int32 groupIds = 2;</code>
   * @return The count of groupIds.
   */
  public int getGroupIdsCount() {
    return groupIds_.size();
  }
  /**
   * <code>repeated int32 groupIds = 2;</code>
   * @param index The index of the element to return.
   * @return The groupIds at the given index.
   */
  public int getGroupIds(int index) {
    return groupIds_.getInt(index);
  }
  private int groupIdsMemoizedSerializedSize = -1;

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
    getSerializedSize();
    if (personId_ != 0) {
      output.writeInt32(1, personId_);
    }
    if (getGroupIdsList().size() > 0) {
      output.writeUInt32NoTag(18);
      output.writeUInt32NoTag(groupIdsMemoizedSerializedSize);
    }
    for (int i = 0; i < groupIds_.size(); i++) {
      output.writeInt32NoTag(groupIds_.getInt(i));
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (personId_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(1, personId_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < groupIds_.size(); i++) {
        dataSize += com.google.protobuf.CodedOutputStream
          .computeInt32SizeNoTag(groupIds_.getInt(i));
      }
      size += dataSize;
      if (!getGroupIdsList().isEmpty()) {
        size += 1;
        size += com.google.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(dataSize);
      }
      groupIdsMemoizedSerializedSize = dataSize;
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
    if (!(obj instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput)) {
      return super.equals(obj);
    }
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput other = (gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput) obj;

    if (getPersonId()
        != other.getPersonId()) return false;
    if (!getGroupIdsList()
        .equals(other.getGroupIdsList())) return false;
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
    hash = (37 * hash) + PERSONID_FIELD_NUMBER;
    hash = (53 * hash) + getPersonId();
    if (getGroupIdsCount() > 0) {
      hash = (37 * hash) + GROUPIDS_FIELD_NUMBER;
      hash = (53 * hash) + getGroupIdsList().hashCode();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput parseFrom(
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
  public static Builder newBuilder(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput prototype) {
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
   * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonToGroupMembershipInput}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonToGroupMembershipInput)
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInputOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonToGroupMembershipInput_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonToGroupMembershipInput_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput.Builder.class);
    }

    // Construct using gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput.newBuilder()
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
      personId_ = 0;
      groupIds_ = emptyIntList();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PersonToGroupMembershipInput_descriptor;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput getDefaultInstanceForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput.getDefaultInstance();
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput build() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput buildPartial() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput result = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput result) {
      if (((bitField0_ & 0x00000002) != 0)) {
        groupIds_.makeImmutable();
        bitField0_ = (bitField0_ & ~0x00000002);
      }
      result.groupIds_ = groupIds_;
    }

    private void buildPartial0(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.personId_ = personId_;
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
      if (other instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput) {
        return mergeFrom((gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput other) {
      if (other == gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput.getDefaultInstance()) return this;
      if (other.getPersonId() != 0) {
        setPersonId(other.getPersonId());
      }
      if (!other.groupIds_.isEmpty()) {
        if (groupIds_.isEmpty()) {
          groupIds_ = other.groupIds_;
          bitField0_ = (bitField0_ & ~0x00000002);
        } else {
          ensureGroupIdsIsMutable();
          groupIds_.addAll(other.groupIds_);
        }
        onChanged();
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
            case 8: {
              personId_ = input.readInt32();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 16: {
              int v = input.readInt32();
              ensureGroupIdsIsMutable();
              groupIds_.addInt(v);
              break;
            } // case 16
            case 18: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              ensureGroupIdsIsMutable();
              while (input.getBytesUntilLimit() > 0) {
                groupIds_.addInt(input.readInt32());
              }
              input.popLimit(limit);
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

    private int personId_ ;
    /**
     * <code>int32 personId = 1;</code>
     * @return The personId.
     */
    @java.lang.Override
    public int getPersonId() {
      return personId_;
    }
    /**
     * <code>int32 personId = 1;</code>
     * @param value The personId to set.
     * @return This builder for chaining.
     */
    public Builder setPersonId(int value) {
      
      personId_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>int32 personId = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearPersonId() {
      bitField0_ = (bitField0_ & ~0x00000001);
      personId_ = 0;
      onChanged();
      return this;
    }

    private com.google.protobuf.Internal.IntList groupIds_ = emptyIntList();
    private void ensureGroupIdsIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        groupIds_ = mutableCopy(groupIds_);
        bitField0_ |= 0x00000002;
      }
    }
    /**
     * <code>repeated int32 groupIds = 2;</code>
     * @return A list containing the groupIds.
     */
    public java.util.List<java.lang.Integer>
        getGroupIdsList() {
      return ((bitField0_ & 0x00000002) != 0) ?
               java.util.Collections.unmodifiableList(groupIds_) : groupIds_;
    }
    /**
     * <code>repeated int32 groupIds = 2;</code>
     * @return The count of groupIds.
     */
    public int getGroupIdsCount() {
      return groupIds_.size();
    }
    /**
     * <code>repeated int32 groupIds = 2;</code>
     * @param index The index of the element to return.
     * @return The groupIds at the given index.
     */
    public int getGroupIds(int index) {
      return groupIds_.getInt(index);
    }
    /**
     * <code>repeated int32 groupIds = 2;</code>
     * @param index The index to set the value at.
     * @param value The groupIds to set.
     * @return This builder for chaining.
     */
    public Builder setGroupIds(
        int index, int value) {
      
      ensureGroupIdsIsMutable();
      groupIds_.setInt(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 groupIds = 2;</code>
     * @param value The groupIds to add.
     * @return This builder for chaining.
     */
    public Builder addGroupIds(int value) {
      
      ensureGroupIdsIsMutable();
      groupIds_.addInt(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 groupIds = 2;</code>
     * @param values The groupIds to add.
     * @return This builder for chaining.
     */
    public Builder addAllGroupIds(
        java.lang.Iterable<? extends java.lang.Integer> values) {
      ensureGroupIdsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, groupIds_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 groupIds = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearGroupIds() {
      groupIds_ = emptyIntList();
      bitField0_ = (bitField0_ & ~0x00000002);
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


    // @@protoc_insertion_point(builder_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonToGroupMembershipInput)
  }

  // @@protoc_insertion_point(class_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonToGroupMembershipInput)
  private static final gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput();
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PersonToGroupMembershipInput>
      PARSER = new com.google.protobuf.AbstractParser<PersonToGroupMembershipInput>() {
    @java.lang.Override
    public PersonToGroupMembershipInput parsePartialFrom(
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

  public static com.google.protobuf.Parser<PersonToGroupMembershipInput> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PersonToGroupMembershipInput> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

