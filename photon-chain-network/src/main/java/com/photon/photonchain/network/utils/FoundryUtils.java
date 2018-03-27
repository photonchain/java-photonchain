package com.photon.photonchain.network.utils;

import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.storage.constants.Constants;
import org.spongycastle.util.encoders.Hex;

import java.util.Calendar;

/**
 * @author Wu Created by SKINK on 2018/3/1.
 */

public class FoundryUtils {


    public static int getDiffYear(long genesis, long current) {
        Calendar calendarOne = Calendar.getInstance();
        calendarOne.setTimeInMillis(genesis);
        Calendar calendarTwo = Calendar.getInstance();
        calendarTwo.setTimeInMillis(current);
        int year1 = calendarOne.get(Calendar.YEAR);
        int year2 = calendarTwo.get(Calendar.YEAR);
        return (year2 - year1);
    }

    private static int[] randomExtraction = new int[]{0x0, 0x1, 0x2, 0x0, 0x1, 0x2, 0x0, 0x1, 0x2, 0x0};

    public static long getBlockReward(byte[] publicKey, int diffYear, Long blockHeight, InitializationManager initializationManager, boolean ignoreUnverified) {
        String publickeyHex = Hex.toHexString(publicKey);
        String cardinality = publickeyHex.substring(publickeyHex.length() - 2);
        int magi = Integer.valueOf(cardinality, 16);
        char[] chars = String.valueOf(magi).toCharArray();
        int x = Integer.parseInt(String.valueOf(chars[0]));
        int y = Integer.parseInt(String.valueOf(chars[1]));
        int k = (x * y * blockHeight.intValue()) % 10;
        int diffReward = randomExtraction[k];
        int userReward = 0;
        switch (diffYear) {
            case 0:
                int bestYearOneReward = 14*10;
                userReward = bestYearOneReward - diffReward;
                break;
            case 1:
                int bestYearTwoReward = 8*10;
                userReward = bestYearTwoReward - diffReward;
                break;
            default:
                int bestDefaultReward = 4*10;
                userReward = bestDefaultReward - diffReward;
                break;
        }

        long currentAmount = initializationManager.getTokenAssets(Constants.PTN, ignoreUnverified);

        long canFoundryAmount = Constants.MAX_PTN_AMOUT_UNIT - currentAmount;
        if (canFoundryAmount == 0) return 0;
        if (userReward * Constants.MININUMUNIT > canFoundryAmount) return canFoundryAmount;
        return userReward * Constants.MININUMUNIT;
    }

}
