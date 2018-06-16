package com.photon.photonchain.storage.repository;


import com.photon.photonchain.storage.entity.AddressAndPubKey;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * @Author:PTN
 * @Description:
 * @Date:14:11 2017/12/29
 * @Modified by:
 */
public interface AddressAndPubkeyRepository extends CrudRepository<AddressAndPubKey, Long> {

    @Query(value = "select addressAndPubKey.pubKey from AddressAndPubKey addressAndPubKey where addressAndPubKey.pubKey in(:#{#pubkeys})")
    Set<String> findByPubkeys(@Param("pubkeys") Set pubkeys);

    @Query(value = "select addressAndPubKey from AddressAndPubKey addressAndPubKey where addressAndPubKey.address =:#{#address}")
    AddressAndPubKey findByAddress(@Param("address") String address);
}
