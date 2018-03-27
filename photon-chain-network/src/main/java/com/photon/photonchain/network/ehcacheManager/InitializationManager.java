package com.photon.photonchain.network.ehcacheManager;

import com.photon.photonchain.network.utils.DeEnCode;
import com.photon.photonchain.network.utils.FileUtil;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.encryption.ECKey;
import com.photon.photonchain.storage.entity.Assets;
import com.photon.photonchain.storage.entity.Block;
import com.photon.photonchain.storage.entity.Transaction;
import com.photon.photonchain.storage.entity.UnconfirmedTran;
import com.photon.photonchain.storage.repository.AssetsRepository;
import com.photon.photonchain.storage.repository.TokenRepository;
import com.photon.photonchain.storage.repository.TransactionRepository;
import com.photon.photonchain.storage.repository.UnconfirmedTranRepository;
import net.sf.ehcache.Cache;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author:PTN
 * @Description:
 * @Date:20:02 2018/1/18
 * @Modified by:
 */
@Component
public class InitializationManager {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(InitializationManager.class);

    private Cache initializationCache = EhCacheManager.getCache("initializationCache");

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UnconfirmedTranRepository unconfirmedTranRepository;

    @Autowired
    private AssetsRepository assetsRepository;

    private static final String LAST_BLOCK = "LAST_BLOCK";
    private static final String BLOCK_HEIGHT = "BLOCK_HEIGHT";
    private static final String ACCOUNT = "ACCOUNT";
    private static final String ACCOUNT_LIST = "ACCOUNT_LIST";
    private static final String NODE_LIST = "NODE_LIST";
    private static final String ALREADY_SYNC_NODE = "ALREADY_SYNC_NODE";
    private static final String TOKEN_LIST = "TOKEN_LIST";
    private static final String ACCOUNT_TOKEN = "ACCOUNT_TOKEN";
    private static final String TOKEN_DECIMAL = "TOKEN_DECIMAL";
    private static final String LAST_TRANSACTION = "LAST_TRANSACTION";

    public void setLastBlock(Block lastBlock) {
        EhCacheManager.put(initializationCache, LAST_BLOCK, lastBlock);
        EhCacheManager.put(initializationCache, BLOCK_HEIGHT, lastBlock.getBlockHeight());
    }

    public Block getLastBlock() {
        return EhCacheManager.getCacheValue(initializationCache, LAST_BLOCK, Block.class);
    }

    public long getBlockHeight() {
        return EhCacheManager.getCacheValue(initializationCache, BLOCK_HEIGHT, long.class);
    }

