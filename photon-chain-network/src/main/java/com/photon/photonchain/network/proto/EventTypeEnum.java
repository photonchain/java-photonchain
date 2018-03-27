// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: EventTypeEnum.proto

package com.photon.photonchain.network.proto;

public final class EventTypeEnum {
  private EventTypeEnum() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  /**
   * Protobuf enum {@code EventType}
   */
  public enum EventType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>NEW_BLOCK = 0;</code>
     */
    NEW_BLOCK(0),
    /**
     * <code>NEW_TRANSACTION = 1;</code>
     */
    NEW_TRANSACTION(1),
    /**
     * <code>SYNC_BLOCK = 2;</code>
     */
    SYNC_BLOCK(2),
    /**
     * <code>SYNC_TRANSACTION = 3;</code>
     */
    SYNC_TRANSACTION(3),
    /**
     * <code>NODE_ADDRESS = 4;</code>
     */
    NODE_ADDRESS(4),
    /**
     * <code>SYNC_TOKEN = 5;</code>
     */
    SYNC_TOKEN(5),
    /**
     * <code>NEW_TOKEN = 6;</code>
     */
    NEW_TOKEN(6),
    /**
     * <code>PUSH_MAC = 7;</code>
     */
    PUSH_MAC(7),
    ;

    /**
     * <code>NEW_BLOCK = 0;</code>
     */
    public static final int NEW_BLOCK_VALUE = 0;
    /**
     * <code>NEW_TRANSACTION = 1;</code>
     */
    public static final int NEW_TRANSACTION_VALUE = 1;
    /**
     * <code>SYNC_BLOCK = 2;</code>
     */
    public static final int SYNC_BLOCK_VALUE = 2;
    /**
     * <code>SYNC_TRANSACTION = 3;</code>
     */
    public static final int SYNC_TRANSACTION_VALUE = 3;
    /**
     * <code>NODE_ADDRESS = 4;</code>
     */
    public static final int NODE_ADDRESS_VALUE = 4;
    /**
     * <code>SYNC_TOKEN = 5;</code>
     */
    public static final int SYNC_TOKEN_VALUE = 5;
    /**
     * <code>NEW_TOKEN = 6;</code>
     */
    public static final int NEW_TOKEN_VALUE = 6;
    /**
     * <code>PUSH_MAC = 7;</code>
     */
    public static final int PUSH_MAC_VALUE = 7;


    public final int getNumber() {
      return value;
    }

    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static EventType valueOf(int value) {
      return forNumber(value);
    }

    public static EventType forNumber(int value) {
      switch (value) {
        case 0: return NEW_BLOCK;
        case 1: return NEW_TRANSACTION;
        case 2: return SYNC_BLOCK;
        case 3: return SYNC_TRANSACTION;
        case 4: return NODE_ADDRESS;
        case 5: return SYNC_TOKEN;
        case 6: return NEW_TOKEN;
        case 7: return PUSH_MAC;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<EventType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        EventType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<EventType>() {
            public EventType findValueByNumber(int number) {
              return EventType.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.photon.photonchain.network.proto.EventTypeEnum.getDescriptor().getEnumTypes().get(0);
    }

    private static final EventType[] VALUES = values();

    public static EventType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private EventType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:EventType)
  }


  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023EventTypeEnum.proto*\224\001\n\tEventType\022\r\n\tN" +
      "EW_BLOCK\020\000\022\023\n\017NEW_TRANSACTION\020\001\022\016\n\nSYNC_" +
      "BLOCK\020\002\022\024\n\020SYNC_TRANSACTION\020\003\022\020\n\014NODE_AD" +
      "DRESS\020\004\022\016\n\nSYNC_TOKEN\020\005\022\r\n\tNEW_TOKEN\020\006\022\014" +
      "\n\010PUSH_MAC\020\007B5\n$com.photon.photonchain.n" +
      "etwork.protoB\rEventTypeEnum"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
