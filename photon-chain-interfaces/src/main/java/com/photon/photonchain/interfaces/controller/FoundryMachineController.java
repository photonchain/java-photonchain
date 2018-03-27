package com.photon.photonchain.interfaces.controller;


import com.photon.photonchain.interfaces.utils.DeEnCode;
import com.photon.photonchain.interfaces.utils.FileUtil;
import com.photon.photonchain.interfaces.utils.Res;
import com.photon.photonchain.network.core.FoundryMachine;
import com.photon.photonchain.network.ehcacheManager.FoundryMachineManager;
import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.network.ehcacheManager.SyncBlockManager;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.entity.Assets;
import com.photon.photonchain.storage.repository.AssetsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sun.rmi.runtime.Log;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @Author:PTN
 * @Description:
 * @Date:11:11 2017/11/11
 * @Modified by:
 */
@RestController
@RequestMapping("FoundryMachineController")
public class FoundryMachineController {
    @Autowired
    FoundryMachine foundryMachine;

    @Autowired
    FoundryMachineManager foundryMachineManager;

    @Autowired
    SyncBlockManager syncBlockManager;

    @Autowired
    InitializationManager initializationManager;

    @Autowired
    private AssetsRepository assetsRepository;

    @GetMapping("startFoundryMachine")
    @ResponseBody
    public Res startFoundryMachine(String passWord, String address) {
        Res res = new Res();
        if (syncBlockManager.isSyncBlock()) {
            res.setCode(Res.CODE_115);
            return res;
        }
        Map<String, String> accountInfo = initializationManager.getAccountListByAddress(address);
        String pwd = accountInfo.get(Constants.PWD);
        String priKey = accountInfo.get(Constants.PRIKEY);
        String pubKey = accountInfo.get(Constants.PUBKEY);
        if (pubKey.equals("")) {
            res.setCode(Res.CODE_102);
            return res;
        }
        if (!pwd.equals(passWord)) {
            res.code = Res.CODE_301;
            return res;
        }
        Assets assets = assetsRepository.findByPubKeyAndTokenName(pubKey, Constants.PTN);
        long effectiveIncome = 0;
        long income = 0;
        long expenditure = 0;
        if (assets != null) {
            effectiveIncome = assets.getTotalEffectiveIncome();
            income = assets.getTotalIncome();
            expenditure = assets.getTotalExpenditure();
        }
        if (effectiveIncome - expenditure <= 0) {
            res.code = Res.CODE_106;
            return res;
        }
        foundryMachineManager.setFoundryMachine(pubKey, true);
        foundryMachine.init(pubKey, priKey);
        res.code = Res.CODE_116;
        return res;
    }

    @GetMapping("stopFoundryMachine")
    @ResponseBody
    public Res stopFoundryMachine(String passWord, String address) {
        Res res = new Res();
        if (syncBlockManager.isSyncBlock()) {
            res.setCode(Res.CODE_117);
            return res;
        }
        Map<String, String> accountInfo = initializationManager.getAccountListByAddress(address);
        String pwd = accountInfo.get(Constants.PWD);
        String priKey = accountInfo.get(Constants.PRIKEY);
        String pubKey = accountInfo.get(Constants.PUBKEY);
        if (pubKey.equals("")) {
            res.setCode(Res.CODE_102);
            return res;
        }
        if (!pwd.equals(passWord)) {
            res.code = Res.CODE_301;
            return res;
        }
        foundryMachineManager.setFoundryMachine(pubKey, false);
        res.code = Res.CODE_118;
        return res;
    }

    @GetMapping("foundryMachineState")
    @ResponseBody
    public Res FoundryMachineState(String address) {
        Res res = new Res();
        Map<String, String> accountInfo = initializationManager.getAccountListByAddress(address);
        String pubKey = accountInfo.get(Constants.PUBKEY);
        if (pubKey.equals("")) {
            res.setCode(Res.CODE_102);
            return res;
        }
        if (foundryMachineManager.foundryMachineIsStart(pubKey)) {
            res.code = Res.CODE_120;
        } else {
            res.code = Res.CODE_121;
        }
        return res;
    }

}