    public Map<String, Object> getAccountInfoByToken(String address, String tokenName) {
        Map<String, Object> balanceMap = new HashMap<>();
        try {
            Map<String, Map> accountMap = EhCacheManager.getCacheValue(initializationCache, ACCOUNT, HashMap.class);
            Map<String, Map> tokenMap = accountMap.get(address);
            balanceMap = tokenMap.get(tokenName);
        } catch (Exception e) {
            logger.info("获取账户资产异常--address:{},tokenName:{}", address, tokenName);
            balanceMap.put(Constants.TOTAL_EXPENDITURE, "0");
            balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, "0");
            balanceMap.put(Constants.TOTAL_INCOME, "0");
            balanceMap.put(Constants.PUBKEY, "");
        }
        if (balanceMap == null) {
            balanceMap = new HashMap<>();
            balanceMap.put(Constants.TOTAL_EXPENDITURE, "0");
            balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, "0");
            balanceMap.put(Constants.TOTAL_INCOME, "0");
            balanceMap.put(Constants.PUBKEY, "");
        }
        return balanceMap;
    }

    public Map<String, Map> getTokenInfoByAddress(String address) {
        Map<String, Map> tokenMap = new HashMap<>();
        try {
            Map<String, Map> accountMap = EhCacheManager.getCacheValue(initializationCache, ACCOUNT, HashMap.class);
            tokenMap = accountMap.get(address);
        } catch (Exception e) {
        }
        return tokenMap;
    }

    public void setAccount(Iterable<Transaction> transactionList) {
        Map<String, Map> accountMap = new HashMap<>();
        for (Transaction transaction : transactionList) {
            String tokenName = transaction.getTokenName();
            String pubkey_from = transaction.getTransFrom();
            String pubkey_to = transaction.getTransTo();
            String address_from = ECKey.pubkeyToAddress(pubkey_from);
            String address_to = ECKey.pubkeyToAddress(pubkey_to);
            long value = transaction.getTransactionHead().getTransValue();
            long fee = transaction.getTransactionHead().getFee();
            if (address_from.equals("") || address_to.equals("")) {
                continue;
            }
            if (transaction.getTransType() == 1 && !transaction.getTokenName().equalsIgnoreCase(Constants.PTN)) {  //token
                if (accountMap.containsKey(address_from)) {
                    Map<String, Map> tokenMap = accountMap.get(address_from);
                    if (tokenMap.containsKey(tokenName)) {
                        Map<String, Object> balanceMap = tokenMap.get(tokenName);
                        if (transaction.getBlockHeight() != -1) {
                            balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, Long.parseLong(balanceMap.get(Constants.TOTAL_EFFECTIVE_INCOME).toString()) + value);
                        }
                    } else {
                        Map<String, Object> balanceMap = new HashMap<>();
                        balanceMap.put(Constants.TOTAL_EXPENDITURE, 0);
                        balanceMap.put(Constants.TOTAL_INCOME, value);
                        if (transaction.getBlockHeight() != -1) {
                            balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, value);
                        } else {
                            balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0);
                        }
                        balanceMap.put(Constants.PUBKEY, pubkey_from);
                        tokenMap.put(tokenName, balanceMap);
                    }

                    dealTokenFee(pubkey_from, fee, tokenMap);


                } else {
                    Map<String, Object> balanceMap = new HashMap<>();
                    Map<String, Map> tokenMap = new HashMap<>();
                    balanceMap.put(Constants.TOTAL_EXPENDITURE, 0L);
                    balanceMap.put(Constants.TOTAL_INCOME, value);
                    if (transaction.getBlockHeight() != -1) {
                        balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, value);
                    } else {
                        balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);

                    }
                    balanceMap.put(Constants.PUBKEY, pubkey_from);
                    tokenMap.put(tokenName, balanceMap);

                    Map<String, Object> ptnMap = new HashMap<>();
                    ptnMap.put(Constants.TOTAL_EXPENDITURE, fee);
                    ptnMap.put(Constants.TOTAL_INCOME, 0L);
                    ptnMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                    ptnMap.put(Constants.PUBKEY, pubkey_from);
                    tokenMap.put(Constants.PTN, ptnMap);

                    accountMap.put(address_from, tokenMap);
                }
            } else
                commonTransactionDeal(accountMap, transaction, tokenName, pubkey_from, pubkey_to, address_from, address_to, value, fee);
        }
        EhCacheManager.put(initializationCache, ACCOUNT, accountMap);
    }

    private void commonTransactionDeal(Map<String, Map> accountMap, Transaction transaction, String tokenName, String pubkey_from, String pubkey_to, String address_from, String address_to, long value, long fee) {
        //deal with from account
        dealWithFromAccount(accountMap, tokenName, pubkey_from, address_from, value, fee, transaction.getTransType());
        //deal with to account
        if (accountMap.containsKey(address_to)) {
            Map<String, Map> tokenMap = accountMap.get(address_to);
            if (tokenMap.containsKey(tokenName)) {
                Map<String, Object> balanceMap = tokenMap.get(tokenName);
                balanceMap.put(Constants.TOTAL_INCOME, Long.parseLong(balanceMap.get(Constants.TOTAL_INCOME).toString()) + value);
                if (transaction.getBlockHeight() != -1) {
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, Long.parseLong(balanceMap.get(Constants.TOTAL_EFFECTIVE_INCOME).toString()) + value);
                }
                tokenMap.put(tokenName, balanceMap);
            } else {
                Map<String, Object> balanceMap = new HashMap<>();
                balanceMap.put(Constants.TOTAL_INCOME, value);
                balanceMap.put(Constants.TOTAL_EXPENDITURE, 0L);
                if (transaction.getBlockHeight() != -1) {
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, value);
                } else {
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                }
                balanceMap.put(Constants.PUBKEY, pubkey_to);
                tokenMap.put(tokenName, balanceMap);
            }
            accountMap.put(address_to, tokenMap);
        } else {
            Map<String, Object> balanceMap = new HashMap<>();
            Map<String, Map> tokenMap = new HashMap<>();
            balanceMap.put(Constants.TOTAL_INCOME, value);
            balanceMap.put(Constants.TOTAL_EXPENDITURE, 0L);
            if (transaction.getBlockHeight() != -1) {
                balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, value);
            } else {
                balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
            }
            balanceMap.put(Constants.PUBKEY, pubkey_to);
            tokenMap.put(tokenName, balanceMap);
            accountMap.put(address_to, tokenMap);
        }
    }

    /*    private void dealWithFromAccount(Map<String, Map> accountMap, String tokenName, String pubkey_from, String address_from, long value, long fee) {
            if (accountMap.containsKey(address_from)) {
                Map<String, Map> tokenMap = accountMap.get(address_from);
                if (tokenMap.containsKey(tokenName)) {
                    Map<String, Object> balanceMap = tokenMap.get(tokenName);
                    balanceMap.put(Constants.TOTAL_EXPENDITURE, Long.parseLong(balanceMap.get(Constants.TOTAL_EXPENDITURE).toString()) + value + fee);
                    tokenMap.put(tokenName, balanceMap);
                } else {
                    Map<String, Object> balanceMap = new HashMap<>();
                    balanceMap.put(Constants.TOTAL_EXPENDITURE, value + fee);
                    balanceMap.put(Constants.TOTAL_INCOME, 0L);
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                    tokenMap.put(tokenName, balanceMap);
                }
                accountMap.put(address_from, tokenMap);
            } else {
                Map<String, Object> balanceMap = new HashMap<>();
                Map<String, Map> tokenMap = new HashMap<>();
                balanceMap.put(Constants.TOTAL_EXPENDITURE, value + fee);
                balanceMap.put(Constants.TOTAL_INCOME, 0L);
                balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                balanceMap.put(Constants.PUBKEY, pubkey_from);
                tokenMap.put(tokenName, balanceMap);
                accountMap.put(address_from, tokenMap);
            }
        }*/
    private void dealWithFromAccount(Map<String, Map> accountMap, String tokenName, String pubkey_from, String address_from, long value, long fee, int transType) {
        if (transType == 2) {
            return;
        }
        if (accountMap.containsKey(address_from)) {
            Map<String, Map> tokenMap = accountMap.get(address_from);
            if (tokenMap.containsKey(tokenName)) {
                Map<String, Object> balanceMap = tokenMap.get(tokenName);
                balanceMap.put(Constants.TOTAL_EXPENDITURE, Long.parseLong(balanceMap.get(Constants.TOTAL_EXPENDITURE).toString()) + value);
                tokenMap.put(tokenName, balanceMap);
            } else {
                Map<String, Object> balanceMap = new HashMap<>();
                balanceMap.put(Constants.TOTAL_EXPENDITURE, value);
                balanceMap.put(Constants.TOTAL_INCOME, 0L);
                balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                balanceMap.put(Constants.PUBKEY, pubkey_from);
                tokenMap.put(tokenName, balanceMap);
            }
            this.dealTokenFee(pubkey_from, fee, tokenMap);
            accountMap.put(address_from, tokenMap);
        } else {
            if (tokenName.equalsIgnoreCase(Constants.PTN)) {
                Map<String, Object> balanceMap = new HashMap<>();
                Map<String, Map> tokenMap = new HashMap<>();
                balanceMap.put(Constants.TOTAL_EXPENDITURE, value + fee);
                balanceMap.put(Constants.TOTAL_INCOME, 0L);
                balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                balanceMap.put(Constants.PUBKEY, pubkey_from);
                tokenMap.put(tokenName, balanceMap);
                accountMap.put(address_from, tokenMap);
            } else {
                Map<String, Object> balanceMap = new HashMap<>();
                Map<String, Map> tokenMap = new HashMap<>();
                balanceMap.put(Constants.TOTAL_EXPENDITURE, value);
                balanceMap.put(Constants.TOTAL_INCOME, 0L);
                balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                balanceMap.put(Constants.PUBKEY, pubkey_from);
                tokenMap.put(tokenName, balanceMap);
                this.dealTokenFee(pubkey_from, fee, tokenMap);
                accountMap.put(address_from, tokenMap);
            }
        }
    }


    public void setAccountUnconfirmed(Iterable<UnconfirmedTran> unconfirmedTren) {
        Map<String, Map> accountMap = EhCacheManager.getCacheValue(initializationCache, ACCOUNT, Map.class);
        for (UnconfirmedTran transaction : unconfirmedTren) {
            String tokenName = transaction.getTokenName();
            String pubkey_from = transaction.getTransFrom();
            String pubkey_to = transaction.getTransTo();
            String address_from = ECKey.pubkeyToAddress(pubkey_from);
            String address_to = ECKey.pubkeyToAddress(pubkey_to);
            long value = transaction.getTransValue();
            long fee = transaction.getFee();
            if (address_from.equals("") || address_to.equals("")) {
                continue;
            }
            dealUnconfirmedTransaction(transaction, accountMap, tokenName, pubkey_from, pubkey_to, address_from, address_to, value, fee);
        }
        EhCacheManager.put(initializationCache, ACCOUNT, accountMap);
    }

    public void updateEffective(Transaction transaction) {
        Map<String, Map> accountMap = EhCacheManager.getCacheValue(initializationCache, ACCOUNT, Map.class);
        String pubkey_to = transaction.getTransTo();
        String address_to = ECKey.pubkeyToAddress(pubkey_to);
        if (address_to.equals("")) {
            return;
        }
        if (accountMap.containsKey(address_to)) {
            Map<String, Object> accountAmount = getAccountInfoByToken(address_to, transaction.getTokenName());
            if (transaction.getBlockHeight() != -1) {
                accountAmount.put(Constants.TOTAL_EFFECTIVE_INCOME, Long.valueOf(accountAmount.get(Constants.TOTAL_EFFECTIVE_INCOME).toString()) + transaction.getTransactionHead().getTransValue());
            }
            if (transaction.getTransType() == 2) {
                accountAmount.put(Constants.TOTAL_INCOME, Long.valueOf(accountAmount.get(Constants.TOTAL_INCOME).toString()) + transaction.getTransactionHead().getTransValue());
            }
        }

        EhCacheManager.put(initializationCache, ACCOUNT, accountMap);
    }

