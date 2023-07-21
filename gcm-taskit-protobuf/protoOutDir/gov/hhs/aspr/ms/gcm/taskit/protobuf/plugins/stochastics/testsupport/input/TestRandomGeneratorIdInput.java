// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: stochastics/testsupport.proto

package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.testsupport.input;

/**
 * Protobuf enum {@code gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.TestRandomGeneratorIdInput}
 */
public enum TestRandomGeneratorIdInput
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>DASHER = 0;</code>
   */
  DASHER(0),
  /**
   * <code>DANCER = 1;</code>
   */
  DANCER(1),
  /**
   * <code>PRANCER = 2;</code>
   */
  PRANCER(2),
  /**
   * <code>VIXEN = 3;</code>
   */
  VIXEN(3),
  /**
   * <code>COMET = 4;</code>
   */
  COMET(4),
  /**
   * <code>CUPID = 5;</code>
   */
  CUPID(5),
  /**
   * <code>DONNER = 6;</code>
   */
  DONNER(6),
  /**
   * <code>BLITZEN = 7;</code>
   */
  BLITZEN(7),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>DASHER = 0;</code>
   */
  public static final int DASHER_VALUE = 0;
  /**
   * <code>DANCER = 1;</code>
   */
  public static final int DANCER_VALUE = 1;
  /**
   * <code>PRANCER = 2;</code>
   */
  public static final int PRANCER_VALUE = 2;
  /**
   * <code>VIXEN = 3;</code>
   */
  public static final int VIXEN_VALUE = 3;
  /**
   * <code>COMET = 4;</code>
   */
  public static final int COMET_VALUE = 4;
  /**
   * <code>CUPID = 5;</code>
   */
  public static final int CUPID_VALUE = 5;
  /**
   * <code>DONNER = 6;</code>
   */
  public static final int DONNER_VALUE = 6;
  /**
   * <code>BLITZEN = 7;</code>
   */
  public static final int BLITZEN_VALUE = 7;


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
  public static TestRandomGeneratorIdInput valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static TestRandomGeneratorIdInput forNumber(int value) {
    switch (value) {
      case 0: return DASHER;
      case 1: return DANCER;
      case 2: return PRANCER;
      case 3: return VIXEN;
      case 4: return COMET;
      case 5: return CUPID;
      case 6: return DONNER;
      case 7: return BLITZEN;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<TestRandomGeneratorIdInput>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      TestRandomGeneratorIdInput> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<TestRandomGeneratorIdInput>() {
          public TestRandomGeneratorIdInput findValueByNumber(int number) {
            return TestRandomGeneratorIdInput.forNumber(number);
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
    return gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.testsupport.input.Testsupport.getDescriptor().getEnumTypes().get(0);
  }

  private static final TestRandomGeneratorIdInput[] VALUES = values();

  public static TestRandomGeneratorIdInput valueOf(
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

  private TestRandomGeneratorIdInput(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.TestRandomGeneratorIdInput)
}

