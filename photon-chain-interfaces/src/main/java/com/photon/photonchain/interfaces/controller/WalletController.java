package com.photon.photonchain.interfaces.controller;

import com.alibaba.fastjson.JSON;
import com.photon.photonchain.interfaces.utils.*;
import com.photon.photonchain.network.core.GenesisBlock;
import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.network.ehcacheManager.NioSocketChannelManager;
import com.photon.photonchain.network.ehcacheManager.SyncBlockManager;
import com.photon.photonchain.network.ehcacheManager.SyncUnconfirmedTranManager;
import com.photon.photonchain.network.proto.InesvMessage;
import com.photon.photonchain.network.proto.MessageManager;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.encryption.ECKey;
import com.photon.photonchain.storage.encryption.SHAEncrypt;
import com.photon.photonchain.storage.entity.*;
import com.photon.photonchain.storage.repository.AssetsRepository;
import com.photon.photonchain.storage.repository.BlockRepository;
import com.photon.photonchain.storage.repository.TokenRepository;
import com.photon.photonchain.storage.repository.TransactionRepository;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Soundbank;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

import static com.photon.photonchain.network.proto.EventTypeEnum.EventType.NEW_TRANSACTION;
import static com.photon.photonchain.network.proto.MessageTypeEnum.MessageType.RESPONSE;

/**
 * @author: lqh
 * @description: ss
 * @program: photon-chain-new
 * @create: 2017-11-19 15:40
 **/
@Controller
@RequestMapping("WalletController")
public class WalletController {

    private static Logger logger = LoggerFactory.getLogger(WalletController.class);

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    InitializationManager initializationManager;

    @Autowired
    SyncBlockManager syncBlockManager;

    @Autowired
    NioSocketChannelManager nioSocketChannelManager;

    @Autowired
    SyncUnconfirmedTranManager syncUnconfirmedTranManager;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    private AssetsRepository assetsRepository;

    /**
     * Get wallet total balance
     *
     * @return
     */
    @GetMapping("getTotalBalance")
    @ResponseBody
    public Res getTotalBalance(String tokenName) {
        Res res = new Res();
        Map<String, Object> dataMap = new HashMap<>();
        long totalEffectiveIncome = 0;
        long totalExpenditure = 0l;
        try {
            Map<String, String> localAccount = initializationManager.getAccountList();
            for (Map.Entry<String, String> entry : localAccount.entrySet()) {
                String address = entry.getKey();
                Map<String, String> accountInfo = initializationManager.getAccountListByAddress(address);
                String pubkey = accountInfo.get(Constants.PUBKEY);
                Assets assets = assetsRepository.findByPubKeyAndTokenName(pubkey, tokenName);
                if (assets != null) {
                    totalEffectiveIncome += assets.getTotalEffectiveIncome();
                    totalExpenditure += assets.getTotalExpenditure();
                }
            }
            BigDecimal total = initializationManager.unitConvert(totalEffectiveIncome - totalExpenditure, tokenName, Constants.MAX_UNIT);
            dataMap.put("totalBalance", total.compareTo(new BigDecimal(0)) > 0 ? total : new BigDecimal(0));
            res.setCode(Res.CODE_100);
            res.setMsg("");
            res.setData(dataMap);
        } catch (Exception e) {
            res.setCode(Res.CODE_101);
            res.setMsg("");
            res.setData("");
            e.printStackTrace();
        }
        return res;
    }

    /**
     * get node account list
     *
     * @return
     */
    @GetMapping("getAllAccount")
    @ResponseBody
    public Res getAllAccount(String tokenName) {
        Res res = new Res();
        List<Map> accountMapList = new ArrayList<>();
        Map<String, Object> dataMap = new HashMap<>();
        try {
            Map<String, String> localAccount = initializationManager.getAccountList();
            if (localAccount.isEmpty()) {
                res.setCode(Res.CODE_101);
                return res;
            }
            for (Map.Entry<String, String> entry : localAccount.entrySet()) {
                String address = entry.getKey();
                Map<String, String> accountInfo = initializationManager.getAccountListByAddress(address);
                String pubkey = accountInfo.get(Constants.PUBKEY);
                Long totalExpenditure = 0l;
                Long totalIncome = 0l;
                Long totalEffectiveIncome = 0l;
                Long balance = 0l;
                Map<String, Object> accountMap = new HashMap<>();
                Assets assets = assetsRepository.findByPubKeyAndTokenName(pubkey, tokenName);
                if (assets != null) {
                    totalExpenditure = assets.getTotalExpenditure();
                    totalIncome = assets.getTotalIncome();
                    totalEffectiveIncome = assets.getTotalEffectiveIncome();
                    balance = totalEffectiveIncome - totalExpenditure;
                }
                if (balance < 0) {
                    balance = 0L;
                }
                accountMap.put("totalExpenditure", initializationManager.unitConvert(totalExpenditure, tokenName, Constants.MAX_UNIT));
                accountMap.put("totalIncome", initializationManager.unitConvert(totalIncome, tokenName, Constants.MAX_UNIT));
                accountMap.put("totalEffectiveIncome", initializationManager.unitConvert(totalEffectiveIncome, tokenName, Constants.MAX_UNIT));
                accountMap.put("balance", initializationManager.unitConvert(balance, tokenName, Constants.MAX_UNIT));
                accountMap.put("address", address);
                accountMap.put(Constants.PUBKEY, pubkey);
                accountMapList.add(accountMap);
            }
            dataMap.put("accounts", accountMapList);
            res.setCode(Res.CODE_100);
            res.setMsg("");
            res.setData(dataMap);
        } catch (Exception e) {
            res.setCode(Res.CODE_101);
            res.setMsg("");
            res.setData("");
            e.printStackTrace();
        }
        return res;
    }

