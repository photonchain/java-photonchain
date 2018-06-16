package com.photon.photonchain.network.ehcacheManager;

import com.photon.photonchain.network.core.GenesisBlock;
import com.photon.photonchain.network.utils.DeEnCode;
import com.photon.photonchain.network.utils.FileUtil;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.encryption.ECKey;
import com.photon.photonchain.storage.entity.AddressAndPubKey;
import com.photon.photonchain.storage.entity.Block;
import com.photon.photonchain.storage.entity.Transaction;
import com.photon.photonchain.storage.entity.UnconfirmedTran;
import com.photon.photonchain.storage.repository.AddressAndPubkeyRepository;
import com.photon.photonchain.storage.repository.TokenRepository;
import com.photon.photonchain.storage.repository.TransactionRepository;
import com.photon.photonchain.storage.repository.UnconfirmedTranRepository;
import net.sf.ehcache.Cache;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Author:PTN
 * @Description:
 * @Date:20:02 2018/1/18
 * @Modified by:
 */
@Component
public class UnconfirmedTranManager {

    private final static String UNCONFIRMEDTRAN_MAP = "unconfirmedTranMap";

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(UnconfirmedTranManager.class);

    private Cache unconfirmedTranCache = EhCacheManager.getCache("unconfirmedTranCache");

    @Autowired
    private UnconfirmedTranRepository unconfirmedTranRepository;

    public void setUnconfirmedTranMap(UnconfirmedTran unconfirmedtran) {
        Map<String, UnconfirmedTran> map = null;
        if (EhCacheManager.existKey(unconfirmedTranCache, UNCONFIRMEDTRAN_MAP)) {
            map = EhCacheManager.getCacheValue(unconfirmedTranCache, UNCONFIRMEDTRAN_MAP, Map.class);
        } else {
            map = new HashMap<>();
        }
        map.put(Hex.toHexString(unconfirmedtran.getTransSignature()), unconfirmedtran);
        EhCacheManager.put(unconfirmedTranCache, UNCONFIRMEDTRAN_MAP, map);
    }

    public Map getUnconfirmedTranMap() {
        Map<String, UnconfirmedTran> map = null;
        if (EhCacheManager.existKey(unconfirmedTranCache, UNCONFIRMEDTRAN_MAP)) {
            map = EhCacheManager.getCacheValue(unconfirmedTranCache, UNCONFIRMEDTRAN_MAP, Map.class);
        } else {
            map = new HashMap<>();
            EhCacheManager.put(unconfirmedTranCache, UNCONFIRMEDTRAN_MAP, map);
        }
        return map;
    }

    public void deleteUnconfirmedTrans(List<UnconfirmedTran> unconfirmedTrans) throws Exception {
        Map<String, UnconfirmedTran> map = getUnconfirmedTranMap();
        Map<String, UnconfirmedTran> mapcopy = map;
        for (String sign : map.keySet()) {
            for (UnconfirmedTran unconfirmedTran : unconfirmedTrans) {
                if (sign.equals(Hex.toHexString(unconfirmedTran.getTransSignature()))) {
                    mapcopy.remove(sign);
                }
            }
        }
        EhCacheManager.put(unconfirmedTranCache, UNCONFIRMEDTRAN_MAP, mapcopy);
    }

    public void deleteUnconfirmedTransBysignatures(List<byte[]> signatures) throws Exception {
        Map<String, UnconfirmedTran> map = getUnconfirmedTranMap();
        Map<String, UnconfirmedTran> mapcopy = map;
        for (String sign : map.keySet()) {
            for (byte[] signature : signatures) {
                System.out.println(Hex.toHexString(signature));
                if (sign.equals(Hex.toHexString(signature))) {
                    mapcopy.remove(sign);
                }
            }
        }
        EhCacheManager.put(unconfirmedTranCache, UNCONFIRMEDTRAN_MAP, mapcopy);
    }


    public void resetUnconfirmedTranMap() {
        Map map = new HashMap<>();
        EhCacheManager.put(unconfirmedTranCache, UNCONFIRMEDTRAN_MAP, map);
    }


    //contractAddress transType transFrom transTo timeStamp
    public List<UnconfirmedTran> queryUnconfirmedTran(String contractAddress, int transType, String transFrom, String transTo, long timeStamp) {
        UnconfirmedTran unconfirmedTran = new UnconfirmedTran();
        Map<String, UnconfirmedTran> map = getUnconfirmedTranMap();
        List<UnconfirmedTran> list = new ArrayList<>();
        List<String> li = new ArrayList<>();
        if (StringUtils.isNotBlank(contractAddress)) {
            unconfirmedTran.setContractAddress(contractAddress);
            li.add("ContractAddress");
        }
        if (-1 != transType) {
            unconfirmedTran.setTransType(transType);
            li.add("TransType");
        }
        if (StringUtils.isNotBlank(transFrom)) {
            unconfirmedTran.setTransFrom(transFrom);
            li.add("TransFrom");
        }
        if (StringUtils.isNotBlank(transTo)) {
            unconfirmedTran.setTransTo(transTo);
            li.add("TransTo");
        }
        if (0 < timeStamp) {
            unconfirmedTran.setTimeStamp(timeStamp);
            li.add("TimeStamp");
        }
        for (String transSignature : map.keySet()) {
            if (li.size() == 0) {
                list.add(map.get(transSignature));
            } else {
                if (classCompareValue(unconfirmedTran, map.get(transSignature), li)) {
                    list.add(map.get(transSignature));
                }
            }
        }
        return list;
    }


    public boolean classCompareValue(Object obj1, Object obj2, List<String> fieldName) {
        if (fieldName.size() == 0) {
            return false;
        }
        int flag = 0;
        if (obj1 == null) {
            return false;
        }
        try {
            Class beanClass = obj1.getClass();
            Method[] ms = beanClass.getMethods();
            Class beanClass1 = obj2.getClass();
            Method[] ms2 = beanClass1.getMethods();
            for (int i = 0; i < ms.length; i++) {

                if (!ms[i].getName().startsWith("get")) {
                    continue;
                }
                Object objValue = null;
                objValue = ms[i].invoke(obj1, new Object[]{});
                if (objValue == null) {
                    continue;
                }
                for (String s : fieldName) {
                    if (ms[i].getName().equalsIgnoreCase(s) || ms[i].getName().substring(3).equalsIgnoreCase(s)) {
                        if (s.equalsIgnoreCase("TimeStamp")) {
                            logger.info(Long.parseLong(objValue.toString()) + ":" + Long.parseLong(ms2[i].invoke(obj2, new Object[]{}).toString()));
                            if (Long.parseLong(objValue.toString()) > Long.parseLong(ms2[i].invoke(obj2, new Object[]{}).toString())) {
                                flag++;
                            }
                        } else {
                            if (objValue.equals(ms2[i].invoke(obj2, new Object[]{}))) {
                                flag++;
                            }
                        }
                    }
                }
            }
            if (flag == fieldName.size()) {
                return true;
            }
        } catch (Exception e) {
            logger.info("method error！" + e.toString());
        }
        return false;
    }
}
