package com.photon.photonchain.network.core;


import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.encryption.ECKey;
import com.photon.photonchain.storage.encryption.SHAEncrypt;
import com.photon.photonchain.storage.entity.*;
import com.photon.photonchain.storage.repository.BlockRepository;
import com.photon.photonchain.storage.repository.NodeAddressRepository;
import com.photon.photonchain.storage.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    final static Logger logger = LoggerFactory.getLogger(GenesisBlock.class);


    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private NodeAddressRepository nodeAddressRepository;

    private static final byte[] GENESIS_PUBLIC_KEY = {4, -2, 14, 62, 39, 76, -61, -26, -94, 95, 31, -45, -85, -2, -71, 79, 47, -34, 92, -37, -86, 8, -70, -1, 77, -15, -66, 16, 8, 29, -113, -124, -90, -113, -22, -3, 28, -31, -37, -55, 48, -63, 69, 65, 82, 68, -33, -13, -116, -43, -91, 115, 64, -104, 115, 92, 97, 62, -1, -51, 127, 87, -33, -25, -33};
    public static final byte[] ACCEPTER = {4, 63, -120, 63, -117, 112, 46, -55, 77, -52, 86, 34, 9, -102, -57, 49, 88, 34, 0, -60, -106, -15, 16, -97, -38, 57, -45, -38, 13, -128, -18, -79, 35, -91, 51, -98, 54, 9, 83, -66, -53, -35, -42, 10, -92, 75, -127, 50, 49, 80, 7, 27, -12, -118, 33, -24, -30, -107, -16, -100, -113, 60, 20, 72, 8};
    private static final String HASH_PREV_BLOCK = "aced000570";
    public static final long RECEIVE_QUANTITY = 3500000000L * Constants.MININUMUNIT;
    public static final long GENESIS_TIME = 1514736000000L;
    private static final String TRANS_SIGNATURE = "7b2272223a39333039303036373830343933313231333232313732313136303733333935393537353239363931343532333430393933333933333935343032353230383636303333323330373538303638352c2273223a32323037323533373330393436373539393635343930353734353639333839393134313339333933323437393430313333353330353733343239353236333135323839303237303530343932362c2276223a32387d";
    private static final String HASH_MERKLE_ROOT = "62386137363564613738363836333134663631643433663561363338646231613331336333663764633030346264386532326537626563623464666561643635";
    private static final String BLOCK_SIGNATURE = "7b2272223a36333339353935353436383536363032323933343839313835373333333131323630383436373530383037383235333437393834323535303739373031333034323931383235373138363437332c2273223a32373033303034383933393834363532363730373337373038313634313134343036343431373931393032393830343931393132383436323538303339353333383330333736313238363734332c2276223a32377d";

    @Transactional(rollbackFor = Exception.class)
    public void init() {
        if (blockRepository.count() == 0) {
            logger.info("MainAccount:" + ECKey.pubkeyToAddress(Hex.toHexString(ACCEPTER)));
            TransactionHead transactionHead = new TransactionHead(Hex.toHexString(GENESIS_PUBLIC_KEY), Hex.toHexString(ACCEPTER), RECEIVE_QUANTITY, 0, GENESIS_TIME);
            BlockHead blockHead = new BlockHead(Constants.BLOCK_VERSION, GENESIS_TIME, Constants.CUMULATIVE_DIFFICULTY, Hex.decode(HASH_PREV_BLOCK), Hex.decode(HASH_MERKLE_ROOT));
            Transaction transaction = new Transaction(Hex.decode(TRANS_SIGNATURE), transactionHead, 0, 0, Hex.toHexString(GENESIS_PUBLIC_KEY), Hex.toHexString(ACCEPTER), "", Constants.PTN, 1, RECEIVE_QUANTITY, 0);
            List<Transaction> transactionList = new ArrayList<>();
            transactionList.add(transaction);
            Block block = new Block(0, 441, RECEIVE_QUANTITY, 0, Hex.decode(BLOCK_SIGNATURE), GENESIS_PUBLIC_KEY, blockHead, transactionList);
            block.setBlockHash(Hex.toHexString(SHAEncrypt.SHA256(blockHead)));
            transactionRepository.save(transaction);
            blockRepository.save(block);
        }
    }
}
