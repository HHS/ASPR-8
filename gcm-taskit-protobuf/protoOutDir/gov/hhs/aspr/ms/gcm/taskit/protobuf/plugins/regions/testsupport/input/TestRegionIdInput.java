// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: regions/testsupport.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.testsupport.input;

/**
 * Protobuf enum {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.TestRegionIdInput}
 */
public enum TestRegionIdInput
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>REGION_1 = 0;</code>
   */
  REGION_1(0),
  /**
   * <code>REGION_2 = 1;</code>
   */
  REGION_2(1),
  /**
   * <code>REGION_3 = 2;</code>
   */
  REGION_3(2),
  /**
   * <code>REGION_4 = 3;</code>
   */
  REGION_4(3),
  /**
   * <code>REGION_5 = 4;</code>
   */
  REGION_5(4),
  /**
   * <code>REGION_6 = 5;</code>
   */
  REGION_6(5),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>REGION_1 = 0;</code>
   */
  public static final int REGION_1_VALUE = 0;
  /**
   * <code>REGION_2 = 1;</code>
   */
  public static final int REGION_2_VALUE = 1;
  /**
   * <code>REGION_3 = 2;</code>
   */
  public static final int REGION_3_VALUE = 2;
  /**
   * <code>REGION_4 = 3;</code>
   */
  public static final int REGION_4_VALUE = 3;
  /**
   * <code>REGION_5 = 4;</code>
   */
  public static final int REGION_5_VALUE = 4;
  /**
   * <code>REGION_6 = 5;</code>
   */
  public static final int REGION_6_VALUE = 5;


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
  public static TestRegionIdInput valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static TestRegionIdInput forNumber(int value) {
    switch (value) {
      case 0: return REGION_1;
      case 1: return REGION_2;
      case 2: return REGION_3;
      case 3: return REGION_4;
      case 4: return REGION_5;
      case 5: return REGION_6;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<TestRegionIdInput>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      TestRegionIdInput> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<TestRegionIdInput>() {
          public TestRegionIdInput findValueByNumber(int number) {
            return TestRegionIdInput.forNumber(number);
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
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.testsupport.input.Testsupport.getDescriptor().getEnumTypes().get(0);
  }

  private static final TestRegionIdInput[] VALUES = values();

  public static TestRegionIdInput valueOf(
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

  private TestRegionIdInput(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.TestRegionIdInput)
}
