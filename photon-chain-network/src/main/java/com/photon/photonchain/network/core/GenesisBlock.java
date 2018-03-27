package com.photon.photonchain.network.core;


import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.encryption.ECKey;
import com.photon.photonchain.storage.entity.*;
import com.photon.photonchain.storage.repository.AssetsRepository;
import com.photon.photonchain.storage.repository.BlockRepository;
import com.photon.photonchain.storage.repository.NodeAddressRepository;
import com.photon.photonchain.storage.repository.TransactionRepository;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:16:09 2018/1/12
 * @Modified by:
 */
@Component
public class GenesisBlock {
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private NodeAddressRepository nodeAddressRepository;
    @Autowired
    private AssetsRepository assetsRepository;

    private static final byte[] GENESIS_PUBLIC_KEY = {4, -42, 45, 16, 106, -67, 104, -24, 99, 55, 64, 118, -29, -113, 14, -110, -72, -83, -23, 44, 121, 127, -94, 124, -21, -51, 57, -29, 124, 108, 80, -107, -5, 61, 100, 20, 10, -94, 103, 36, 95, 55, 49, -87, 109, -9, -14, -68, 89, 12, -26, 60, -16, -54, -92, -116, 94, -68, 98, -63, 33, -40, 17, 124, -82};
    private static final byte[] ACCEPTER = {4, -100, -6, -56, -60, -117, -103, 17, 122, -34, 38, 107, 85, -95, -104, -46, -109, 110, -114, -40, -88, 41, 23, 109, 2, -94, 92, -49, 95, 53, -39, 126, 10, 35, 61, 70, -17, -23, 88, -123, 29, 41, 112, 105, 78, -72, 78, 78, -53, 72, -70, -94, 11, 21, -88, -4, 92, -63, -42, 8, -8, -115, -113, -96, -22};
    private static final String HASH_MERKLE_ROOT = "33373837333764633562333766396531376130333034306666363933353361313632346534323939356136613266616431363561383066303430393532316466";
    private static final String HASH_PREV_BLOCK = "aced000570";
    private static final String TRANS_SIGNATURE = "7b2272223a35363932313135303939363035343130343733383639313736393231363631353137323938373336343134363638313931303631353238373938373331393339393333303839363031333036312c2273223a35373733393232373033383932393134303933343137363832333435373130333139383233363538363435333336313938353239303437333737333339313135323631323032383430393137382c2276223a32387d";
    private static final String BLOCK_SIGNATURE = "7b2272223a37343432333133393532323134313837333530363335373437373533393035383538313136353833313936323539323634353439313133303535333433363730383335373135343436333638342c2273223a31393730333137333130373137323134343434323832313135363434343230303139393238343234373634393436343239343635363933353436343331323133353033363037393936353532302c2276223a32387d";
    private static final long RECEIVE_QUANTITY = 3500000000L * Constants.MININUMUNIT;
    public static final long GENESIS_TIME = 1514736000000L;

    @Transactional(rollbackFor = Exception.class)
    public void init() {
        if (blockRepository.count() == 0) {
            TransactionHead transactionHead = new TransactionHead(Hex.toHexString(GENESIS_PUBLIC_KEY), Hex.toHexString(ACCEPTER), RECEIVE_QUANTITY, 0, 1514736000000L);
            BlockHead blockHead = new BlockHead(Constants.BLOCK_VERSION, GENESIS_TIME, Constants.CUMULATIVE_DIFFICULTY, Hex.decode(HASH_PREV_BLOCK), Hex.decode(HASH_MERKLE_ROOT));
            //TODO
            Transaction transaction = new Transaction(Hex.decode(TRANS_SIGNATURE), transactionHead, 0, 0, Hex.toHexString(GENESIS_PUBLIC_KEY), Hex.toHexString(ACCEPTER), "", Constants.PTN, 1);
            List<Transaction> transactionList = new ArrayList<>();
            transactionList.add(transaction);
            Block block = new Block(0, 441, RECEIVE_QUANTITY, 0, Hex.decode(BLOCK_SIGNATURE), GENESIS_PUBLIC_KEY, blockHead, transactionList);
            Assets assets = new Assets(Hex.toHexString(ACCEPTER), Constants.PTN, RECEIVE_QUANTITY, 0, RECEIVE_QUANTITY, ECKey.pubkeyToAddress(Hex.toHexString(ACCEPTER)));
            transactionRepository.save(transaction);
            assetsRepository.save(assets);
            blockRepository.save(block);
        }
    }
}