/*    public void updateEffective(Transaction transaction) {
        Map<String, Map> accountMap = EhCacheManager.getCacheValue(initializationCache, ACCOUNT, Map.class);
        String pubkey_to = transaction.getTransTo();
        String address_to = ECKey.pubkeyToAddress(pubkey_to);
        if (address_to.equals("")) {
            return;
        }
        if (accountMap.containsKey(address_to)) {
            Map<String, Map> tokenMap = accountMap.get(address_to);
            if(tokenMap.containsKey(transaction.getTokenName())){
                Map<String, Object> balanceMap = tokenMap.get(transaction.getTokenName());
                if (transaction.getBlockHeight() != -1) {
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, Long.parseLong(balanceMap.get(Constants.TOTAL_EFFECTIVE_INCOME).toString()) + transaction.getTransactionHead().getTransValue());
                }
                if (transaction.getTransType() == 2) {
                    balanceMap.put(Constants.TOTAL_INCOME,Long.parseLong(Constants.TOTAL_INCOME.toString())+transaction.getTransactionHead().getTransValue());
                }
                tokenMap.put(transaction.getTokenName(),balanceMap);
                accountMap.put(address_to,tokenMap);
            }
        }

        EhCacheManager.put(initializationCache, ACCOUNT, accountMap);
    }*/

    public void addAccount(Transaction transaction) {
        Map<String, Map> accountMap = EhCacheManager.getCacheValue(initializationCache, ACCOUNT, Map.class);
        String tokenName = transaction.getTokenName();
        String pubkey_from = transaction.getTransFrom();
        String pubkey_to = transaction.getTransTo();
        String address_from = ECKey.pubkeyToAddress(pubkey_from);
        String address_to = ECKey.pubkeyToAddress(pubkey_to);
        long value = transaction.getTransactionHead().getTransValue();
        long fee = transaction.getTransactionHead().getFee();
        if (address_from.equals("") || address_to.equals("")) {
            return;
        }
        if (transaction.getTransType() == 1) {  //token
            if (accountMap.containsKey(address_from)) {
                Map<String, Map> tokenMap = accountMap.get(address_from);
                if (tokenMap.containsKey(tokenName)) {
                    Map<String, Object> balanceMap = tokenMap.get(tokenName);
                    if (transaction.getBlockHeight() != -1) {
                        balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, Long.parseLong(balanceMap.get(Constants.TOTAL_EFFECTIVE_INCOME).toString()) + value);
                    }
                } else {
                    Map<String, Object> balanceMap = new HashMap<>();
                    balanceMap.put(Constants.TOTAL_EXPENDITURE, 0L);
                    balanceMap.put(Constants.TOTAL_INCOME, value);
                    if (transaction.getBlockHeight() != -1) {
                        balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, value);
                    } else {
                        balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                    }
                    balanceMap.put(Constants.PUBKEY, pubkey_from);
                    tokenMap.put(tokenName, balanceMap);
                }

                dealTokenFee(pubkey_from, fee, tokenMap);

            } else {
                Map<String, Object> balanceMap = new HashMap<>();
                Map<String, Map> tokenMap = new HashMap<>();
                balanceMap.put(Constants.TOTAL_EXPENDITURE, 0L);
                balanceMap.put(Constants.TOTAL_INCOME, value);
                if (transaction.getBlockHeight() != -1) {
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, value);
                } else {
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);

                }
                balanceMap.put(Constants.PUBKEY, pubkey_from);
                tokenMap.put(tokenName, balanceMap);

                Map<String, Object> ptnMap = new HashMap<>();
                ptnMap.put(Constants.TOTAL_EXPENDITURE, fee);
                ptnMap.put(Constants.TOTAL_INCOME, 0L);
                ptnMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                ptnMap.put(Constants.PUBKEY, pubkey_from);
                tokenMap.put(Constants.PTN, ptnMap);

                accountMap.put(address_from, tokenMap);
            }
        } else {
            //deal with from account
            commonTransactionDeal(accountMap, transaction, tokenName, pubkey_from, pubkey_to, address_from, address_to, value, fee);
        }
        EhCacheManager.put(initializationCache, ACCOUNT, accountMap);
    }

    private void dealTokenFee(String pubkey_from, long fee, Map<String, Map> tokenMap) {
        if (tokenMap.containsKey(Constants.PTN)) {
            Map<String, Object> balanceMap = tokenMap.get(Constants.PTN);
            balanceMap.put(Constants.TOTAL_EXPENDITURE, Long.parseLong(balanceMap.get(Constants.TOTAL_EXPENDITURE).toString()) + fee);
            tokenMap.put(Constants.PTN, balanceMap);
        } else {
            Map<String, Object> balanceMap = new HashMap<>();
            balanceMap.put(Constants.TOTAL_EXPENDITURE, fee);
            balanceMap.put(Constants.TOTAL_INCOME, 0L);
            balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
            balanceMap.put(Constants.PUBKEY, pubkey_from);
            tokenMap.put(Constants.PTN, balanceMap);
        }
    }

    public void addAccount(UnconfirmedTran unconfirmedTran) {

        Map<String, Map> accountMap = EhCacheManager.getCacheValue(initializationCache, ACCOUNT, Map.class);

        String tokenName = unconfirmedTran.getTokenName();
        String pubkey_from = unconfirmedTran.getTransFrom();
        String pubkey_to = unconfirmedTran.getTransTo();
        String address_from = ECKey.pubkeyToAddress(pubkey_from);
        String address_to = ECKey.pubkeyToAddress(pubkey_to);
        long value = unconfirmedTran.getTransValue();
        long fee = unconfirmedTran.getFee();

        dealUnconfirmedTransaction(unconfirmedTran, accountMap, tokenName, pubkey_from, pubkey_to, address_from, address_to, value, fee);

        EhCacheManager.put(initializationCache, ACCOUNT, accountMap);

    }

    private void dealUnconfirmedTransaction(UnconfirmedTran unconfirmedTran, Map<String, Map> accountMap, String tokenName, String pubkey_from, String pubkey_to, String address_from, String address_to, long value, long fee) {
        if (unconfirmedTran.getTransType() == 1 && !unconfirmedTran.getTokenName().equalsIgnoreCase(Constants.PTN)) {  //token
            if (accountMap.containsKey(address_from)) {
                Map<String, Map> tokenMap = accountMap.get(address_from);
                if (tokenMap.containsKey(tokenName)) {
                    Map<String, Object> balanceMap = tokenMap.get(tokenName);
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, Long.parseLong(balanceMap.get(Constants.TOTAL_EFFECTIVE_INCOME).toString()) + 0);
                } else {
                    Map<String, Object> balanceMap = new HashMap<>();
                    balanceMap.put(Constants.TOTAL_EXPENDITURE, 0);
                    balanceMap.put(Constants.TOTAL_INCOME, value);
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0);
                    balanceMap.put(Constants.PUBKEY, pubkey_from);
                    tokenMap.put(tokenName, balanceMap);
                }

                dealTokenFee(pubkey_from, fee, tokenMap);


            } else {
                Map<String, Object> balanceMap = new HashMap<>();
                Map<String, Map> tokenMap = new HashMap<>();
                balanceMap.put(Constants.TOTAL_EXPENDITURE, 0L);
                balanceMap.put(Constants.TOTAL_INCOME, value);
                balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                balanceMap.put(Constants.PUBKEY, pubkey_from);
                tokenMap.put(tokenName, balanceMap);

                Map<String, Object> ptnMap = new HashMap<>();
                ptnMap.put(Constants.TOTAL_EXPENDITURE, fee);
                ptnMap.put(Constants.TOTAL_INCOME, 0L);
                ptnMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                ptnMap.put(Constants.PUBKEY, pubkey_from);
                tokenMap.put(Constants.PTN, ptnMap);

                accountMap.put(address_from, tokenMap);
            }
        } else {
            //deal with from account
            dealWithFromAccount(accountMap, tokenName, pubkey_from, address_from, value, fee, unconfirmedTran.getTransType());
            //deal with to account
            if (accountMap.containsKey(address_to)) {
                Map<String, Map> tokenMap = accountMap.get(address_to);
                if (tokenMap.containsKey(tokenName)) {
                    Map<String, Object> balanceMap = tokenMap.get(tokenName);
                    balanceMap.put(Constants.TOTAL_INCOME, Long.parseLong(balanceMap.get(Constants.TOTAL_INCOME).toString()) + value);
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, Long.parseLong(balanceMap.get(Constants.TOTAL_EFFECTIVE_INCOME).toString()) + 0);
                    tokenMap.put(tokenName, balanceMap);
                } else {
                    Map<String, Object> balanceMap = new HashMap<>();
                    balanceMap.put(Constants.TOTAL_INCOME, value);
                    balanceMap.put(Constants.TOTAL_EXPENDITURE, 0L);
                    balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                    balanceMap.put(Constants.PUBKEY, pubkey_to);
                    tokenMap.put(tokenName, balanceMap);
                }
                accountMap.put(address_to, tokenMap);
            } else {
                Map<String, Object> balanceMap = new HashMap<>();
                Map<String, Map> tokenMap = new HashMap<>();
                balanceMap.put(Constants.TOTAL_INCOME, value);
                balanceMap.put(Constants.TOTAL_EXPENDITURE, 0L);
                balanceMap.put(Constants.TOTAL_EFFECTIVE_INCOME, 0L);
                balanceMap.put(Constants.PUBKEY, pubkey_to);
                tokenMap.put(tokenName, balanceMap);
                accountMap.put(address_to, tokenMap);
            }
        }
    }

    public Map<String, Map> getAccount() {
        return FileUtil.clone(EhCacheManager.getCacheValue(initializationCache, ACCOUNT, HashMap.class));
    }

    public void setAccount(Map<String, Map> accountMap) {
        EhCacheManager.put(initializationCache, ACCOUNT, accountMap);
    }

    public void setAccountList(Map<String, String> accountList) {
        EhCacheManager.put(initializationCache, ACCOUNT_LIST, accountList);
    }

    public void addAccountList(String address, String account) {
        Map<String, String> accountMap = EhCacheManager.getCacheValue(initializationCache, ACCOUNT_LIST, Map.class);
        accountMap.put(address, account);
        EhCacheManager.put(initializationCache, ACCOUNT_LIST, accountMap);
    }

    public Map<String, String> getAccountList() {
        return EhCacheManager.getCacheValue(initializationCache, ACCOUNT_LIST, Map.class);
    }

    public Map<String, String> getAccountListByAddress(String address) {
        Map<String, String> resultMap = new HashMap<>();

        String pwd = "";
        String pubkey = "";
        String prikey = "";
        try {
            Map<String, String> localAccount = this.getAccountList();
            String accountEncode = localAccount.get(address);
            String account = DeEnCode.decode(accountEncode);
            pwd = account.substring(account.indexOf(Constants.PWD_FLAG) + Constants.PWD_FLAG.length(), account.indexOf(Constants.PUBKEY_FLAG));
            pubkey = account.substring(account.indexOf(Constants.PUBKEY_FLAG) + Constants.PUBKEY_FLAG.length(), account.indexOf(Constants.PRIKEY_FLAG));
            prikey = account.substring(account.indexOf(Constants.PRIKEY_FLAG) + Constants.PRIKEY_FLAG.length());
        } catch (Exception e) {
            System.out.println("getAccountListByAddress exception...");
        }
        resultMap.put(Constants.PWD, pwd);
        resultMap.put(Constants.PUBKEY, pubkey);
        resultMap.put(Constants.PRIKEY, prikey);
        return resultMap;
    }

    public void setAlreadySyncNodeNumber(Integer nodeNumber) {
        EhCacheManager.put(initializationCache, ALREADY_SYNC_NODE, nodeNumber);
    }

    public Integer getAlreadySyncNodeNumber() {
        return EhCacheManager.getCacheValue(initializationCache, ALREADY_SYNC_NODE, Integer.class);
    }

    public void setNodeList(List<String> nodeList) {
        EhCacheManager.put(initializationCache, NODE_LIST, nodeList);
    }

    public List<String> getNodeList() {
        return EhCacheManager.getCacheValue(initializationCache, NODE_LIST, List.class);
    }

    public BigDecimal unitConvert(Object value, String tokenName, String flag) {
        Integer decimals = this.getTokenDecimal(tokenName);
        long unit = 1;
        for (int i = 0; i < decimals; i++) {
            unit = unit * 10;
        }
        BigDecimal big_result = new BigDecimal(0);
        BigDecimal value_big = new BigDecimal(String.valueOf(value));
        BigDecimal value_unit = new BigDecimal(unit);
        if (flag.equals(Constants.MINI_UNIT)) {
            big_result = value_big.multiply(value_unit).setScale(decimals, BigDecimal.ROUND_HALF_UP);
        } else if (flag.equals(Constants.MAX_UNIT)) {
            big_result = value_big.divide(value_unit).setScale(decimals, BigDecimal.ROUND_HALF_UP);
        }
        return big_result;
    }

    public void setAccountToken(Map<String, Set> accountToken) {
        EhCacheManager.put(initializationCache, ACCOUNT_TOKEN, accountToken);
    }

    public Map<String, Set> getAccountToken() {
        return EhCacheManager.getCacheValue(initializationCache, ACCOUNT_TOKEN, Map.class);
    }

    public long getTokenAssets(String tokenName, boolean ignoreUnverified) {
        long tokenAssets = 0;
        List<Assets> assetsList = assetsRepository.findAllByTokenName(tokenName);
        for (Assets assets:assetsList) {
         tokenAssets = tokenAssets + (assets.getTotalIncome()-assets.getTotalExpenditure());

        }
        if (tokenName.equals(Constants.PTN) && !ignoreUnverified) {
            tokenAssets = tokenAssets + unconfirmedTranRepository.getUnconfirmedFee();
        }
        return tokenAssets;
    }

    public void addTokenDecimal(String token, Integer decimal) {
        Map<String, Integer> tokenDecimalMap = null;
        try {
            tokenDecimalMap = EhCacheManager.getCacheValue(initializationCache, TOKEN_DECIMAL, Map.class);
        } catch (Exception e) {
            tokenDecimalMap = new HashMap<>();
        }
        tokenDecimalMap.put(token, decimal);
        EhCacheManager.put(initializationCache, TOKEN_DECIMAL, tokenDecimalMap);
    }

    public Integer getTokenDecimal(String token) {
        Integer decimal = 6;
        try {
            Map<String, Integer> tokenDecimalMap = EhCacheManager.getCacheValue(initializationCache, TOKEN_DECIMAL, Map.class);
            decimal = tokenDecimalMap.get(token);
            return decimal == null ? 6 : decimal;
        } catch (Exception e) {
            return 6;
        }

    }


    public void setLastTransaction(Transaction lastTransaction) {
        EhCacheManager.put(initializationCache, LAST_TRANSACTION, lastTransaction);
    }

    public Transaction getLastTransaction() {
        return EhCacheManager.getCacheValue(initializationCache, LAST_TRANSACTION, Transaction.class);
    }
}
