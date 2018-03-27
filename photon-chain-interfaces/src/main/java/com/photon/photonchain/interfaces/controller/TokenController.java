package com.photon.photonchain.interfaces.controller;

import com.alibaba.fastjson.JSON;
import com.photon.photonchain.interfaces.utils.Res;
import com.photon.photonchain.interfaces.utils.ValidateUtil;
import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.network.ehcacheManager.NioSocketChannelManager;
import com.photon.photonchain.network.proto.InesvMessage;
import com.photon.photonchain.network.proto.MessageManager;
import com.photon.photonchain.network.utils.TokenUtil;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.encryption.ECKey;
import com.photon.photonchain.storage.encryption.SHAEncrypt;
import com.photon.photonchain.storage.entity.Assets;
import com.photon.photonchain.storage.entity.Token;
import com.photon.photonchain.storage.entity.UnconfirmedTran;
import com.photon.photonchain.storage.repository.AssetsRepository;
import com.photon.photonchain.storage.repository.TokenRepository;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.photon.photonchain.network.proto.EventTypeEnum.EventType.NEW_TOKEN;
import static com.photon.photonchain.network.proto.MessageTypeEnum.MessageType.RESPONSE;

/**
 * @Author:PTN
 * @Description:
 * @Date:11:11 2017/11/11
 * @Modified by:
 */
@Controller
@RequestMapping("TokenController")
public class TokenController {

    private static Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;

    @Autowired
    private InitializationManager initializationManager;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AssetsRepository assetsRepository;

    @PostMapping("addToken")
    @ResponseBody
    public Res addToken(String address, String symbol, String name, String decimals, String tokenAmount, String remark, String passWord) {
        logger.info("addToken【{}】", name);
        Res res = new Res();
        if (name.length() < 3 || name.length() > 20 || !ValidateUtil.checkLetter(name)) {
            res.setCode(Res.CODE_127);
            return res;
        }
        if (tokenRepository.findByName(name) != null) {
            res.setCode(Res.CODE_123);
            return res;
        }
        if (!ValidateUtil.checkPositiveInteger(tokenAmount) || tokenAmount.length() > 12) {
            res.setCode(Res.CODE_124);
            return res;
        }
        int decimalsInt = Integer.parseInt(decimals);
        long tokenAmountLong = Long.parseLong(tokenAmount);
        long tokenAmountLongMiniUnit = 1;
        for (int i = 0; i < decimalsInt; i++) {
            tokenAmountLongMiniUnit *= 10;
        }
        Map<String, String> localAccount = initializationManager.getAccountListByAddress(address);
        String pubKey = localAccount.get(Constants.PUBKEY);
        String priKey = localAccount.get(Constants.PRIKEY);
        String savePwd = localAccount.get(Constants.PWD);
        if (!savePwd.equals(passWord)) {
            res.setCode(Res.CODE_301);
            return res;
        }
        if (pubKey.equals("")) {
            res.setCode(Res.CODE_102);
            return res;
        }
        if (tokenAmountLong < 1000 || tokenAmountLong > 100000000000l) {
            res.setCode(Res.CODE_124);
            return res;
        }
        if (decimalsInt < 6 || decimalsInt > 12) {
            res.setCode(Res.CODE_125);
            return res;
        }
        Token token = new Token(symbol, name, "", decimalsInt);
        long fee = Double.valueOf(TokenUtil.TokensRate(name) * tokenAmountLong * tokenAmountLongMiniUnit * Constants.MININUMUNIT).longValue();
        if (fee == 0) fee = 1;

        Assets assets = assetsRepository.findByPubKeyAndTokenName(pubKey, Constants.PTN);
        long effectiveIncome = 0;
        long income = 0;
        long expenditure = 0;
        if (assets != null) {
            effectiveIncome = assets.getTotalEffectiveIncome();
            income = assets.getTotalIncome();
            expenditure = assets.getTotalExpenditure();

        }
        long balance = effectiveIncome - expenditure;
        logger.info("#########---balance【{}】,fee【{}】,value【{}】", balance, fee, tokenAmountLongMiniUnit * tokenAmountLong);
        if (balance < fee) {
            res.setCode(Res.CODE_106);
            return res;
        }
        UnconfirmedTran unconfirmedTran = new UnconfirmedTran(pubKey, pubKey, remark, name, tokenAmountLongMiniUnit * tokenAmountLong, fee, System.currentTimeMillis(), 1);
        byte[] transSignature = JSON.toJSONString(ECKey.fromPrivate(Hex.decode(priKey)).sign(SHAEncrypt.sha3(SerializationUtils.serialize(unconfirmedTran.toString())))).getBytes();
        unconfirmedTran.setTransSignature(transSignature);
        InesvMessage.Message.Builder builder = MessageManager.createMessageBuilder(RESPONSE, NEW_TOKEN);
        builder.setUnconfirmedTran(MessageManager.createUnconfirmedTranMessage(unconfirmedTran));
        builder.setToken(MessageManager.createTokenMessage(token));
        List<String> hostList = nioSocketChannelManager.getChannelHostList();
        builder.addAllNodeAddressList(hostList);
        nioSocketChannelManager.write(builder.build());
        res.setCode(Res.CODE_119);
        return res;
    }


    @GetMapping("getAddTokenFee")
    @ResponseBody
    public Res getAddTokenFee(String name, String tokenAmount, int decimal) {
        Res res = new Res();
        Map resultMap = new HashMap();
        long tokenAmountLongMiniUnit = 1;
        for (int i = 0; i < decimal; i++) {
            tokenAmountLongMiniUnit *= 10;
        }
        long tokenAmountLong = Long.parseLong(tokenAmount);
        long fee = Double.valueOf(TokenUtil.TokensRate(name) * tokenAmountLong * tokenAmountLongMiniUnit * Constants.MININUMUNIT).longValue();
        if (fee == 0) fee = 1;
        resultMap.put("fee", new BigDecimal(fee).divide(new BigDecimal(Constants.MININUMUNIT)).setScale(6, BigDecimal.ROUND_HALF_UP));
        res.setCode(Res.CODE_100);
        res.setData(resultMap);
        return res;
    }


    @GetMapping("getAllToken")
    @ResponseBody
    public Iterable<Token> getTokenList() {
        return tokenRepository.findAll();
    }

}
