// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: plugins/people/data.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input;

/**
 * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PeoplePluginDataInput}
 */
public final class PeoplePluginDataInput extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PeoplePluginDataInput)
    PeoplePluginDataInputOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PeoplePluginDataInput.newBuilder() to construct.
  private PeoplePluginDataInput(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PeoplePluginDataInput() {
    personRanges_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new PeoplePluginDataInput();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.Data.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PeoplePluginDataInput_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.Data.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PeoplePluginDataInput_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput.Builder.class);
  }

  private int bitField0_;
  public static final int PERSONRANGES_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput> personRanges_;
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
   */
  @java.lang.Override
  public java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput> getPersonRangesList() {
    return personRanges_;
  }
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
   */
  @java.lang.Override
  public java.util.List<? extends gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInputOrBuilder> 
      getPersonRangesOrBuilderList() {
    return personRanges_;
  }
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
   */
  @java.lang.Override
  public int getPersonRangesCount() {
    return personRanges_.size();
  }
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
   */
  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput getPersonRanges(int index) {
    return personRanges_.get(index);
  }
  /**
   * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
   */
  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInputOrBuilder getPersonRangesOrBuilder(
      int index) {
    return personRanges_.get(index);
  }

  public static final int PERSONCOUNT_FIELD_NUMBER = 2;
  private int personCount_ = 0;
  /**
   * <code>optional int32 personCount = 2;</code>
   * @return Whether the personCount field is set.
   */
  @java.lang.Override
  public boolean hasPersonCount() {
    return ((bitField0_ & 0x00000001) != 0);
  }
  /**
   * <code>optional int32 personCount = 2;</code>
   * @return The personCount.
   */
  @java.lang.Override
  public int getPersonCount() {
    return personCount_;
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
    for (int i = 0; i < personRanges_.size(); i++) {
      output.writeMessage(1, personRanges_.get(i));
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      output.writeInt32(2, personCount_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < personRanges_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, personRanges_.get(i));
    }
    if (((bitField0_ & 0x00000001) != 0)) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, personCount_);
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
    if (!(obj instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput)) {
      return super.equals(obj);
    }
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput other = (gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput) obj;

    if (!getPersonRangesList()
        .equals(other.getPersonRangesList())) return false;
    if (hasPersonCount() != other.hasPersonCount()) return false;
    if (hasPersonCount()) {
      if (getPersonCount()
          != other.getPersonCount()) return false;
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
    if (getPersonRangesCount() > 0) {
      hash = (37 * hash) + PERSONRANGES_FIELD_NUMBER;
      hash = (53 * hash) + getPersonRangesList().hashCode();
    }
    if (hasPersonCount()) {
      hash = (37 * hash) + PERSONCOUNT_FIELD_NUMBER;
      hash = (53 * hash) + getPersonCount();
    }
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput parseFrom(
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
  public static Builder newBuilder(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput prototype) {
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
   * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PeoplePluginDataInput}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PeoplePluginDataInput)
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInputOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.Data.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PeoplePluginDataInput_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.Data.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PeoplePluginDataInput_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput.Builder.class);
    }

    // Construct using gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput.newBuilder()
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
      if (personRangesBuilder_ == null) {
        personRanges_ = java.util.Collections.emptyList();
      } else {
        personRanges_ = null;
        personRangesBuilder_.clear();
      }
      bitField0_ = (bitField0_ & ~0x00000001);
      personCount_ = 0;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.Data.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_PeoplePluginDataInput_descriptor;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput getDefaultInstanceForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput.getDefaultInstance();
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput build() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput buildPartial() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput result = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput result) {
      if (personRangesBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          personRanges_ = java.util.Collections.unmodifiableList(personRanges_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.personRanges_ = personRanges_;
      } else {
        result.personRanges_ = personRangesBuilder_.build();
      }
    }

    private void buildPartial0(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput result) {
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.personCount_ = personCount_;
        to_bitField0_ |= 0x00000001;
      }
      result.bitField0_ |= to_bitField0_;
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
      if (other instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput) {
        return mergeFrom((gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput other) {
      if (other == gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput.getDefaultInstance()) return this;
      if (personRangesBuilder_ == null) {
        if (!other.personRanges_.isEmpty()) {
          if (personRanges_.isEmpty()) {
            personRanges_ = other.personRanges_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensurePersonRangesIsMutable();
            personRanges_.addAll(other.personRanges_);
          }
          onChanged();
        }
      } else {
        if (!other.personRanges_.isEmpty()) {
          if (personRangesBuilder_.isEmpty()) {
            personRangesBuilder_.dispose();
            personRangesBuilder_ = null;
            personRanges_ = other.personRanges_;
            bitField0_ = (bitField0_ & ~0x00000001);
            personRangesBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getPersonRangesFieldBuilder() : null;
          } else {
            personRangesBuilder_.addAllMessages(other.personRanges_);
          }
        }
      }
      if (other.hasPersonCount()) {
        setPersonCount(other.getPersonCount());
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
              gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput m =
                  input.readMessage(
                      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.parser(),
                      extensionRegistry);
              if (personRangesBuilder_ == null) {
                ensurePersonRangesIsMutable();
                personRanges_.add(m);
              } else {
                personRangesBuilder_.addMessage(m);
              }
              break;
            } // case 10
            case 16: {
              personCount_ = input.readInt32();
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

    private java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput> personRanges_ =
      java.util.Collections.emptyList();
    private void ensurePersonRangesIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        personRanges_ = new java.util.ArrayList<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput>(personRanges_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInputOrBuilder> personRangesBuilder_;

    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput> getPersonRangesList() {
      if (personRangesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(personRanges_);
      } else {
        return personRangesBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public int getPersonRangesCount() {
      if (personRangesBuilder_ == null) {
        return personRanges_.size();
      } else {
        return personRangesBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput getPersonRanges(int index) {
      if (personRangesBuilder_ == null) {
        return personRanges_.get(index);
      } else {
        return personRangesBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public Builder setPersonRanges(
        int index, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput value) {
      if (personRangesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePersonRangesIsMutable();
        personRanges_.set(index, value);
        onChanged();
      } else {
        personRangesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public Builder setPersonRanges(
        int index, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.Builder builderForValue) {
      if (personRangesBuilder_ == null) {
        ensurePersonRangesIsMutable();
        personRanges_.set(index, builderForValue.build());
        onChanged();
      } else {
        personRangesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public Builder addPersonRanges(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput value) {
      if (personRangesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePersonRangesIsMutable();
        personRanges_.add(value);
        onChanged();
      } else {
        personRangesBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public Builder addPersonRanges(
        int index, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput value) {
      if (personRangesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensurePersonRangesIsMutable();
        personRanges_.add(index, value);
        onChanged();
      } else {
        personRangesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public Builder addPersonRanges(
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.Builder builderForValue) {
      if (personRangesBuilder_ == null) {
        ensurePersonRangesIsMutable();
        personRanges_.add(builderForValue.build());
        onChanged();
      } else {
        personRangesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public Builder addPersonRanges(
        int index, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.Builder builderForValue) {
      if (personRangesBuilder_ == null) {
        ensurePersonRangesIsMutable();
        personRanges_.add(index, builderForValue.build());
        onChanged();
      } else {
        personRangesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public Builder addAllPersonRanges(
        java.lang.Iterable<? extends gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput> values) {
      if (personRangesBuilder_ == null) {
        ensurePersonRangesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, personRanges_);
        onChanged();
      } else {
        personRangesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public Builder clearPersonRanges() {
      if (personRangesBuilder_ == null) {
        personRanges_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        personRangesBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public Builder removePersonRanges(int index) {
      if (personRangesBuilder_ == null) {
        ensurePersonRangesIsMutable();
        personRanges_.remove(index);
        onChanged();
      } else {
        personRangesBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.Builder getPersonRangesBuilder(
        int index) {
      return getPersonRangesFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInputOrBuilder getPersonRangesOrBuilder(
        int index) {
      if (personRangesBuilder_ == null) {
        return personRanges_.get(index);  } else {
        return personRangesBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public java.util.List<? extends gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInputOrBuilder> 
         getPersonRangesOrBuilderList() {
      if (personRangesBuilder_ != null) {
        return personRangesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(personRanges_);
      }
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.Builder addPersonRangesBuilder() {
      return getPersonRangesFieldBuilder().addBuilder(
          gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.getDefaultInstance());
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.Builder addPersonRangesBuilder(
        int index) {
      return getPersonRangesFieldBuilder().addBuilder(
          index, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.getDefaultInstance());
    }
    /**
     * <code>repeated .gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PersonRangeInput personRanges = 1;</code>
     */
    public java.util.List<gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.Builder> 
         getPersonRangesBuilderList() {
      return getPersonRangesFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInputOrBuilder> 
        getPersonRangesFieldBuilder() {
      if (personRangesBuilder_ == null) {
        personRangesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInput.Builder, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonRangeInputOrBuilder>(
                personRanges_,
                ((bitField0_ & 0x00000001) != 0),
                getParentForChildren(),
                isClean());
        personRanges_ = null;
      }
      return personRangesBuilder_;
    }

    private int personCount_ ;
    /**
     * <code>optional int32 personCount = 2;</code>
     * @return Whether the personCount field is set.
     */
    @java.lang.Override
    public boolean hasPersonCount() {
      return ((bitField0_ & 0x00000002) != 0);
    }
    /**
     * <code>optional int32 personCount = 2;</code>
     * @return The personCount.
     */
    @java.lang.Override
    public int getPersonCount() {
      return personCount_;
    }
    /**
     * <code>optional int32 personCount = 2;</code>
     * @param value The personCount to set.
     * @return This builder for chaining.
     */
    public Builder setPersonCount(int value) {
      
      personCount_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>optional int32 personCount = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearPersonCount() {
      bitField0_ = (bitField0_ & ~0x00000002);
      personCount_ = 0;
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


    // @@protoc_insertion_point(builder_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PeoplePluginDataInput)
  }

  // @@protoc_insertion_point(class_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.PeoplePluginDataInput)
  private static final gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput();
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PeoplePluginDataInput>
      PARSER = new com.google.protobuf.AbstractParser<PeoplePluginDataInput>() {
    @java.lang.Override
    public PeoplePluginDataInput parsePartialFrom(
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

  public static com.google.protobuf.Parser<PeoplePluginDataInput> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PeoplePluginDataInput> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