    /**
     * get account balance
     * address ： wallet address
     * tokenName ：token
     *
     * @return
     */
    @GetMapping("getAccountBalance")
    @ResponseBody
    public Res getAccountBalance(String address, String tokenName) {
        Res res = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> accountMap = new HashMap<>();
        List<Map<String, Object>> transactionList = new ArrayList<>();
        long totalIncome = 0;
        long totalExpenditure = 0;
        long totalEffectiveIncome = 0;
        long balance = 0;
        String pubKey = "";

        Map<String, String> account = initializationManager.getAccountListByAddress(address);
        if (account.get(Constants.PUBKEY).equals("")) {
            res.setCode(Res.CODE_102);
            res.setMsg("");
            res.setData(resultMap);
            return res;
        }
        pubKey = account.get(Constants.PUBKEY);
        Assets assets = assetsRepository.findByPubKeyAndTokenName(pubKey, tokenName);
        if (assets != null) {
            totalIncome = assets.getTotalIncome();
            totalExpenditure = assets.getTotalExpenditure();
            totalEffectiveIncome = assets.getTotalEffectiveIncome();
        }
        accountMap.put("totalExpenditure", initializationManager.unitConvert(totalExpenditure, tokenName, Constants.MAX_UNIT));
        accountMap.put("totalIncome", initializationManager.unitConvert(totalIncome, tokenName, Constants.MAX_UNIT));
        accountMap.put("totalEffectiveIncome", initializationManager.unitConvert(totalEffectiveIncome, tokenName, Constants.MAX_UNIT));
        accountMap.put("balance", initializationManager.unitConvert(balance, tokenName, Constants.MAX_UNIT));
        accountMap.put("address", address);
        accountMap.put("pubKey", account.get(Constants.PUBKEY));
        resultMap.put("accountMap", accountMap);
        res.setCode(Res.CODE_100);
        res.setMsg("");
        res.setData(resultMap);
        return res;
    }


    /**
     * get account info(address & transactions)
     *
     * @param address   ： wallet address
     * @param tokenName ： token
     * @return
     */
    @GetMapping("getAccountInfo")
    @ResponseBody
    public Res getAccountInfo(String address, String tokenName, PageObject pageObject) {
        Res res = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> accountMap = new HashMap<>();
        List<Map<String, Object>> transactionList = new ArrayList<>();
        long totalIncome = 0;
        long totalExpenditure = 0;
        long totalEffectiveIncome = 0;
        long balance = 0;
        Map<String, String> localAccount = initializationManager.getAccountListByAddress(address);
        String pubKey = localAccount.get(Constants.PUBKEY);
        if (pubKey.equals("")) {
            res.setCode(Res.CODE_102);
            return res;
        }
        Assets assets = assetsRepository.findByPubKeyAndTokenName(pubKey, tokenName);
        if (assets != null) {
            totalIncome = assets.getTotalIncome();
            totalExpenditure = assets.getTotalExpenditure();
            totalEffectiveIncome = assets.getTotalEffectiveIncome();
            balance = totalEffectiveIncome - totalExpenditure;
        }
        if (balance < 0) {
            balance = 0L;
        }
        accountMap.put("totalExpenditure", initializationManager.unitConvert(totalExpenditure, tokenName, Constants.MAX_UNIT));
        accountMap.put("totalIncome", initializationManager.unitConvert(totalIncome, tokenName, Constants.MAX_UNIT));
        accountMap.put("totalEffectiveIncome", initializationManager.unitConvert(totalEffectiveIncome, tokenName, Constants.MAX_UNIT));
        accountMap.put("balance", initializationManager.unitConvert(balance, tokenName, Constants.MAX_UNIT));
        accountMap.put("address", address);
        accountMap.put("pubKey", pubKey);
        resultMap.put("accountMap", accountMap);
        resultMap.put("transactionList", transactionList);
        resultMap.put("count", 1);
        resultMap.put("pageNumber", 1);
        res.setCode(Res.CODE_100);
        res.setMsg("");
        res.setData(resultMap);
        return res;
    }

