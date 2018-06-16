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

    @Query(value = "select unconfirmedTran from UnconfirmedTran unconfirmedTran where unconfirmedTran.transType not in (4) and unconfirmedTran.timeStamp<:#{#time}")
    List<UnconfirmedTran> findUnconfirmedTran(Pageable pageable, @Param("time") long time);

    @Modifying
    @Query(value = "insert into UNCONFIRMED_TRAN(TRANS_FROM,TRANS_TO,REMARK,TOKEN_NAME,TRANS_VALUE,FEE,TIME_STAMP,TRANS_TYPE,TRANS_SIGNATURE,CONTRACT_ADDRESS,CONTRACT_STATE,CONTRACT_TYPE,CONTRACT_BIN,UNIQUE_ADDRESS,EXCHENGE_TOKEN) values(:#{#unconfirmedTran.transFrom},:#{#unconfirmedTran.transTo},:#{#unconfirmedTran.remark},:#{#unconfirmedTran.tokenName},:#{#unconfirmedTran.transValue},:#{#unconfirmedTran.fee},:#{#unconfirmedTran.timeStamp},:#{#unconfirmedTran.transType},:#{#unconfirmedTran.transSignature},:#{#unconfirmedTran.contractAddress},:#{#unconfirmedTran.contractState},:#{#unconfirmedTran.contractType},:#{#unconfirmedTran.contractBin},:#{#unconfirmedTran.uniqueAddress},:#{#unconfirmedTran.exchengeToken})", nativeQuery = true)
    void saveUnconfirmedTran(@Param("unconfirmedTran") UnconfirmedTran unconfirmedTran) throws Exception;

    @Query(value = "SELECT nvl(sum(FEE),0) FROM UNCONFIRMED_TRAN ", nativeQuery = true)
    long getUnconfirmedFee();

    @Modifying
    @Query(value = "TRUNCATE TABLE UNCONFIRMED_TRAN", nativeQuery = true)
    void truncate();


    @Modifying
    @Query(value = "update UnconfirmedTran unconfirmedTran set unconfirmedTran.transValue=unconfirmedTran.transValue+:#{#transValue} where unconfirmedTran.transType =:#{#transType} and unconfirmedTran.transTo=:#{#transTo}")
    void updateTransValueByTransTo(long transValue, long transType, String transTo);

    @Modifying
    @Query(value = "update UNCONFIRMED_TRAN set TRANS_VALUE =TRANS_VALUE +:#{#transValue} where TRANS_TYPE =:#{#transType} and TRANS_TO =:#{#transTo}", nativeQuery = true)
    void updateTransValueByTransToNative(@Param("transValue") long transValue, @Param("transType") long transType, @Param("transTo") String transTo);


    @Query(value = "SELECT IFNULL (SUM(TRANS_VALUE ),0) FROM  UNCONFIRMED_TRAN WHERE TRANS_TYPE = :#{#transType}", nativeQuery = true)
    long getTransValueSumByType(@Param("transType") long transType);

    @Query(value = "SELECT * FROM UNCONFIRMED_TRAN where TRANS_TYPE =:#{#transType}", nativeQuery = true)
    List<UnconfirmedTran> findUnconfirmedTranByTransType(@Param("transType") long transType);

    @Query(value = "SELECT * FROM UNCONFIRMED_TRAN   where  TRANS_TYPE  =:#{#transType} and  TRANS_FROM =:#{#transFrom} and  CONTRACT_ADDRESS  =:#{#contractAddress}", nativeQuery = true)
    UnconfirmedTran findByExchengeTrans(@Param("transType") long transType, @Param("transFrom") String transFrom, @Param("contractAddress") String contractAddress);

    @Query(value = "SELECT  count(TRANS_SIGNATURE ) FROM UNCONFIRMED_TRAN where TRANS_TYPE =:#{#transType} and TRANS_TO =:#{#transTo}", nativeQuery = true)
    long findByTransTypeAndTransToNative(@Param("transType") long transType, @Param("transTo") String transTo);

    @Query(value = "SELECT IFNULL(SUM(TRANS_VALUE),0)  FROM UNCONFIRMED_TRAN where  TRANS_FROM =:#{#transFrom} and  lower(TOKEN_NAME) =lower(:#{#tokenName})  AND TRANS_TYPE !=1", nativeQuery = true)
    long findExpenditureValue(@Param("transFrom") String transFrom, @Param("tokenName") String tokenName);

    @Query(value = "SELECT IFNULL(SUM(TRANS_VALUE ),0)  FROM UNCONFIRMED_TRAN where   TRANS_TO =:#{#transTo} and  lower(TOKEN_NAME) =lower(:#{#tokenName}) ", nativeQuery = true)
    long findIncome(@Param("transTo") String transTo, @Param("tokenName") String tokenName);

    @Query(value = "SELECT IFNULL(SUM(FEE),0)  FROM UNCONFIRMED_TRAN where TRANS_FROM =:#{#transFrom}", nativeQuery = true)
    long findSumFee(@Param("transFrom") String transFrom);

    @Query(value = "select count(unconfirmedTran) from UnconfirmedTran unconfirmedTran where unconfirmedTran.contractAddress=:#{#contractAddress} and unconfirmedTran.transType=:#{#transType}")
    long findContractCount(@Param("contractAddress") String contractAddress, @Param("transType") int transType);

    @Query(value = "select unconfirmedTran from UnconfirmedTran unconfirmedTran where :#{#time}-unconfirmedTran.timeStamp > 300000 and unconfirmedTran.transType=:#{#transType}")
    List<UnconfirmedTran> findUnconfirmedTranBeforFiveMin(@Param("time") long time,@Param("transType") int transType);

    @Query(value = "select unconfirmedTran from UnconfirmedTran unconfirmedTran where unconfirmedTran.contractAddress=:#{#contractAddress} and unconfirmedTran.transType=:#{#transType}")
    List<UnconfirmedTran> findByTypeAndAddress(@Param("contractAddress") String contractAddress, @Param("transType") int transType);

    @Modifying
    @Query(value = "delete from UnconfirmedTran unconfirmedTran where unconfirmedTran.contractAddress=:#{#contractAddress} and unconfirmedTran.transType=:#{#transType}")
    void deleteByTypeAndAddress(@Param("contractAddress") String contractAddress, @Param("transType") int transType);

}
