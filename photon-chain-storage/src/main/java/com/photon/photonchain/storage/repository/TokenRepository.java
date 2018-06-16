package com.photon.photonchain.storage.repository;

import com.photon.photonchain.storage.entity.Token;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:19:53 2018/1/30
 * @Modified by:
 */
public interface TokenRepository extends CrudRepository<Token, Integer> {
    @Query(value = "select token from Token token where lower(token.name)=lower(:#{#tokenName})")
    Token findByName(@Param("tokenName") String tokenName);

    @Query(value = "select token from Token token where token.name=:#{#tokenName} or token.symbol=:#{#tokenName}")
    List<Token> findAllByTokenName(@Param("tokenName") String tokenName, Pageable pageable);

    @Modifying
    @Query(value = "TRUNCATE TABLE TOKEN", nativeQuery = true)
    void truncate();
}