    /**
     * get block info
     *
     * @return
     */
    @GetMapping("getBlockInfo")
    @ResponseBody
    public Res getBlockInfo() {
        Res r = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        Double avgAmount = blockRepository.getAmountAvg();
        Double avgFee = blockRepository.getFeeAvg();
        Transaction lastTransaction = initializationManager.getLastTransaction();
        TransactionHead lastTransactionHead = lastTransaction.getTransactionHead();
        long trans_timeDiff_h = (lastTransactionHead.getTimeStamp() - GenesisBlock.GENESIS_TIME) / 3600000;  //h
        if (trans_timeDiff_h == 0) {
            trans_timeDiff_h = 1;
        }
        long trans_count = transactionRepository.count();
        long h_trans_count = trans_count / trans_timeDiff_h;
        Block lastBlock = initializationManager.getLastBlock();
        BlockHead lastBlockHead = lastBlock.getBlockHead();
        long block_timeDiff_s = (lastBlockHead.getTimeStamp() - GenesisBlock.GENESIS_TIME) / 1000; //s
        long block_count = blockRepository.count();
        if (block_count == 0) {
            block_count = 1;
        }
        long block_time = block_timeDiff_s / block_count;

        resultMap.put("avgAmount", initializationManager.unitConvert(avgAmount, Constants.PTN, Constants.MAX_UNIT));
        resultMap.put("avgFee", initializationManager.unitConvert(avgFee, Constants.PTN, Constants.MAX_UNIT));
        resultMap.put("hTransCount", h_trans_count);
        resultMap.put("blockTime", block_time);
        r.setCode(Res.CODE_100);
        r.setMsg("");
        r.setData(resultMap);
        return r;
    }

    /**
     * get block list info
     *
     * @return
     */
    @GetMapping("getBlockListInfo")
    @ResponseBody
    public Res getBlockListInfo(PageObject page) {
        Res r = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> blockMapList = new ArrayList<>();
        Integer pageNumber = page.getPageNumber();
        Integer pageSize = page.getPageSize();
        Long count = blockRepository.count();
        Block lastBlock = initializationManager.getLastBlock();
        long start = lastBlock.getBlockHeight() - pageNumber * pageSize;
        long end = lastBlock.getBlockHeight() - (pageNumber - 1) * pageSize;
        List<Block> blockList = blockRepository.findOneInterval(start, end);
        for (Block block : blockList) {
            double totalFee = 0;
            for (Transaction transaction : block.getBlockTransactions()) {
                if (transaction.getTransType() == 2) {
                    totalFee += transaction.getTransactionHead().getTransValue();
                } else {
                    totalFee += transaction.getTransactionHead().getFee();
                }
            }
            BlockHead blockHead = block.getBlockHead();
            Map<String, Object> blockMap = new HashMap<>();
            blockMap.put("blockHeight", block.getBlockHeight());
            blockMap.put("date", DateUtil.stampToDate(blockHead.getTimeStamp()));
            blockMap.put("totalAmount", 0);
            blockMap.put("totalFee", initializationManager.unitConvert(totalFee, Constants.PTN, Constants.MAX_UNIT));
            blockMap.put("transactionNumber", block.getBlockTransactions().size());
            blockMap.put("foundryPublicKey", ECKey.pubkeyToAddress(Hex.toHexString(block.getFoundryPublicKey())));
            blockMap.put("blockSize", block.getBlockSize());
            blockMapList.add(blockMap);
        }
        Collections.reverse(blockMapList);
        resultMap.put("pageNumber", pageNumber);
        resultMap.put("count", count);
        resultMap.put("blockMapList", blockMapList);
        r.setCode(Res.CODE_100);
        r.setMsg("");
        r.setData(resultMap);
        return r;
    }

    /**
     * create wallet
     *
     * @param passWord
     * @return
     * @throws IOException
     */
    @PostMapping("createAccount")
    @ResponseBody
    public Res createAccount(@RequestParam("passWord") String passWord) throws IOException {
        Res res = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        if (!ValidateUtil.rexCheckPassword(passWord)) {
            res.code = Res.CODE_103;
            return res;
        }
        ECKey ecKey = new ECKey(new SecureRandom());
        String privateKey = Hex.toHexString(ecKey.getPrivKeyBytes());
        String publicKey = Hex.toHexString(ecKey.getPubKey());
        String address = Constants.ADDRESS_PREFIX + Hex.toHexString(ecKey.getAddress());
        String accountPath = System.getProperty("user.dir") + File.separator + "account";
        String addressPath = accountPath + File.separator + address;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Constants.PWD_FLAG);
        stringBuffer.append(passWord);
        stringBuffer.append(Constants.PUBKEY_FLAG);
        stringBuffer.append(publicKey);
        stringBuffer.append(Constants.PRIKEY_FLAG);
        stringBuffer.append(privateKey);
        File folder = new File(accountPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(addressPath);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtil.writeFileContent(addressPath, DeEnCode.encode(stringBuffer.toString()));
        resultMap.put(Constants.PRIKEY, privateKey);
        resultMap.put(Constants.ADDRESS, address);
        resultMap.put(Constants.PUBKEY, publicKey);
        res.code = Res.CODE_100;
        res.msg = "";
        res.data = resultMap;

        //update cache
        initializationManager.addAccountList(address, DeEnCode.encode(stringBuffer.toString()));
        return res;
    }


    @PostMapping("validataAddressIsTrans")
    @ResponseBody
    public Res validataAddressIsTrans(String address, String tokenName) {
        Res res = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        String pubkey = "";
        address = address.trim().replace(" ", "");
        Assets assets = assetsRepository.findByAddressAndTokenName(address, tokenName);
        if (assets != null) {
            pubkey = assets.getPubKey();
        }
        if (pubkey.equals("")) {
            res.code = Res.CODE_201;
        } else {
            res.code = Res.CODE_202;
        }
        return res;
    }


