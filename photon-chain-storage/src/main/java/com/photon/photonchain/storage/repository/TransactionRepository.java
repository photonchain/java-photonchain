package com.photon.photonchain.storage.repository;


import com.photon.photonchain.storage.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author:PTN
 * @Description:
 * @Date:15:49 2017/12/29
 * @Modified by:
 */
public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    @Query(value = "select transaction from Transaction transaction where transaction.blockHeight = -1")
    List<Transaction> unconfirmedTransaction(Pageable pageable);

    @Modifying
    @Query(value = "update Transaction trans set trans.blockHeight=:#{#transaction.blockHeight},trans.lockTime=:#{#transaction.lockTime}")
    void updateTransaction(@Param("transaction") Transaction transaction);

    @Query(value = "select transaction from Transaction transaction where transaction.transFrom=:#{#Account} or transaction.transTo=:#{#Account}")
    List<Transaction> findAllByAccount(@Param("Account") String Account);

    @Query(value = "select transaction from Transaction transaction where transaction.transTo=:#{#Account}")
    List<Transaction> findAllByTransTo(@Param("Account") String Account);

    @Query(value = "select transaction from Transaction transaction")
    List<Transaction> findByTransactionId(@Param("transactionId") long transactionId);

    @Query(value = "select transaction from Transaction transaction where transaction.transSignature in (:#{#signatureList})")
    List<Transaction> findByTransSignatureList(@Param("signatureList") List<byte[]> signatureList);

    @Query(value = "select transaction from Transaction transaction")
    List<Transaction> findTransactionOne(Pageable pageable);


    @Query(value = "select transaction from Transaction transaction where transaction.transFrom=:#{#Account} or transaction.transTo=:#{#Account}")
    List<Transaction> findAllByAccount(@Param("Account") String Account, Pageable pageable);

    @Query(value = "select transaction from Transaction transaction where transaction.transSignature=:#{#transSignature}")
    List<Transaction> findAllByTransSignature(@Param("transSignature") byte[] transSignature);

    @Query(value = "select transaction from Transaction transaction where transaction.tokenName=:#{#tokenName} and transaction.transType=1")
    Transaction findByTokenNameAndTransType(@Param("tokenName") String tokenName);

    @Query(value = "select transaction from Transaction transaction where transaction.tokenName=:#{#tokenName}")
    List<Transaction> findTransaction(@Param("tokenName") String tokenName, Pageable pageable);

    @Query(value = "select transaction from Transaction transaction where transaction.tokenName=:#{#tokenName} and transaction.transType=:#{#transType} and (transaction.transFrom=:#{#account} or transaction.transTo=:#{#account})")
    List<Transaction> findAllByAccountAndTokenName(@Param("tokenName") String tokenName, @Param("transType") int transType, @Param("account") String account, Pageable pageable);

    @Query(value = "select transaction from Transaction transaction where (transaction.transFrom=:#{#Account} or transaction.transTo=:#{#Account}) AND (transaction.tokenName=:#{#tokenName})")
    List<Transaction> findAllByAccountAndTokenName(@Param("Account") String Account, @Param("tokenName") String tokenName, Pageable pageable);

    @Query(value = "select transaction from Transaction transaction ")
    List<Transaction> findAll();

    @Modifying
    @Query(value = "insert into TRANSACTION(TRANS_SIGNATURE,BLOCK_HEIGHT,LOCK_TIME,REMARK,TOKEN_NAME,TRANS_FROM,TRANS_TO,TRANS_TYPE,TRANSACTION_HEAD) values(:#{#transaction.transSignature},:#{#transaction.blockHeight},:#{#transaction.lockTime},:#{#transaction.remark},:#{#transaction.tokenName},:#{#transaction.transFrom},:#{#transaction.transTo},:#{#transaction.transType},:#{#transaction.transactionHead})", nativeQuery = true)
    void saveTransaction(@Param("transaction") Transaction transaction) throws Exception;

    @Query(value = "select count(transaction.transSignature) from Transaction transaction where transaction.tokenName=:#{#tokenName}  ")
    int findTransactionCount(@Param("tokenName") String tokenName);

    @Query(value = "select count(transaction.transSignature) from Transaction transaction where (transaction.transFrom=:#{#Account} or transaction.transTo=:#{#Account}) AND (transaction.tokenName=:#{#tokenName})")
    int findAllByAccountAndTokenNameAndTypeCount(@Param("Account") String Account, @Param("tokenName") String tokenName);

    @Query(value = "select transaction from Transaction transaction where (transaction.transFrom=:#{#Account} or transaction.transTo=:#{#Account}) AND (transaction.tokenName=:#{#tokenName})")
    List<Transaction> findAllByAccountAndTokenNameAndType(@Param("Account") String Account, @Param("tokenName") String tokenName, Pageable pageable);

    @Query(value = "select count(transaction.transSignature) from Transaction transaction where transaction.tokenName=:#{#tokenName} and (transaction.transFrom=:#{#account} or transaction.transTo=:#{#account})")
    int findAllByAccountAndTokenNameAndCount(@Param("tokenName") String tokenName, @Param("account") String account);

    @Query(value = "select count(transaction.blockHeight) from Transaction transaction where transaction.blockHeight =:#{#blockHeight}")
    int findBlockTransCount(long blockHeight);

    @Query(value = "select TRANS_SIGNATURE transSignature, BLOCK_HEIGHT blockHeight,LOCK_TIME lockTime, REMARK remark,TOKEN_NAME tokenName,TRANS_FROM transFrom,TRANS_TO transTo,TRANS_TYPE transType,TRANSACTION_HEAD transactionHead from Transaction where token_name =:#{#tokenName} AND (trans_from=:#{#account} or trans_to=:#{#account}) limit :#{#start},:#{#end}", nativeQuery = true)
    List<Transaction> findAllByAccountAndTokenNameNative(@Param("tokenName") String tokenName, @Param("account") String account,@Param("start") long start,@Param("end") long end);

    @Query(value = "select * from Transaction limit :#{#start},:#{#size}", nativeQuery = true)
    List<Transaction> findTransactionInterval(@Param("start") long start,@Param("size") long size);

}
