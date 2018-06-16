package com.photon.photonchain.network.utils;

import com.photon.photonchain.network.ehcacheManager.AssetsManager;
import com.photon.photonchain.network.ehcacheManager.FoundryMachineManager;
import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.network.ehcacheManager.NioSocketChannelManager;
import com.photon.photonchain.storage.constants.Constants;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author PTN Created by PTN on 2018/3/1.
 */

@Component
public class FoundryUtils {
    @Autowired
    private FoundryMachineManager foundryMachineManager;
    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;
    @Autowired
    private AssetsManager assetsManager;

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FoundryUtils.class);

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
        int y = 0;
        if (chars.length == 1) {
            y = Integer.parseInt(String.valueOf(1));
        } else {
            y = Integer.parseInt(String.valueOf(chars[1]));
        }
        int k = (x * y * blockHeight.intValue()) % 10;
        int diffReward = randomExtraction[k];
        int userReward = 0;
        switch (diffYear) {
            case 0:
                int bestYearOneReward = 14 * 10;
                userReward = bestYearOneReward - diffReward;
                break;
            case 1:
                int bestYearTwoReward = 8 * 10;
                userReward = bestYearTwoReward - diffReward;
                break;
            default:
                int bestDefaultReward = 4 * 10;
                userReward = bestDefaultReward - diffReward;
                break;
        }
        long currentAmount = initializationManager.getTokenAssets(Constants.PTN, ignoreUnverified);
        long canFoundryAmount = Constants.MAX_PTN_AMOUT_UNIT - currentAmount;
        if (canFoundryAmount == 0) return 0;
        if (userReward * Constants.MININUMUNIT > canFoundryAmount) return canFoundryAmount;
        return userReward * Constants.MININUMUNIT;
    }


    public static Map<String, Integer> getSortingMap(Map<String, Integer> map) {

        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        Map<String, Integer> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> mapping : list) {
            newMap.put(mapping.getKey(), mapping.getValue());
        }
        return newMap;
    }

    public void resetParticipant() {
        if (getFoundryMachiner() == null) {
            Map<String, Integer> participantList = foundryMachineManager.getParticipantList();
            for (String pubKey : participantList.keySet()) {
                Map<String, Long> fromAssetsPtn = assetsManager.getAccountAssets(pubKey, Constants.PTN);
                foundryMachineManager.setParticipant(pubKey, foundryMachineManager.getFoundryMachineCount(fromAssetsPtn));
            }
            foundryMachineManager.setWaitfoundryMachine(getFoundryMachiner());
            foundryMachineManager.setWaitFoundryMachineCount(1);
        }
    }

    public String getFoundryMachiner() {
        Map<String, Integer> participantList = FoundryUtils.getSortingMap(foundryMachineManager.getParticipantList());
        String foundryMachiner = null;
        for (String pubKey : participantList.keySet()) {
            if (foundryMachiner == null) {
                foundryMachiner = pubKey;
                continue;
            }
            if (participantList.get(pubKey) > participantList.get(foundryMachiner)) {
                foundryMachiner = pubKey;
            }
        }
        if (foundryMachiner != null && participantList.get(foundryMachiner) <= 0) {
            return null;
        }
        return foundryMachiner;
    }
}
