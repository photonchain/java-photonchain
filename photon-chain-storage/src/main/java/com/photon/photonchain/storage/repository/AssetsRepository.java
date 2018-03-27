package com.photon.photonchain.storage.repository;

import com.photon.photonchain.storage.entity.Assets;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:10:14 2018/3/20
 * @Modified by:
 */
public interface AssetsRepository extends CrudRepository<Assets, Long> {
    @Query(value = "select assets from Assets assets where assets.pubKey=:#{#pubKey} and assets.tokenName=:#{#tokenName}")
    Assets findByPubKeyAndTokenName(@Param("pubKey") String pubKey, @Param("tokenName") String tokenName);

    @Modifying
    @Query(value = "update Assets assets set assets.totalEffectiveIncome=:#{#assets.totalEffectiveIncome},assets.totalExpenditure=:#{#assets.totalExpenditure},assets.totalIncome=:#{#assets.totalIncome} where assets.pubKey=:#{#assets.pubKey} and assets.tokenName=:#{#assets.tokenName}")
    void updateAssets(@Param("assets") Assets assets);

    @Query(value = "select assets from Assets assets where assets.tokenName=:#{#tokenName}")
    List<Assets> findAllByTokenName(@Param("tokenName") String tokenName);

    @Modifying
    @Query(value = "delete from Assets assets where assets.tokenName=:#{#tokenName} and assets.pubKey=:#{#pubKey}")
    void deleteAssetsByTokenNameAndPubKey(@Param("tokenName") String tokenName, @Param("pubKey") String pubKey);

    @Query(value = "select assets from Assets assets where assets.address=:#{#address} and assets.tokenName=:#{#tokenName}")
    Assets findByAddressAndTokenName(@Param("address") String address, @Param("tokenName") String tokenName);
}
