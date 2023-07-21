// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: groups/testsupport.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.testsupport.input;

/**
 * Protobuf enum {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.TestGroupPropertyIdInput}
 */
public enum TestGroupPropertyIdInput
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK = 0;</code>
   */
  GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK(0),
  /**
   * <code>GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK = 1;</code>
   */
  GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK(1),
  /**
   * <code>GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK = 2;</code>
   */
  GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK(2),
  /**
   * <code>GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK = 3;</code>
   */
  GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK(3),
  /**
   * <code>GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK = 4;</code>
   */
  GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK(4),
  /**
   * <code>GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK = 5;</code>
   */
  GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK(5),
  /**
   * <code>GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK = 6;</code>
   */
  GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK(6),
  /**
   * <code>GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK = 7;</code>
   */
  GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK(7),
  /**
   * <code>GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK = 8;</code>
   */
  GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK(8),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK = 0;</code>
   */
  public static final int GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK_VALUE = 0;
  /**
   * <code>GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK = 1;</code>
   */
  public static final int GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK_VALUE = 1;
  /**
   * <code>GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK = 2;</code>
   */
  public static final int GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK_VALUE = 2;
  /**
   * <code>GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK = 3;</code>
   */
  public static final int GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK_VALUE = 3;
  /**
   * <code>GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK = 4;</code>
   */
  public static final int GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK_VALUE = 4;
  /**
   * <code>GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK = 5;</code>
   */
  public static final int GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK_VALUE = 5;
  /**
   * <code>GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK = 6;</code>
   */
  public static final int GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK_VALUE = 6;
  /**
   * <code>GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK = 7;</code>
   */
  public static final int GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK_VALUE = 7;
  /**
   * <code>GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK = 8;</code>
   */
  public static final int GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK_VALUE = 8;


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
  public static TestGroupPropertyIdInput valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static TestGroupPropertyIdInput forNumber(int value) {
    switch (value) {
      case 0: return GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
      case 1: return GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
      case 2: return GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK;
      case 3: return GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK;
      case 4: return GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK;
      case 5: return GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;
      case 6: return GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK;
      case 7: return GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK;
      case 8: return GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<TestGroupPropertyIdInput>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      TestGroupPropertyIdInput> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<TestGroupPropertyIdInput>() {
          public TestGroupPropertyIdInput findValueByNumber(int number) {
            return TestGroupPropertyIdInput.forNumber(number);
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
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.testsupport.input.Testsupport.getDescriptor().getEnumTypes().get(1);
  }

  private static final TestGroupPropertyIdInput[] VALUES = values();

  public static TestGroupPropertyIdInput valueOf(
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

  private TestGroupPropertyIdInput(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.TestGroupPropertyIdInput)
}

