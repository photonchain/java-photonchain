package com.photon.photonchain.network.core;

import com.alibaba.fastjson.JSON;
import com.photon.photonchain.network.ehcacheManager.InitializationManager;
import com.photon.photonchain.network.ehcacheManager.NioSocketChannelManager;
import com.photon.photonchain.network.utils.FileUtil;
import com.photon.photonchain.network.utils.FoundryUtils;
import com.photon.photonchain.network.utils.TokenUtil;
import com.photon.photonchain.storage.constants.Constants;
import com.photon.photonchain.storage.encryption.ECKey;
import com.photon.photonchain.storage.encryption.HashMerkle;
import com.photon.photonchain.storage.encryption.SHAEncrypt;
import com.photon.photonchain.storage.entity.*;
import com.photon.photonchain.storage.repository.AssetsRepository;
import com.photon.photonchain.storage.repository.TokenRepository;
import com.photon.photonchain.storage.repository.UnconfirmedTranRepository;
import org.apache.commons.lang3.SerializationUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:14:16 2018/2/6
 * @Modified by:
 */
@Component
public class Verification {
    @Autowired
    private InitializationManager initializationManager;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UnconfirmedTranRepository unconfirmedTranRepository;
    @Autowired
    private AssetsRepository assetsRepository;
    @Autowired
    private NioSocketChannelManager nioSocketChannelManager;
    @Autowired
    private AssetsHandle assetsHandle;

    public boolean verificationUnconfirmedTran(UnconfirmedTran unconfirmedTran) {
        boolean adopt = false;
        boolean verificationTransSignature = ECKey.fromPublicOnly(Hex.decode(unconfirmedTran.getTransFrom())).verify(SHAEncrypt.sha3(SerializationUtils.serialize(unconfirmedTran.toString())), JSON.parseObject(new String(unconfirmedTran.getTransSignature()), ECKey.ECDSASignature.class));
        boolean verifyTrade = false;
        Assets fromAssets = assetsRepository.findByPubKeyAndTokenName(unconfirmedTran.getTransFrom(), unconfirmedTran.getTokenName());
        Assets fromAssetsPtn = assetsRepository.findByPubKeyAndTokenName(unconfirmedTran.getTransFrom(), Constants.PTN);
        boolean verifyFee = false;
        switch (unconfirmedTran.getTransType()) {
            case 0:
                if (!Constants.PTN.equals(unconfirmedTran.getTokenName())) {
                    boolean verifyTransfer = fromAssets.getTotalEffectiveIncome() - fromAssets.getTotalExpenditure() >= unconfirmedTran.getTransValue() ? true : false;
                    verifyFee = fromAssetsPtn.getTotalEffectiveIncome() - fromAssetsPtn.getTotalExpenditure() >= unconfirmedTran.getFee() ? true : false;
                    if (verifyTransfer && verifyFee) {
                        verifyTrade = true;
                    }
                } else {
                    boolean verifyTransfer = fromAssets.getTotalEffectiveIncome() - fromAssets.getTotalExpenditure() >= unconfirmedTran.getTransValue() + unconfirmedTran.getFee() ? true : false;
                    if (verifyTransfer) {
                        verifyTrade = true;
                    }
                }
                break;
            case 1:
                long fee = Double.valueOf(TokenUtil.TokensRate(unconfirmedTran.getTokenName()) * unconfirmedTran.getTransValue() * Constants.MININUMUNIT).longValue();
                if (fee == 0) fee = 1;
                verifyFee = fromAssetsPtn.getTotalEffectiveIncome() - fromAssetsPtn.getTotalExpenditure() >= fee ? true : false;
                if (fee == unconfirmedTran.getFee() && verifyFee) {
                    verifyTrade = true;
                }
                break;
            default:
                verifyTrade = false;
                break;
        }
        if (verificationTransSignature && verifyTrade) {
            adopt = true;
        }
        return adopt;
    }