    /**
     * send coin
     *
     * @param transFrom     ： from
     * @param transTo       ： to
     * @param transValue    ： amount
     * @param fee           ： poundage(egg)
     * @param passWord      ： password
     * @param remark        ： remark
     * @param transToPubkey ： publickey base on to
     * @return
     */
    @PostMapping("transferAccounts")
    @ResponseBody
    public Res transferAccounts(String transFrom, String transTo, String transValue, String fee, String passWord, String remark, String transToPubkey, String tokenName) {
        Res res = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        long effectiveIncome = 0;
        long income = 0;
        long expenditure = 0;
        long tokenDicimal = 6;
        transFrom = transFrom.trim().replace(" ", "");
        transTo = transTo.trim().replace(" ", "");
        transValue = transValue.trim().replace(" ", "");
        tokenName = tokenName.trim().replace(" ", "");
        Token token = tokenRepository.findByName(tokenName);
        if (token != null) {
            tokenDicimal = token.getDecimals();
        }
        if (transValue.contains(".")) {
            String decimal = transValue.substring(transValue.indexOf(".") + 1);
            if (decimal.length() > tokenDicimal) {
                res.code = Res.CODE_128;
                return res;
            }
        }
        fee = fee.trim().replace(" ", "");
        transToPubkey = transToPubkey.trim().replace(" ", "");
        long transValueLong = initializationManager.unitConvert(transValue, tokenName, Constants.MINI_UNIT).longValue();
        BigDecimal feeDig = new BigDecimal(fee).multiply(new BigDecimal(Constants.MININUMUNIT));
        long feeLong = feeDig.longValue();
        if (transValueLong <= 0) {
            res.code = Res.CODE_126;
            return res;
        }
        if (syncBlockManager.isSyncBlock()) {
            res.code = Res.CODE_104;
            return res;
        }
        Map<String, String> localAccount = initializationManager.getAccountListByAddress(transFrom);
        String pwdFrom = localAccount.get(Constants.PWD);
        String pubkeyFrom = localAccount.get(Constants.PUBKEY);
        String prikeyFrom = localAccount.get(Constants.PRIKEY);

        if (pubkeyFrom.equals("")) {
            res.code = Res.CODE_105;
            res.msg = "";
            res.data = "";
            return res;
        }
        Assets assetsTo = assetsRepository.findByAddressAndTokenName(transTo, tokenName);
        String pubkeyTo = "";
        if (assetsTo != null) {
            pubkeyTo = assetsTo.getPubKey();
        }
        if (("".equals(pubkeyTo)) && (transToPubkey == null || transToPubkey.equals(""))) {
            res.code = Res.CODE_201;
            res.msg = "";
            res.data = "";
            return res;
        }
        if (("".equals(pubkeyTo)) && transToPubkey != null && !transToPubkey.equals("")) {
            pubkeyTo = transToPubkey;
        }

        if (!passWord.equals(pwdFrom)) {
            res.code = Res.CODE_301;
            res.msg = "";
            res.data = "";
            return res;
        }

        if (!ECKey.pubkeyToAddress(transToPubkey).equals(transTo) && transToPubkey != null && !transToPubkey.equals("")) {
            res.code = Res.CODE_401;
            res.msg = "";
            res.data = "";
            return res;
        }

        long timeStamp = System.currentTimeMillis();

        Assets assetsFrom = assetsRepository.findByAddressAndTokenName(transFrom, tokenName);
        if (assetsFrom != null) {
            effectiveIncome = assetsFrom.getTotalEffectiveIncome();
            income = assetsFrom.getTotalIncome();
            expenditure = assetsFrom.getTotalExpenditure();
        }
        long balance = effectiveIncome - expenditure;
        if (tokenName.equalsIgnoreCase(Constants.PTN)) {
            if (balance < (transValueLong + feeLong)) {
                res.code = Res.CODE_106;
                res.msg = "";
                res.data = "";
                return res;
            }
        } else {
            //token
            if (balance < transValueLong) {
                res.code = Res.CODE_106;
                return res;
            }
            //ptn
            Assets assetsFromPtn = assetsRepository.findByAddressAndTokenName(transFrom, Constants.PTN);
            System.out.println("######################-----assetsFromPtn:" + assetsFromPtn);
            effectiveIncome = 0;
            expenditure = 0;
            if (assetsFromPtn != null) {
                effectiveIncome = assetsFromPtn.getTotalEffectiveIncome();
                income = assetsFromPtn.getTotalIncome();
                expenditure = assetsFromPtn.getTotalExpenditure();
            }
            if (effectiveIncome - expenditure < feeLong) {
                res.code = Res.CODE_106;
                res.msg = "";
                res.data = "";
                return res;
            }
        }
        UnconfirmedTran unconfirmedTran = new UnconfirmedTran(pubkeyFrom, pubkeyTo, remark, tokenName, transValueLong, feeLong, timeStamp, 0);
        byte[] transSignature = JSON.toJSONString(ECKey.fromPrivate(Hex.decode(prikeyFrom)).sign(SHAEncrypt.sha3(SerializationUtils.serialize(unconfirmedTran.toString())))).getBytes();
        unconfirmedTran.setTransSignature(transSignature);
        InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(RESPONSE, NEW_TRANSACTION);
        builder.setUnconfirmedTran(MessageManager.createUnconfirmedTranMessage(unconfirmedTran));
        List<String> hostList = nioSocketChannelManager.getChannelHostList();
        builder.addAllNodeAddressList(hostList);
        nioSocketChannelManager.write(builder.build());
        resultMap.put("transHash", Hex.toHexString(transSignature));
        res.code = Res.CODE_200;
        res.msg = "";
        res.data = resultMap;
        return res;
    }


