package com.photon.photonchain.storage.constants;

import java.math.BigInteger;

public class Constants {
    public static final int MAGIC_NO = 0x133c9c4;
    public static final BigInteger CUMULATIVE_DIFFICULTY = BigInteger.ZERO;
    public static final int BLOCK_VERSION = 1;
    public static final String SYNC_BLOCK_HEIGHT = "SYNC_BLOCK_HEIGHT";
    public static final String SYNC_BLOCK_LIST = "SYNC_BLOCK_LIST";
    public static final String SYNC_TRANSACTION_LIST = "SYNC_TRANSACTION_LIST";
    public static final String SYNC_TRANSACTION_ID = "SYNC_TRANSACTION_ID";
    public static final String TOTAL_INCOME = "TOTAL_INCOME";
    public static final String TOTAL_EXPENDITURE = "TOTAL_EXPENDITURE";
    public static final String TOTAL_EFFECTIVE_INCOME = "TOTAL_EFFECTIVE_INCOME";
    public static final String SYNC_MAC_ADDRESS = "SYNC_MAC_ADDRESS";
    public static final int BLOCK_INTERVAL = 0x2710;
    public static final long MININUMUNIT = 1000000;
    public static final String ADDRESS_PREFIX = "px";
    public static final String MINI_UNIT = "MINI_UNIT";
    public static final String MAX_UNIT = "MAX_UNIT";
    public static final String PTN = "ptn";
    public static final int BLOCK_TRANSACTION_SIZE = 0xFF;
    public static final String PUBKEY = "pubKey";
    public static final String PRIKEY = "priKey";
    public static final String PWD = "pwd";
    public static final String PUBKEY_FLAG = " pubKey ";
    public static final String PRIKEY_FLAG = " priKey ";
    public static final String PWD_FLAG = " pwd ";
    public static final String ADDRESS = "address";
    public static final Long MAX_PTN_AMOUNT = 5000000000L;
    public static final Long MAX_PTN_AMOUT_UNIT = MAX_PTN_AMOUNT * MININUMUNIT;
    public static final int FORGABLE_NODES = 2;
    public static final int SYNC_SIZE=0x7d0;
}