    public boolean verificationToken(Token token) {
        Token existsToken = tokenRepository.findByName(token.getName());
        if (existsToken != null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean verificationBlock(Block block) {
        boolean verifySignature = ECKey.fromPublicOnly(block.getFoundryPublicKey()).verify(SHAEncrypt.sha3(SerializationUtils.serialize(block.getBlockHead())), JSON.parseObject(new String(block.getBlockSignature()), ECKey.ECDSASignature.class));
        boolean verifyPrevHash = Arrays.equals(block.getBlockHead().getHashPrevBlock(), SHAEncrypt.SHA256(initializationManager.getLastBlock().getBlockHead()));
        List<Transaction> transactionList = block.getBlockTransactions();
        List<byte[]> transactionSHAList = new ArrayList<>();
        int mining = 0;
        for (Transaction transaction : transactionList) {
            boolean verifyTranSignature = ECKey.fromPublicOnly(Hex.decode(transaction.getTransFrom())).verify(SHAEncrypt.sha3(SerializationUtils.serialize(transaction.toSignature())), JSON.parseObject(new String(transaction.getTransSignature()), ECKey.ECDSASignature.class));
            if (!verifyTranSignature) {
                return false;
            }
            if (transaction.getTransType() != 2) {
                UnconfirmedTran existsUnconfirmedTran = unconfirmedTranRepository.findBySignature(transaction.getTransSignature());
                if (existsUnconfirmedTran == null) {
                    return false;
                }
            } else {
                mining++;
                if (!transaction.getTransTo().equals(Hex.toHexString(block.getFoundryPublicKey()))) {
                    return false;
                }
                int diffYear = FoundryUtils.getDiffYear(GenesisBlock.GENESIS_TIME, block.getBlockHead().getTimeStamp());
                long blockReward = FoundryUtils.getBlockReward(block.getFoundryPublicKey(), diffYear, block.getBlockHeight(), initializationManager, false);
                if (transaction.getTransactionHead().getTransValue() != block.getTotalFee() + blockReward) {
                    return false;
                }
                if (mining != 1) {
                    return false;
                }
            }
            transactionSHAList.add(SHAEncrypt.SHA256(transaction.getTransactionHead().toString()));
        }
        byte[] hashMerkleRoot = transactionSHAList.isEmpty() ? new byte[]{} : HashMerkle.getHashMerkleRoot(transactionSHAList);
        boolean verifyHashMerkleRoot = Arrays.equals(hashMerkleRoot, block.getBlockHead().getHashMerkleRoot());
        boolean verifyFoundrer = false;
        Assets foundrerAssetsPtn = assetsRepository.findByPubKeyAndTokenName(Hex.toHexString(block.getFoundryPublicKey()), Constants.PTN);
        long balance = foundrerAssetsPtn.getTotalEffectiveIncome() - foundrerAssetsPtn.getTotalExpenditure();
        if (balance > 0) {
            verifyFoundrer = true;
        }
        if (verifySignature && verifyPrevHash && verifyHashMerkleRoot && verifyFoundrer) {
            return true;
        }
        return false;
    }

    public List<Block> verificationSyncBlockList(List<Block> syncBlockList, String macAddress) {
        List<Block> saveBlockList = new ArrayList<>();
        Block verificationBlock = initializationManager.getLastBlock();
        ArrayList<Assets> assetsList = new ArrayList<>();
        for (Block block : syncBlockList) {
            if (verificationBlock.getBlockHeight() >= block.getBlockHeight()) {
                continue;
            }
            if (verificationBlock(block, verificationBlock, FileUtil.clone(assetsList))) {
                saveBlockList.add(block);
                for (Transaction transaction : block.getBlockTransactions()) {
                    assetsList = assetsHandle.addTempAssetsList(assetsList, transaction);
                }
            } else {
                saveBlockList = new ArrayList<>();
                if (syncBlockList.size() > 0) {
                    nioSocketChannelManager.removeTheMac(macAddress);
                }
                break;
            }
            verificationBlock = block;
        }
        return saveBlockList;
    }

    private boolean verificationBlock(Block block, Block verificationBlock, List<Assets> assetsList) {
        boolean verifySignature = ECKey.fromPublicOnly(block.getFoundryPublicKey()).verify(SHAEncrypt.sha3(SerializationUtils.serialize(block.getBlockHead())), JSON.parseObject(new String(block.getBlockSignature()), ECKey.ECDSASignature.class));
        boolean verifyPrevHash = Arrays.equals(block.getBlockHead().getHashPrevBlock(), SHAEncrypt.SHA256(verificationBlock.getBlockHead()));
        List<Transaction> transactionList = block.getBlockTransactions();
        List<byte[]> transactionSHAList = new ArrayList<>();
        int mining = 0;
        for (Transaction transaction : transactionList) {
            boolean verifyTranSignature = ECKey.fromPublicOnly(Hex.decode(transaction.getTransFrom())).verify(SHAEncrypt.sha3(SerializationUtils.serialize(transaction.toSignature())), JSON.parseObject(new String(transaction.getTransSignature()), ECKey.ECDSASignature.class));
            if (!verifyTranSignature) {
                return false;
            }
            Assets fromAssets = assetsHandle.getFirstByList(assetsList, transaction.getTransFrom(), transaction.getTokenName());
            Assets toAssets = assetsHandle.getFirstByList(assetsList, transaction.getTransTo(), transaction.getTokenName());
            Assets fromAssetsPtn = assetsHandle.getFirstByList(assetsList, transaction.getTransFrom(), Constants.PTN);
            boolean verifyFee = false;
            switch (transaction.getTransType()) {
                case 0:
                    if (!Constants.PTN.equals(transaction.getTokenName())) {
                        boolean verifyTransfer = fromAssets.getTotalEffectiveIncome() - fromAssets.getTotalExpenditure() >= transaction.getTransactionHead().getTransValue() ? true : false;
                        verifyFee = fromAssetsPtn.getTotalEffectiveIncome() - fromAssetsPtn.getTotalExpenditure() >= transaction.getTransactionHead().getFee() ? true : false;
                        if (!verifyTransfer || !verifyFee) {
                            return false;
                        }
                    } else {
                        boolean verifyTransfer = fromAssets.getTotalEffectiveIncome() - fromAssets.getTotalExpenditure() >= transaction.getTransactionHead().getTransValue() + transaction.getTransactionHead().getFee() ? true : false;
                        if (!verifyTransfer) {
                            return false;
                        }
                    }
                    break;
                case 1:
                    long fee = Double.valueOf(TokenUtil.TokensRate(transaction.getTokenName()) * transaction.getTransactionHead().getTransValue() * Constants.MININUMUNIT).longValue();
                    verifyFee = fromAssetsPtn.getTotalEffectiveIncome() - fromAssetsPtn.getTotalExpenditure() >= fee ? true : false;
                    if (fee == 0) fee = 1;
                    if (fee != transaction.getTransactionHead().getFee() || !verifyFee) {
                        return false;
                    }
                    break;
                case 2:
                    mining++;
                    if (!transaction.getTransTo().equals(Hex.toHexString(block.getFoundryPublicKey()))) {
                        return false;
                    }
                    int diffYear = FoundryUtils.getDiffYear(GenesisBlock.GENESIS_TIME, block.getBlockHead().getTimeStamp());
                    long blockReward = FoundryUtils.getBlockReward(block.getFoundryPublicKey(), diffYear, block.getBlockHeight(), initializationManager, true);
                    if (transaction.getTransactionHead().getTransValue() != block.getTotalFee() + blockReward) {
                        return false;
                    }
                    if (mining != 1) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
            if (transaction.getTransType() != 2) {
                assetsList = assetsHandle.addTempAssetsList(assetsList, transaction.getUnconfirmedTran());
            }
            transactionSHAList.add(SHAEncrypt.SHA256(transaction.getTransactionHead().toString()));
        }
        byte[] hashMerkleRoot = transactionSHAList.isEmpty() ? new byte[]{} : HashMerkle.getHashMerkleRoot(transactionSHAList);
        boolean verifyHashMerkleRoot = Arrays.equals(hashMerkleRoot, block.getBlockHead().getHashMerkleRoot());
        if (verifySignature && verifyPrevHash && verifyHashMerkleRoot) {
            return true;
        }
        return false;
    }

    public List<UnconfirmedTran> verificationSyncUnconfirmedTranList(List<UnconfirmedTran> syncUnconfirmedTranList) {
        List<UnconfirmedTran> saveUnconfirmedTranList = new ArrayList<>();
        Iterable<UnconfirmedTran> existsUnconfirmedTran = unconfirmedTranRepository.findAll();
        for (UnconfirmedTran unconfirmedTran : existsUnconfirmedTran) {
            syncUnconfirmedTranList.remove(unconfirmedTran);
        }
        List<Assets> assetsList = new ArrayList<>();
        for (UnconfirmedTran unconfirmedTran : syncUnconfirmedTranList) {
            boolean verificationTransSignature = ECKey.fromPublicOnly(Hex.decode(unconfirmedTran.getTransFrom())).verify(SHAEncrypt.sha3(SerializationUtils.serialize(unconfirmedTran.toString())), JSON.parseObject(new String(unconfirmedTran.getTransSignature()), ECKey.ECDSASignature.class));
            boolean verifyTrade = false;
            Assets fromAssets = assetsHandle.getFirstByList(assetsList, unconfirmedTran.getTransFrom(), unconfirmedTran.getTokenName());
            Assets fromAssetsPtn = assetsHandle.getFirstByList(assetsList, unconfirmedTran.getTransFrom(), Constants.PTN);
            boolean verifyFee = false;
            switch (unconfirmedTran.getTransType()) {
                case 0:
                    if (!Constants.PTN.equals(unconfirmedTran.getTokenName())) {
                        boolean verifyTransfer = fromAssets.getTotalEffectiveIncome() - fromAssets.getTotalExpenditure() >= unconfirmedTran.getTransValue() ? true : false;
                        verifyFee = fromAssetsPtn.getTotalEffectiveIncome() - fromAssetsPtn.getTotalExpenditure() >= unconfirmedTran.getFee() ? true : false;
                        if (verifyTransfer && verifyFee) {
                            verifyTrade = true;
                        }
                    } else {
                        boolean verifyTransfer = fromAssets.getTotalEffectiveIncome() - fromAssets.getTotalExpenditure() >= unconfirmedTran.getTransValue() + unconfirmedTran.getFee() ? true : false;
                        if (verifyTransfer) {
                            verifyTrade = true;
                        }
                    }
                    break;
                case 1:
                    long fee = Double.valueOf(TokenUtil.TokensRate(unconfirmedTran.getTokenName()) * unconfirmedTran.getTransValue() * Constants.MININUMUNIT).longValue();
                    verifyFee = fromAssetsPtn.getTotalEffectiveIncome() - fromAssetsPtn.getTotalExpenditure() >= fee ? true : false;
                    if (fee == unconfirmedTran.getFee() && verifyFee) {
                        verifyTrade = true;
                    }
                    break;
                default:
                    verifyTrade = false;
                    break;
            }
            if (verificationTransSignature && verifyTrade) {
                assetsList = assetsHandle.addTempAssetsList(assetsList, unconfirmedTran);
                saveUnconfirmedTranList.add(unconfirmedTran);
            } else {
                saveUnconfirmedTranList = new ArrayList<>();
                break;
            }
        }
        return saveUnconfirmedTranList;
    }

    public List<Token> verificationSyncTokenList(List<Token> syncTokenList) {
        List<Token> saveToken = new ArrayList<>();
        Iterable<Token> tokenIterable = tokenRepository.findAll();
        for (Token token : tokenIterable) {
            syncTokenList.remove(token);
        }
        for (Token token : syncTokenList) {
            saveToken.add(token);
        }
        return saveToken;
    }
}
