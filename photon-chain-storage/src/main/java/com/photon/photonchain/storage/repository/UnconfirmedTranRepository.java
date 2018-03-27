package com.photon.photonchain.storage.repository;

import com.photon.photonchain.storage.entity.UnconfirmedTran;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:14:13 2018/2/6
 * @Modified by:
 */
public interface UnconfirmedTranRepository extends CrudRepository<UnconfirmedTran, Long> {
    @Modifying
    @Query(value = "delete from UnconfirmedTran unconfirmedTran where unconfirmedTran.transSignature=:#{#transSignature}")
    void deleteByTransSignature(@Param("transSignature") byte[] transSignature);

    @Modifying
    @Query(value = "delete from UnconfirmedTran unconfirmedTran where unconfirmedTran.transSignature in (:#{#transSignatureList})")
    void deleteByTransSignatureList(@Param("transSignatureList") List<byte[]> transSignatureList);

    @Query(value = "select unconfirmedTran from UnconfirmedTran unconfirmedTran where unconfirmedTran.transSignature=:#{#signature}")
    UnconfirmedTran findBySignature(@Param("signature") byte[] signature);

    @Query(value = "select unconfirmedTran from UnconfirmedTran unconfirmedTran")
    List<UnconfirmedTran> findAll();

    @Query(value = "select unconfirmedTran from UnconfirmedTran unconfirmedTran")
    List<UnconfirmedTran> findUnconfirmedTran(Pageable pageable);

    @Modifying
    @Query(value = "insert into UNCONFIRMED_TRAN(TRANS_FROM,TRANS_TO,REMARK,TOKEN_NAME,TRANS_VALUE,FEE,TIME_STAMP,TRANS_TYPE,TRANS_SIGNATURE) values(:#{#unconfirmedTran.transFrom},:#{#unconfirmedTran.transTo},:#{#unconfirmedTran.remark},:#{#unconfirmedTran.tokenName},:#{#unconfirmedTran.transValue},:#{#unconfirmedTran.fee},:#{#unconfirmedTran.timeStamp},:#{#unconfirmedTran.transType},:#{#unconfirmedTran.transSignature})", nativeQuery = true)
    void saveUnconfirmedTran(@Param("unconfirmedTran") UnconfirmedTran unconfirmedTran) throws Exception;

    @Query(value = "SELECT nvl(sum(FEE),0) FROM UNCONFIRMED_TRAN ", nativeQuery = true)
    long getUnconfirmedFee();
}
