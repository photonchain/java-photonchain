package com.photon.photonchain.storage.repository;

import com.photon.photonchain.storage.entity.NodeAddress;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:10:15 2018/1/22
 * @Modified by:
 */
public interface NodeAddressRepository extends CrudRepository<NodeAddress, String> {
    @Modifying
    @Query(value = "update NodeAddress note set note.port=:#{#nodeAddress.port} where note.hexIp=:#{#nodeAddress.hexIp}")
    void update(@Param("nodeAddress") NodeAddress nodeAddress);

    @Query(value = "select note.hexIp from NodeAddress note")
    List<String> findAllHexIp();
}
