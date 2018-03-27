package com.photon.photonchain.network.core;

import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.network.utils.FileUtil;
import com.photon.photonchain.storage.entity.Block;
import com.photon.photonchain.storage.entity.Token;
import com.photon.photonchain.storage.entity.Transaction;
import com.photon.photonchain.storage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author:PTN
 * @Description:
 * @Date:20:06 2018/1/18
 * @Modified by:
 */
@Component
public class Initialization {
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private InitializationManager initializationManager;
    @Autowired
    private NodeAddressRepository nodeAddressRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    public void init() {
        Block lastBlock = blockRepository.findLastOne(new PageRequest(0, 1, Sort.Direction.DESC, "blockHeight")).get(0);
        initializationManager.setLastBlock(lastBlock);
        String accountPath = System.getProperty("user.dir") + File.separator + "account";
        Map<String, String> accountList = FileUtil.traverseFolder(accountPath);
        initializationManager.setAccountList(accountList);
        List<String> nodeList = nodeAddressRepository.findAllHexIp();
        initializationManager.setNodeList(nodeList);
        String accountTokenInfoPath = System.getProperty("user.dir") + File.separator + "account" + File.separator + "accountTokenInfo";
        File file = new File(accountTokenInfoPath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
        }
        //Token cache
        Iterator tokens = tokenRepository.findAll().iterator();
        while (tokens.hasNext()) {
            Token token = (Token) tokens.next();
            initializationManager.addTokenDecimal(token.getName(), token.getDecimals());
        }
        //set last transaction
        Transaction lastTransaction = transactionRepository.findTransactionOne(new PageRequest(0, 1, Sort.Direction.DESC, "blockHeight")).get(0);
        initializationManager.setLastTransaction(lastTransaction);
    }
}


