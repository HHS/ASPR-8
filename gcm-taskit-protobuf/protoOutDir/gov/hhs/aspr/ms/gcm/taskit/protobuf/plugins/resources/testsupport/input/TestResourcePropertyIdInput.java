// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: resources/testsupport.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.testsupport.input;

/**
 * Protobuf enum {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.TestResourcePropertyIdInput}
 */
public enum TestResourcePropertyIdInput
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>ResourceProperty_1_1_BOOLEAN_MUTABLE = 0;</code>
   */
  ResourceProperty_1_1_BOOLEAN_MUTABLE(0),
  /**
   * <code>ResourceProperty_1_2_INTEGER_MUTABLE = 1;</code>
   */
  ResourceProperty_1_2_INTEGER_MUTABLE(1),
  /**
   * <code>ResourceProperty_1_3_DOUBLE_MUTABLE = 2;</code>
   */
  ResourceProperty_1_3_DOUBLE_MUTABLE(2),
  /**
   * <code>ResourceProperty_2_1_BOOLEAN_MUTABLE = 3;</code>
   */
  ResourceProperty_2_1_BOOLEAN_MUTABLE(3),
  /**
   * <code>ResourceProperty_2_2_INTEGER_MUTABLE = 4;</code>
   */
  ResourceProperty_2_2_INTEGER_MUTABLE(4),
  /**
   * <code>ResourceProperty_3_1_BOOLEAN_MUTABLE = 5;</code>
   */
  ResourceProperty_3_1_BOOLEAN_MUTABLE(5),
  /**
   * <code>ResourceProperty_3_2_STRING_MUTABLE = 6;</code>
   */
  ResourceProperty_3_2_STRING_MUTABLE(6),
  /**
   * <code>ResourceProperty_4_1_BOOLEAN_MUTABLE = 7;</code>
   */
  ResourceProperty_4_1_BOOLEAN_MUTABLE(7),
  /**
   * <code>ResourceProperty_5_1_INTEGER_IMMUTABLE = 8;</code>
   */
  ResourceProperty_5_1_INTEGER_IMMUTABLE(8),
  /**
   * <code>ResourceProperty_5_2_DOUBLE_IMMUTABLE = 9;</code>
   */
  ResourceProperty_5_2_DOUBLE_IMMUTABLE(9),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>ResourceProperty_1_1_BOOLEAN_MUTABLE = 0;</code>
   */
  public static final int ResourceProperty_1_1_BOOLEAN_MUTABLE_VALUE = 0;
  /**
   * <code>ResourceProperty_1_2_INTEGER_MUTABLE = 1;</code>
   */
  public static final int ResourceProperty_1_2_INTEGER_MUTABLE_VALUE = 1;
  /**
   * <code>ResourceProperty_1_3_DOUBLE_MUTABLE = 2;</code>
   */
  public static final int ResourceProperty_1_3_DOUBLE_MUTABLE_VALUE = 2;
  /**
   * <code>ResourceProperty_2_1_BOOLEAN_MUTABLE = 3;</code>
   */
  public static final int ResourceProperty_2_1_BOOLEAN_MUTABLE_VALUE = 3;
  /**
   * <code>ResourceProperty_2_2_INTEGER_MUTABLE = 4;</code>
   */
  public static final int ResourceProperty_2_2_INTEGER_MUTABLE_VALUE = 4;
  /**
   * <code>ResourceProperty_3_1_BOOLEAN_MUTABLE = 5;</code>
   */
  public static final int ResourceProperty_3_1_BOOLEAN_MUTABLE_VALUE = 5;
  /**
   * <code>ResourceProperty_3_2_STRING_MUTABLE = 6;</code>
   */
  public static final int ResourceProperty_3_2_STRING_MUTABLE_VALUE = 6;
  /**
   * <code>ResourceProperty_4_1_BOOLEAN_MUTABLE = 7;</code>
   */
  public static final int ResourceProperty_4_1_BOOLEAN_MUTABLE_VALUE = 7;
  /**
   * <code>ResourceProperty_5_1_INTEGER_IMMUTABLE = 8;</code>
   */
  public static final int ResourceProperty_5_1_INTEGER_IMMUTABLE_VALUE = 8;
  /**
   * <code>ResourceProperty_5_2_DOUBLE_IMMUTABLE = 9;</code>
   */
  public static final int ResourceProperty_5_2_DOUBLE_IMMUTABLE_VALUE = 9;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static TestResourcePropertyIdInput valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static TestResourcePropertyIdInput forNumber(int value) {
    switch (value) {
      case 0: return ResourceProperty_1_1_BOOLEAN_MUTABLE;
      case 1: return ResourceProperty_1_2_INTEGER_MUTABLE;
      case 2: return ResourceProperty_1_3_DOUBLE_MUTABLE;
      case 3: return ResourceProperty_2_1_BOOLEAN_MUTABLE;
      case 4: return ResourceProperty_2_2_INTEGER_MUTABLE;
      case 5: return ResourceProperty_3_1_BOOLEAN_MUTABLE;
      case 6: return ResourceProperty_3_2_STRING_MUTABLE;
      case 7: return ResourceProperty_4_1_BOOLEAN_MUTABLE;
      case 8: return ResourceProperty_5_1_INTEGER_IMMUTABLE;
      case 9: return ResourceProperty_5_2_DOUBLE_IMMUTABLE;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<TestResourcePropertyIdInput>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      TestResourcePropertyIdInput> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<TestResourcePropertyIdInput>() {
          public TestResourcePropertyIdInput findValueByNumber(int number) {
            return TestResourcePropertyIdInput.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalStateException(
          "Can't get the descriptor of an unrecognized enum value.");
    }
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.testsupport.input.Testsupport.getDescriptor().getEnumTypes().get(0);
  }

  private static final TestResourcePropertyIdInput[] VALUES = values();

  public static TestResourcePropertyIdInput valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private TestResourcePropertyIdInput(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.TestResourcePropertyIdInput)
}
