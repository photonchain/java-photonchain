package com.photon.photonchain.network.core;

import com.photon.photonchain.network.ehcacheManager.AssetsManager;
import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.network.ehcacheManager.SyncBlockManager;
import com.photon.photonchain.network.ehcacheManager.UnconfirmedTranManager;
import com.photon.photonchain.storage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Author:PTN
 * @Description:
 * @Date:10:25 2018/3/27
 * @Modified by:
 */
@Component
public class ResetData {
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private GenesisBlock genesisBlock;
    @Autowired
    private Initialization initialization;
    @Autowired
    private SyncBlock syncBlock;
    @Autowired
    private UnconfirmedTranManager unconfirmedTranManager;

    @Transactional
    public void resetAll() {
        tokenRepository.truncate();
        //TODO:unconfirm
        unconfirmedTranManager.resetUnconfirmedTranMap();
        blockRepository.truncateRelation();
        transactionRepository.truncate();
        blockRepository.truncate();
        genesisBlock.init();
        initialization.init();
        syncBlock.init();
    }

    @Transactional
    public void resetAssets() {

    }
}
