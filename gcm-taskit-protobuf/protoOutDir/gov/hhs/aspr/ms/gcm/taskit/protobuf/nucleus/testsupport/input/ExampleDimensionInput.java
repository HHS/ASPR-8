// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: nucleus/testsupport.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input;

/**
 * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.ExampleDimensionInput}
 */
public final class ExampleDimensionInput extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.ExampleDimensionInput)
    ExampleDimensionInputOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ExampleDimensionInput.newBuilder() to construct.
  private ExampleDimensionInput(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ExampleDimensionInput() {
    levelName_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ExampleDimensionInput();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.Testsupport.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExampleDimensionInput_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.Testsupport.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExampleDimensionInput_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput.Builder.class);
  }

  public static final int LEVELNAME_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private volatile java.lang.Object levelName_ = "";
  /**
   * <code>string levelName = 1;</code>
   * @return The levelName.
   */
  @java.lang.Override
  public java.lang.String getLevelName() {
    java.lang.Object ref = levelName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      levelName_ = s;
      return s;
    }
  }
  /**
   * <code>string levelName = 1;</code>
   * @return The bytes for levelName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getLevelNameBytes() {
    java.lang.Object ref = levelName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      levelName_ = b;
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
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(levelName_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, levelName_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(levelName_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, levelName_);
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
    if (!(obj instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput)) {
      return super.equals(obj);
    }
    gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput other = (gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput) obj;

    if (!getLevelName()
        .equals(other.getLevelName())) return false;
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
    hash = (37 * hash) + LEVELNAME_FIELD_NUMBER;
    hash = (53 * hash) + getLevelName().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput parseFrom(
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
  public static Builder newBuilder(gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput prototype) {
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
   * Protobuf type {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.ExampleDimensionInput}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.ExampleDimensionInput)
      gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInputOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.Testsupport.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExampleDimensionInput_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.Testsupport.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExampleDimensionInput_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput.class, gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput.Builder.class);
    }

    // Construct using gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput.newBuilder()
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
      levelName_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.Testsupport.internal_static_gov_hhs_aspr_ms_gcm_taskit_protobuf_nucleus_ExampleDimensionInput_descriptor;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput getDefaultInstanceForType() {
      return gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput.getDefaultInstance();
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput build() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput buildPartial() {
      gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput result = new gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.levelName_ = levelName_;
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
      if (other instanceof gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput) {
        return mergeFrom((gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput other) {
      if (other == gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput.getDefaultInstance()) return this;
      if (!other.getLevelName().isEmpty()) {
        levelName_ = other.levelName_;
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
              levelName_ = input.readStringRequireUtf8();
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

    private java.lang.Object levelName_ = "";
    /**
     * <code>string levelName = 1;</code>
     * @return The levelName.
     */
    public java.lang.String getLevelName() {
      java.lang.Object ref = levelName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        levelName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string levelName = 1;</code>
     * @return The bytes for levelName.
     */
    public com.google.protobuf.ByteString
        getLevelNameBytes() {
      java.lang.Object ref = levelName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        levelName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string levelName = 1;</code>
     * @param value The levelName to set.
     * @return This builder for chaining.
     */
    public Builder setLevelName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      levelName_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>string levelName = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearLevelName() {
      levelName_ = getDefaultInstance().getLevelName();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>string levelName = 1;</code>
     * @param value The bytes for levelName to set.
     * @return This builder for chaining.
     */
    public Builder setLevelNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      levelName_ = value;
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


    // @@protoc_insertion_point(builder_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.ExampleDimensionInput)
  }

  // @@protoc_insertion_point(class_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.ExampleDimensionInput)
  private static final gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput();
  }

  public static gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ExampleDimensionInput>
      PARSER = new com.google.protobuf.AbstractParser<ExampleDimensionInput>() {
    @java.lang.Override
    public ExampleDimensionInput parsePartialFrom(
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

  public static com.google.protobuf.Parser<ExampleDimensionInput> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ExampleDimensionInput> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.input.ExampleDimensionInput getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

