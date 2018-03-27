package com.photon.photonchain.interfaces.controller;


import com.alibaba.fastjson.JSON;
import com.photon.photonchain.interfaces.utils.DeEnCode;
import com.photon.photonchain.interfaces.utils.Res;
import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.network.ehcacheManager.NioSocketChannelManager;
import com.photon.photonchain.network.ehcacheManager.SyncBlockManager;
import com.photon.photonchain.network.proto.InesvMessage;
import com.photon.photonchain.network.proto.MessageManager;
import com.photon.photonchain.storage.encryption.ECKey;
import com.photon.photonchain.storage.encryption.SHAEncrypt;
import com.photon.photonchain.storage.entity.Transaction;
import com.photon.photonchain.storage.entity.TransactionHead;
import com.photon.photonchain.storage.repository.TransactionRepository;
import org.apache.commons.lang3.SerializationUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.photon.photonchain.network.proto.EventTypeEnum.EventType.NEW_TRANSACTION;
import static com.photon.photonchain.network.proto.MessageTypeEnum.MessageType.RESPONSE;

/**
 * @Author:PTN
 * @Description:
 * @Date:11:11 2017/11/11
 * @Modified by:
 */
@RestController
@RequestMapping("TransactionController")
public class TransactionController {
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;
    @Autowired
    SyncBlockManager syncBlockManager;
    @Autowired
    InitializationManager initializationManager;

    private static final String TOTAL_INCOME = "TOTAL_INCOME";
    private static final String TOTAL_EXPENDITURE = "TOTAL_EXPENDITURE";
    private static final String TOTAL_EFFECTIVE_INCOME = "TOTAL_EFFECTIVE_INCOME";


}