    /**
     * get transactions
     *
     * @param address ： wallet address
     * @return
     */
    @GetMapping("getTransactionList")
    @ResponseBody
    public Res getTransactionList(String address, String tokenName, PageObject page) {
        Res res = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> transactionList = new ArrayList<>();
        long confirm = 0;
        long blockHeight = 0;
        long count = 0;
        Integer pageNumber = page.getPageNumber();
        Integer pageSize = page.getPageSize();
        Iterable<Transaction> transactions = null;
        if (address.equalsIgnoreCase("all")) {
            transactions = transactionRepository.findTransaction(tokenName, new PageRequest(pageNumber - 1, pageSize, Sort.Direction.DESC, "blockHeight"));
            count = transactionRepository.findTransactionCount(tokenName);
        } else {
            Map<String, String> localAccount = initializationManager.getAccountListByAddress(address);
            String pubKey = localAccount.get(Constants.PUBKEY);
            if (pubKey.equals("")) {
                res.code = Res.CODE_102;
                return res;
            }
            transactions = transactionRepository.findAllByAccountAndTokenName(tokenName, pubKey, new PageRequest(pageNumber - 1, pageSize, Sort.Direction.DESC, "blockHeight"));
            count = transactionRepository.findAllByAccountAndTokenNameAndCount(tokenName, pubKey);
        }
        for (Transaction transaction : transactions) {
            TransactionHead transactionHead = transaction.getTransactionHead();
            blockHeight = transaction.getBlockHeight();
            Block lastBlock = initializationManager.getLastBlock();
            if (lastBlock != null) {
                confirm = lastBlock.getBlockHeight() - blockHeight;
            }
            Map<String, Object> transactionMap = new HashMap<>();
            transactionMap.put("date", DateUtil.stampToDate(transactionHead.getTimeStamp()));  // trans time
            transactionMap.put("amount", initializationManager.unitConvert(transactionHead.getTransValue(), tokenName, Constants.MAX_UNIT));  // deal amount
            transactionMap.put("fee", initializationManager.unitConvert(transactionHead.getFee(), Constants.PTN, Constants.MAX_UNIT));  // use eggs.
            transactionMap.put("blockHeight", blockHeight);  // height
            transactionMap.put("confirm", confirm < 0 || blockHeight < 0 ? 0 : confirm);  // confim
            transactionMap.put("from", transactionHead.getTransFrom());
            transactionMap.put("to", transactionHead.getTransTo());
            transactionList.add(transactionMap);
        }
        resultMap.put("transactionList", transactionList);
        resultMap.put("count", count);
        resultMap.put("pageNumber", pageNumber);
        res.setCode(Res.CODE_100);
        res.setMsg("");
        res.setData(resultMap);
        return res;
    }

