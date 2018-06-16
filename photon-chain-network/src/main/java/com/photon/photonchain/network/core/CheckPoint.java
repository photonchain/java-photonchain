package com.photon.photonchain.network.core;

import com.photon.photonchain.storage.encryption.SHAEncrypt;
import com.photon.photonchain.storage.entity.Block;
import com.photon.photonchain.storage.repository.BlockRepository;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:PTN
 * @Description:
 * @Date:17:50 2018/3/26
 * @Modified by:
 */
@Component
public class CheckPoint {
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private ResetData resetData;

    public static final Map<Long, String> CHECK_POINT_MAP = new HashMap<>();

    @PostConstruct
    public void init() {
        CHECK_POINT_MAP.put(0L, "38353364653762323463386564623831626434333866346337366536373530393939303364393563383564363636303233353163313931316238643839373939");
    }

    public void checkDate() {
        for (long blockHeight : CHECK_POINT_MAP.keySet()) {
            Block block = blockRepository.findBlockByBlockId(blockHeight);
            if (block != null) {
                boolean verifyPrevHash = Arrays.equals(Hex.decode(block.getBlockHash()), Hex.decode(CHECK_POINT_MAP.get(blockHeight)));
                if (!verifyPrevHash) {
                    resetData.resetAll();
                    break;
                }
            }
        }
    }

    public boolean checkDate(List<Block> blockList) {
        for (Block block : blockList) {
            if (CHECK_POINT_MAP.containsKey(block.getBlockHeight())) {
                boolean verifyPrevHash = Arrays.equals(Hex.decode(block.getBlockHash()), Hex.decode(CHECK_POINT_MAP.get(block.getBlockHeight())));
                if (!verifyPrevHash) {
                    return false;
                }
            }
        }
        return true;
    }
}
