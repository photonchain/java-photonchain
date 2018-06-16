package com.photon.photonchain.network.ehcacheManager;

import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.entity.UnconfirmedTran;
import com.photon.photonchain.storage.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: PTN
 * @description: Assets
 * @program: photon-chain
 * @create: 2018-05-23 10:07
 **/
@Component
public class AssetsManager {

    final static Logger logger = LoggerFactory.getLogger(AssetsManager.class);


    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UnconfirmedTranManager unconfirmedTranManager;

    public Map<String, Long> getAccountAssets(String pubKey, String tokenName) {
        long start = System.currentTimeMillis();
        Map<String, Long> resultMap = new HashMap<>();

        long totalIncome = 0;
        long totalExpenditure = 0;
        long totalEffectiveIncome = 0;
        long balance = 0;
        long transExpenditure = 0;
        long untransExpenditure = 0;
        long untransIncome = 0;
        long transIncome = 0;

        try {
            //TODO:unconfirm
            //unConfirm
            Map<String, String> unConfirmTransAssets = getAccountsUnTransValue(unconfirmedTranManager.getUnconfirmedTranMap());
            String unConfirmTransAsset = unConfirmTransAssets.get(pubKey + "_" + tokenName.toLowerCase());
            if (unConfirmTransAsset != null) {
                untransIncome = Long.parseLong(unConfirmTransAsset.split(",")[0]);
                untransExpenditure = Long.parseLong(unConfirmTransAsset.split(",")[1]);
            }
            //confirm
            transIncome = transactionRepository.findIncome(pubKey, tokenName);
            if (tokenName.equalsIgnoreCase(Constants.PTN)) {
                transExpenditure = transactionRepository.findExpenditureValue(pubKey, tokenName) + transactionRepository.findSumFee(pubKey);
            } else {
                transExpenditure = transactionRepository.findExpenditureValue(pubKey, tokenName);
            }
            totalIncome = transIncome + untransIncome;
            totalExpenditure = transExpenditure + untransExpenditure;
            totalEffectiveIncome = transIncome;
            balance = (totalEffectiveIncome - totalExpenditure) < 0 ? 0L : (totalEffectiveIncome - totalExpenditure);
        } catch (Exception e) {
            logger.error("exception：" + e.getMessage());
        }
        resultMap.put(Constants.TOTAL_INCOME, totalIncome);
        resultMap.put(Constants.TOTAL_EFFECTIVE_INCOME, totalEffectiveIncome);
        resultMap.put(Constants.TOTAL_EXPENDITURE, totalExpenditure);
        resultMap.put(Constants.BALANCE, balance);
        long end = System.currentTimeMillis();
        logger.info("use:" + (end - start));
        return resultMap;
    }


    private static Map<String, String> getAccountsUnTransValue(Map<String, UnconfirmedTran> Unmap) {
        Map<String, String> map = new HashMap();
        for (String key : Unmap.keySet()) {
            UnconfirmedTran transaction = Unmap.get(key);
            if (map.get(transaction.getTransFrom() + "_" + transaction.getTokenName().toLowerCase()) != null) {
                String value = map.get(transaction.getTransFrom() + "_" + transaction.getTokenName().toLowerCase());
                long valueNow = Long.valueOf(value.split(",")[1]).longValue() + transaction.getTransValue();
                map.put(transaction.getTransFrom() + "_" + transaction.getTokenName().toLowerCase(), value.split(",")[0] + "," + valueNow);
            }
            if (map.get(transaction.getTransTo() + "_" + transaction.getTokenName().toLowerCase()) != null) {
                String value = map.get(transaction.getTransTo() + "_" + transaction.getTokenName().toLowerCase());
                long valueNow = Long.valueOf(value.split(",")[0]).longValue() + transaction.getTransValue();
                map.put(transaction.getTransTo() + "_" + transaction.getTokenName().toLowerCase(), valueNow + "," + value.split(",")[1]);
            }
            if (map.get(transaction.getTransFrom() + "_" + transaction.getTokenName().toLowerCase()) == null) {
                long value = transaction.getTransValue();
                map.put(transaction.getTransFrom() + "_" + transaction.getTokenName().toLowerCase(), 0 + "," + value);
            }
            if (map.get(transaction.getTransTo() + "_" + transaction.getTokenName().toLowerCase()) == null) {
                long value = transaction.getTransValue();
                map.put(transaction.getTransTo() + "_" + transaction.getTokenName().toLowerCase(), value + "," + 0);
            }
        }
        for (String key : Unmap.keySet()) {
            UnconfirmedTran transaction = Unmap.get(key);
            if (map.get(transaction.getTransFrom() + "_" + Constants.PTN) != null) {
                String value = map.get(transaction.getTransFrom() + "_" + Constants.PTN);
                long valueNow = Long.valueOf(value.split(",")[1]).longValue() + transaction.getFee();
                map.put(transaction.getTransFrom() + "_" + Constants.PTN, value.split(",")[0] + "," + valueNow);
            }
            if (map.get(transaction.getTransFrom() + "_" + Constants.PTN) == null) {
                map.put(transaction.getTransFrom() + "_" + Constants.PTN, 0 + "," + -transaction.getFee());
            }
        }
        Map<String, String> newMap = new HashMap();
        for (String s : map.keySet()) {
            System.out.println(s + ":" + map.get(s));
        }
        return map;
    }


}
