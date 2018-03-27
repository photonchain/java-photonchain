package com.photon.photonchain.interfaces.controller;


import com.photon.photonchain.interfaces.utils.DeEnCode;
import com.photon.photonchain.interfaces.utils.FileUtil;
import com.photon.photonchain.interfaces.utils.Res;
import com.photon.photonchain.network.core.Initialization;
import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.storage.encryption.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:11:11 2017/11/11
 * @Modified by:
 */
@RestController
@RequestMapping("AccountController")
public class AccountController {
    private static Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    InitializationManager initializationManager;


}
