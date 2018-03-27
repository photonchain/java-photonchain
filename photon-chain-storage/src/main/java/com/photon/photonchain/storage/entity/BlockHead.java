package com.photon.photonchain.storage.entity;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * @Author:PTN
 * @Description:
 * @Date:14:24 2018/1/15
 * @Modified by:
 */
public class BlockHead implements Serializable {
    private static final long serialVersionUID = -3292451712962647094L;
    private int version;
    private long timeStamp;
    private BigInteger cumulativeDifficulty;
    private byte[] hashPrevBlock;
    private byte[] hashMerkleRoot;

    public BlockHead() {
    }

    public BlockHead(int version, long timeStamp, BigInteger cumulativeDifficulty, byte[] hashPrevBlock, byte[] hashMerkleRoot) {
        this.version = version;
        this.timeStamp = timeStamp;
        this.cumulativeDifficulty = cumulativeDifficulty;
        this.hashPrevBlock = hashPrevBlock;
        this.hashMerkleRoot = hashMerkleRoot;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public BigInteger getCumulativeDifficulty() {
        return cumulativeDifficulty;
    }

    public void setCumulativeDifficulty(BigInteger cumulativeDifficulty) {
        this.cumulativeDifficulty = cumulativeDifficulty;
    }

    public byte[] getHashPrevBlock() {
        return hashPrevBlock;
    }

    public void setHashPrevBlock(byte[] hashPrevBlock) {
        this.hashPrevBlock = hashPrevBlock;
    }

    public byte[] getHashMerkleRoot() {
        return hashMerkleRoot;
    }

    public void setHashMerkleRoot(byte[] hashMerkleRoot) {
        this.hashMerkleRoot = hashMerkleRoot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockHead blockHead = (BlockHead) o;

        if (version != blockHead.version) return false;
        if (timeStamp != blockHead.timeStamp) return false;
        if (cumulativeDifficulty != null ? !cumulativeDifficulty.equals(blockHead.cumulativeDifficulty) : blockHead.cumulativeDifficulty != null)
            return false;
        if (!Arrays.equals(hashPrevBlock, blockHead.hashPrevBlock)) return false;
        return Arrays.equals(hashMerkleRoot, blockHead.hashMerkleRoot);
    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
        result = 31 * result + (cumulativeDifficulty != null ? cumulativeDifficulty.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(hashPrevBlock);
        result = 31 * result + Arrays.hashCode(hashMerkleRoot);
        return result;
    }

    @Override
    public String toString() {
        return "BlockHead{" +
                "version=" + version +
                ", timeStamp=" + timeStamp +
                ", cumulativeDifficulty=" + cumulativeDifficulty +
                ", hashPrevBlock=" + Hex.toHexString(hashPrevBlock) +
                ", hashMerkleRoot=" + Hex.toHexString(hashMerkleRoot) +
                '}';
    }
}