    /**
     * get block sync status
     *
     * @return
     */
    @GetMapping("getsyncBlockSchedule")
    @ResponseBody
    public Res getsyncBlockSchedule() {
        Res res = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        BigDecimal blockSchedule = new BigDecimal(0);
        long needSyncBlockHeight = 0;
        long curBlockHeight = 0;
        boolean is = true;
        try {
            while (is) {
                needSyncBlockHeight = syncBlockManager.needSyncBlockHeight();
                if (needSyncBlockHeight != 0) {
                    is = false;
                }
            }
            curBlockHeight = initializationManager.getBlockHeight();
            blockSchedule = new BigDecimal((double) curBlockHeight / needSyncBlockHeight);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        logger.info("needSyncBlockHeight【{}】，curBlockHeight【{}】,blockSchedule:【{}】", needSyncBlockHeight, curBlockHeight, blockSchedule);
        blockSchedule.setScale(2, BigDecimal.ROUND_HALF_UP);
        if(blockSchedule.compareTo(new BigDecimal(1))==1){
            blockSchedule = new BigDecimal(1);
        }
        resultMap.put("blockSchedule", blockSchedule);
        res.setCode(Res.CODE_100);
        res.setMsg("");
        res.setData(resultMap);
        return res;
    }


    /**
     * export
     *
     * @return
     */
    @PostMapping("exportWallet")
    @ResponseBody
    public Res exportWallet(String address, String passWord, String mnemonic) {
        Res res = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> jsonMap = new HashMap<>();
        String SHAEncrypTmnemonic = Hex.toHexString(SHAEncrypt.sha3(mnemonic.getBytes()));
        Map<String, String> accountList = initializationManager.getAccountList();
        String account = accountList.get(address);
        if (account == null || "".equals(account)) {
            res.setCode(Res.CODE_102);
            return res;
        }
        String savaPwd = initializationManager.getAccountListByAddress(address).get(Constants.PWD);
        if (!passWord.equals(savaPwd)) {
            res.setCode(Res.CODE_301);
            return res;
        }
        jsonMap.put("mnemonic", SHAEncrypTmnemonic);
        jsonMap.put("account", account);
        resultMap.put("jsonStr", jsonMap);
        res.setCode(Res.CODE_107);
        res.setMsg("");
        res.setData(resultMap);
        return res;

    }

    /**
     * import
     *
     * @return
     */
    @PostMapping("importWallet")
    @ResponseBody
    public Res importWallet(String mnemonicText, String jsonStr) {
        Res res = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> jsonMap = new HashMap<>();
        String SHAEncrypTmnemonic = Hex.toHexString(SHAEncrypt.sha3(mnemonicText.getBytes()));
        try {
            jsonMap = JSON.parseObject(jsonStr, Map.class);
            String savaMnemonic = jsonMap.get("mnemonic").toString();
            String accountEncode = jsonMap.get("account").toString();
            if (!savaMnemonic.equals(SHAEncrypTmnemonic)) {
                res.setCode(Res.CODE_108);
                return res;
            }
            String account = DeEnCode.decode(accountEncode);
            String pubkey = account.substring(account.indexOf(Constants.PUBKEY_FLAG) + Constants.PUBKEY_FLAG.length(), account.indexOf(Constants.PRIKEY_FLAG));
            String address = ECKey.pubkeyToAddress(pubkey);
            Map<String, String> localAccount = initializationManager.getAccountListByAddress(address);
            if (!localAccount.get(Constants.PUBKEY).equals("")) {
                resultMap.put(Constants.ADDRESS, address);
                res.setCode(Res.CODE_109);
                res.setData(resultMap);
                return res;
            }

            String accountPath = System.getProperty("user.dir") + "\\account";
            String addressPath = System.getProperty("user.dir") + "\\account\\" + address;
            File dir = new File(accountPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(addressPath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileUtil.writeFileContent(addressPath, accountEncode);
            //update local Account
            initializationManager.addAccountList(address, accountEncode);
            resultMap.put(Constants.ADDRESS, address);
            res.setCode(Res.CODE_110);
            res.setData(resultMap);
            return res;
        } catch (Exception e) {
            res.setCode(Res.CODE_111);
            e.printStackTrace();
            return res;
        }

    }


    @PostMapping("getTransactionByHash")
    @ResponseBody
    public Res getTransactionByHash(String hash) {
        Res res = new Res();
        Map<String, Object> resultMap = new HashMap<>();
        List<Transaction> transactions = new ArrayList<>();
        List<Map<String, Object>> transactionList = new ArrayList<>();
        long confirm = 0;
        long blockHeight = 0;
        try {
            byte[] hashByte = Hex.decode(hash);
            transactions = transactionRepository.findAllByTransSignature(hashByte);
            for (Transaction transaction : transactions) {
                TransactionHead transactionHead = transaction.getTransactionHead();
                blockHeight = transaction.getBlockHeight();
                Block lastBlock = initializationManager.getLastBlock();
                if (lastBlock != null) {
                    confirm = lastBlock.getBlockHeight() - blockHeight;
                }
                Map<String, Object> transactionMap = new HashMap<>();
                transactionMap.put("coinType", transaction.getTokenName());
                transactionMap.put("date", DateUtil.stampToDate(transactionHead.getTimeStamp()));
                transactionMap.put("from", ECKey.pubkeyToAddress(transactionHead.getTransFrom()));
                transactionMap.put("to", ECKey.pubkeyToAddress(transactionHead.getTransTo()));
                transactionMap.put("amount", initializationManager.unitConvert(transactionHead.getTransValue(), transaction.getTokenName(), Constants.MAX_UNIT));
                transactionMap.put("fee", initializationManager.unitConvert(transactionHead.getFee(), transaction.getTokenName(), Constants.MAX_UNIT));
                transactionMap.put("blockHeight", blockHeight);
                transactionMap.put("confirm", confirm < 0 || blockHeight < 0 ? 0 : confirm);
                transactionMap.put("hash", Hex.toHexString(transaction.getTransSignature()));
                transactionList.add(transactionMap);
            }
            resultMap.put("transactions", transactionList);
            res.code = Res.CODE_100;
            res.data = resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            res.code = Res.CODE_114;
        }
        return res;
    }

    @GetMapping("getTokenList")
    @ResponseBody
    public Res getTokenList() {
        Res res = new Res();
        String fileContent = "";
        Set tokenList = new HashSet();
        String accountTokenInfoPath = System.getProperty("user.dir") + File.separator + "account" + File.separator + "accountTokenInfo";
        File file = new File(accountTokenInfoPath);
        try {
            if (file.exists()) {
                for (int i = 0; i < FileUtil.readFileByLines(accountTokenInfoPath).size(); i++) {
                    fileContent += FileUtil.readFileByLines(accountTokenInfoPath).get(i);
                }
                tokenList = JSON.parseObject(fileContent, Set.class);
            }
        } catch (Exception e) {
            tokenList = new HashSet();
        }
        res.setCode(100);
        res.setData(tokenList);
        return res;
    }

    @GetMapping("getTokenListByAddress")
    @ResponseBody
    public Res getTokenListByAddress(String address) {
        return this.getTokenList();
    }


    @GetMapping("tokenOpenAndClose")
    @ResponseBody
    public Res tokenOpenAndClose(int flag, String tokenName) {
        Res res = new Res();
        String fileContent = "";
        try {
            String accountTokenInfoPath = System.getProperty("user.dir") + File.separator + "account" + File.separator + "accountTokenInfo";
            File file = new File(accountTokenInfoPath);
            if (file.exists()) {
                for (int i = 0; i < FileUtil.readFileByLines(accountTokenInfoPath).size(); i++) {
                    fileContent += FileUtil.readFileByLines(accountTokenInfoPath).get(i);
                }
            }
            if (fileContent.equals("")) {
                if (flag == 0) {
                    System.out.println("");
                } else {
                    Set tokenList = new HashSet();
                    tokenList.add(tokenName);
                    FileUtil.writeFileContent(accountTokenInfoPath, JSON.toJSONString(tokenList));
                }
            } else {
                Set tokenList = JSON.parseObject(fileContent, Set.class);
                if (flag == 0) {
                    tokenList.remove(tokenName);
                } else {
                    tokenList.add(tokenName);
                }
                file.delete();
                file.createNewFile();
                fileContent = JSON.toJSONString(tokenList);
                FileUtil.writeFileContent(accountTokenInfoPath, fileContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("");
        }
        res.setCode(Res.CODE_122);
        return res;
    }


    @GetMapping("tokenOpenAndCloseList")
    @ResponseBody
    public Res tokenOpenAndCloseList() {
        List tokenInfoList = new ArrayList();
        String fileContent = "";
        Set tokenList = new HashSet();
        String accountTokenInfoPath = System.getProperty("user.dir") + File.separator + "account" + File.separator + "accountTokenInfo";
        File file = new File(accountTokenInfoPath);
        try {
            if (file.exists()) {
                for (int i = 0; i < FileUtil.readFileByLines(accountTokenInfoPath).size(); i++) {
                    fileContent += FileUtil.readFileByLines(accountTokenInfoPath).get(i);
                }
                tokenList = JSON.parseObject(fileContent, Set.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Res res = new Res();
        Iterator tokens = tokenRepository.findAll().iterator();
        while (tokens.hasNext()) {
            Token token = (Token) tokens.next();
            Map tokenMap = new HashMap();
            tokenMap.put("tokenName", token.getName());
            tokenMap.put("isOpen", tokenList.contains(token.getName()) ? 1 : 0);
            tokenInfoList.add(tokenMap);
        }
        res.setCode(Res.CODE_122);
        res.setData(tokenInfoList);
        return res;
    }

    @GetMapping("getBlockInfoByBlockHeight")
    @ResponseBody
    public Block getBlockInfoByBlockHeight(long blockHeight) {
        Block block = blockRepository.findBlockByBlockId(blockHeight);
        byte[] blockSHA = SHAEncrypt.SHA256(block.getBlockHead());
        block.setBlockHash(Hex.toHexString(blockSHA));
        return block;
    }

    @GetMapping("getBlockInfoByBlockHeightDealWithData")
    @ResponseBody
    public Res getBlockInfoByBlockHeightDealWithData(long blockHeight) {
        Res res = new Res();
        LinkedHashMap resultMap = new LinkedHashMap();
        Map blockMap = new HashMap();
        List<Map> transactionListMap = new ArrayList<>();
        int type = 0;
        double totalFee = 0;
        Map<String, BigDecimal> tokenTotalAmount = new HashMap<>();
        try {
            Block block = blockRepository.findBlockByBlockId(blockHeight);
            byte[] blockSHA = SHAEncrypt.SHA256(block.getBlockHead());
            for (Transaction transaction : block.getBlockTransactions()) {
                String tokenName = transaction.getTokenName();
                long value = transaction.getTransactionHead().getTransValue();
                long fee = transaction.getTransactionHead().getFee();
                if (transaction.getTransType() == 2) {
                    BigDecimal a = initializationManager.unitConvert(value, Constants.PTN, Constants.MAX_UNIT);
                    totalFee += a.doubleValue();
                } else {
                    BigDecimal MaxUnitValue = initializationManager.unitConvert(value, transaction.getTokenName(), Constants.MAX_UNIT);
                    BigDecimal MaxUnitFee = initializationManager.unitConvert(fee, Constants.PTN, Constants.MAX_UNIT);
                    if (tokenTotalAmount.containsKey(tokenName)) {
                        tokenTotalAmount.put(tokenName, tokenTotalAmount.get(tokenName).add(MaxUnitValue));
                    } else {
                        tokenTotalAmount.put(tokenName, MaxUnitValue);
                    }
                    totalFee += MaxUnitFee.doubleValue();
                }
                Map transactionMap = new HashMap();
                transactionMap.put("tokenName", transaction.getTokenName());
                transactionMap.put("from", ECKey.pubkeyToAddress(transaction.getTransFrom()));
                transactionMap.put("to", ECKey.pubkeyToAddress(transaction.getTransTo()));
                transactionMap.put("value", initializationManager.unitConvert(transaction.getTransactionHead().getTransValue(), transaction.getTokenName(), Constants.MAX_UNIT));
                transactionMap.put("fee", initializationManager.unitConvert(transaction.getTransactionHead().getFee(), Constants.PTN, Constants.MAX_UNIT));
                transactionMap.put("date", DateUtil.stampToDate(transaction.getTransactionHead().getTimeStamp()));
                transactionMap.put("transType", transaction.getTransType());
                transactionMap.put("confirm", initializationManager.getLastBlock().getBlockHeight() - transaction.getBlockHeight() < 0 ? 0 : initializationManager.getLastBlock().getBlockHeight() - transaction.getBlockHeight());
                transactionMap.put("remark", transaction.getRemark());
                transactionListMap.add(transactionMap);
            }
            blockMap.put("blockHeight", block.getBlockHeight());
            blockMap.put("blockSize", block.getBlockSize());
            blockMap.put("tokenTotalAmount", tokenTotalAmount);
            blockMap.put("totalFee", totalFee);
            blockMap.put("blockSignature", block.getBlockSignature());
            blockMap.put("foundryPublicKey", ECKey.pubkeyToAddress(Hex.toHexString(block.getFoundryPublicKey())));
            blockMap.put("date", DateUtil.stampToDate(block.getBlockHead().getTimeStamp()));
            blockMap.put("hashPrevBlock", Hex.toHexString(block.getBlockHead().getHashPrevBlock()));
            blockMap.put("hashBlock", Hex.toHexString(blockSHA));
            blockMap.put("blockTransactions", transactionListMap);
            resultMap.put("block", blockMap);
        } catch (Exception e) {
            res.setCode(Res.CODE_101);
            res.setData(resultMap);
            return res;
        }
        res.setCode(Res.CODE_100);
        res.setData(resultMap);
        return res;
    }


    @GetMapping("getCurNodeBlockHeight")
    @ResponseBody
    public long getCurNodeBlockHeight() {
        Block block = initializationManager.getLastBlock();
        return block.getBlockHeight();
    }

    /**
     * get account info(address+transactions)
     *
     * @param pubKey   ：account publickey
     * @param tokenName ： token
     * @return
     */
    @GetMapping("getTransactionByPubkey")
    @ResponseBody
    public Res getTransactionByPubkey(String pubKey, String tokenName, PageObject pageObject) {
        long t1 = System.currentTimeMillis();
        Res res = new Res();
        long blockHeight = 0;
        long confirm = 0;
        long type = 0;
        int pageNumber = pageObject.getPageNumber();
        int pageSize = pageObject.getPageSize();
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> accountMap = new HashMap<>();
        List<Map<String, Object>> transactionList = new ArrayList<>();
        int count = transactionRepository.findAllByAccountAndTokenNameAndTypeCount(pubKey, tokenName);
        long start = count - pageNumber * pageSize;
        start = start < 1 ? 1 : start;
        Block lastBlock = initializationManager.getLastBlock();
        List<Transaction> transactions = transactionRepository.findAllByAccountAndTokenNameAndType(pubKey, tokenName, new PageRequest(pageNumber - 1, pageSize, Sort.Direction.DESC, "blockHeight"));
        for (Transaction transaction : transactions) {

            blockHeight = transaction.getBlockHeight();
            if (lastBlock != null) {
                confirm = lastBlock.getBlockHeight() - blockHeight;
            }
            Map<String, Object> transactionMap = new HashMap<>();
            transactionMap.put("date", DateUtil.stampToDate(transaction.getTransactionHead().getTimeStamp()));
            if (transaction.getTransType() == 0) {
                type = transaction.getTransFrom().equals(pubKey) ? 1 : 0;
            } else if (transaction.getTransType() == 1) {
                type = 2;
            } else {
                type = 3;
            }
            transactionMap.put("type", type);
            transactionMap.put("coinType", transaction.getTokenName());
            transactionMap.put("amount", initializationManager.unitConvert(transaction.getTransactionHead().getTransValue(), transaction.getTokenName(), Constants.MAX_UNIT));
            transactionMap.put("fee", initializationManager.unitConvert(transaction.getTransactionHead().getFee(), Constants.PTN, Constants.MAX_UNIT));
            transactionMap.put("blockHeight", blockHeight);
            transactionMap.put("confirm", confirm < 0 || blockHeight < 0 ? 0 : confirm);
            transactionMap.put("hash", transaction.getTransSignature());
            transactionMap.put("from", ECKey.pubkeyToAddress(transaction.getTransFrom()));
            transactionMap.put("to", ECKey.pubkeyToAddress(transaction.getTransTo()));
            transactionList.add(transactionMap);
        }
        Collections.reverse(transactionList);
        resultMap.put("transactionList", transactionList);
        resultMap.put("count", count);
        resultMap.put("pageNumber", pageObject.getPageNumber());
        res.setCode(Res.CODE_100);
        res.setMsg("");
        res.setData(resultMap);
        return res;
    }
}
