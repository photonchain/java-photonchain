package com.photon.photonchain.storage.repository;


import com.photon.photonchain.storage.entity.Block;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:14:11 2017/12/29
 * @Modified by:
 */
public interface BlockRepository extends CrudRepository<Block, Long> {
    @Query(value = "select block from Block block")
    List<Block> findLastOne(Pageable pageable);

    @Query(value = "select block from Block block where block.blockHeight=:#{#block_height}")
    Block findBlockByBlockId(@Param("block_height") long block_height);

    @Query(value = "select block from Block block where block.blockHeight>:#{#blockHeight} and block.blockHeight<=:#{#endBlockHeight} order by block.blockHeight asc ")
    List<Block> findByBlockHeight(@Param("blockHeight") long blockHeight, @Param("endBlockHeight") long endBlockHeight);

    @Query(value = "select avg(block.totalAmount) as totalAmount  from Block block")
    BigDecimal getAmountAvg();

    @Query(value = "select avg(block.totalFee) as totalFee  from Block block")
    BigDecimal getFeeAvg();

    @Query(value = "select block.blockSize,block.foundryPublicKey,block.blockHead,block.blockHeight,block.totalAmount,block.totalFee from Block block")
    List<Block> findOne(Pageable pageable);

    @Query(value = "select block from Block block where block.blockHeight>:#{#start} and block.blockHeight<=:#{#end}")
    List<Block> findOneInterval(@Param("start") long start, @Param("end") long end);

    @Modifying
    @Query(value = "DELETE FROM BLOCK", nativeQuery = true)
    void truncate();

    @Modifying
    @Query(value = "TRUNCATE TABLE BLOCK_BLOCK_TRANSACTIONS", nativeQuery = true)
    void truncateRelation();
}
