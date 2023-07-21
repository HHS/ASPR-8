// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: groups/support.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input;

/**
 * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.SimpleGroupTypeIdInput}
 */
public final class SimpleGroupTypeIdInput extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.SimpleGroupTypeIdInput)
    SimpleGroupTypeIdInputOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SimpleGroupTypeIdInput.newBuilder() to construct.
  private SimpleGroupTypeIdInput(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SimpleGroupTypeIdInput() {
    value_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SimpleGroupTypeIdInput();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleGroupTypeIdInput_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleGroupTypeIdInput_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput.Builder.class);
  }

  public static final int VALUE_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object value_ = "";
  /**
   * <code>string value = 1;</code>
   * @return The value.
   */
  @java.lang.Override
  public java.lang.String getValue() {
    java.lang.Object ref = value_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      value_ = s;
      return s;
    }
  }
  /**
   * <code>string value = 1;</code>
   * @return The bytes for value.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getValueBytes() {
    java.lang.Object ref = value_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      value_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(value_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, value_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(value_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, value_);
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
    if (!(obj instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput)) {
      return super.equals(obj);
    }
    gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput other = (gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput) obj;

    if (!getValue()
        .equals(other.getValue())) return false;
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
    hash = (37 * hash) + VALUE_FIELD_NUMBER;
    hash = (53 * hash) + getValue().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput parseFrom(
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
  public static Builder newBuilder(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput prototype) {
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
   * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.SimpleGroupTypeIdInput}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.SimpleGroupTypeIdInput)
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInputOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleGroupTypeIdInput_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleGroupTypeIdInput_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput.Builder.class);
    }

    // Construct using gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput.newBuilder()
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
      value_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.Support.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_plugins_SimpleGroupTypeIdInput_descriptor;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput getDefaultInstanceForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput.getDefaultInstance();
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput build() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput buildPartial() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput result = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.value_ = value_;
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
      if (other instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput) {
        return mergeFrom((gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput other) {
      if (other == gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput.getDefaultInstance()) return this;
      if (!other.getValue().isEmpty()) {
        value_ = other.value_;
        bitField0_ |= 0x00000001;
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
            case 10: {
              value_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000001;
              break;
            } // case 10
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

    private java.lang.Object value_ = "";
    /**
     * <code>string value = 1;</code>
     * @return The value.
     */
    public java.lang.String getValue() {
      java.lang.Object ref = value_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        value_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string value = 1;</code>
     * @return The bytes for value.
     */
    public com.google.protobuf.ByteString
        getValueBytes() {
      java.lang.Object ref = value_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        value_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string value = 1;</code>
     * @param value The value to set.
     * @return This builder for chaining.
     */
    public Builder setValue(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      value_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string value = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearValue() {
      value_ = getDefaultInstance().getValue();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string value = 1;</code>
     * @param value The bytes for value to set.
     * @return This builder for chaining.
     */
    public Builder setValueBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      value_ = value;
      bitField0_ |= 0x00000001;
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


    // @@protoc_insertion_point(builder_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.SimpleGroupTypeIdInput)
  }

  // @@protoc_insertion_point(class_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.SimpleGroupTypeIdInput)
  private static final gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput();
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SimpleGroupTypeIdInput>
      PARSER = new com.google.protobuf.AbstractParser<SimpleGroupTypeIdInput>() {
    @java.lang.Override
    public SimpleGroupTypeIdInput parsePartialFrom(
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

  public static com.google.protobuf.Parser<SimpleGroupTypeIdInput> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SimpleGroupTypeIdInput> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.SimpleGroupTypeIdInput getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

