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
    @Query(value = "insert into TRANSACTION(TRANS_SIGNATURE,BLOCK_HEIGHT,LOCK_TIME,REMARK,TOKEN_NAME,TRANS_FROM,TRANS_TO,TRANS_TYPE,TRANSACTION_HEAD, CONTRACT_ADDRESS ,CONTRACT_BIN ,CONTRACT_STATE ,CONTRACT_TYPE ,EXCHENGE_TOKEN, TRANS_VALUE ,FEE) values(:#{#transaction.transSignature},:#{#transaction.blockHeight},:#{#transaction.lockTime},:#{#transaction.remark},:#{#transaction.tokenName},:#{#transaction.transFrom},:#{#transaction.transTo},:#{#transaction.transType},:#{#transaction.transactionHead},:#{#transaction.contractAddress},:#{#transaction.contractBin},:#{#transaction.contractState},:#{#transaction.contractType},:#{#transaction.exchengeToken},:#{#transaction.transValue},:#{#transaction.fee})", nativeQuery = true)
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
    List<Transaction> findAllByAccountAndTokenNameNative(@Param("tokenName") String tokenName, @Param("account") String account, @Param("start") long start, @Param("end") long end);

    @Query(value = "select * from Transaction limit :#{#start},:#{#size}", nativeQuery = true)
    List<Transaction> findTransactionInterval(@Param("start") long start, @Param("size") long size);

    @Modifying
    @Query(value = "DELETE FROM TRANSACTION ", nativeQuery = true)
    void truncate();

    @Query(value = "select transaction from Transaction transaction where transaction.transSignature=:#{#signature}")
    Transaction findByTransSignature(@Param("signature") byte[] signature);



    @Query(value = "select transaction from Transaction transaction where transaction.contractAddress=:#{#contractAddress} and transaction.transType=:#{#transType}")
    Transaction findByContract(@Param("contractAddress") String contractAddress, @Param("transType") int transType);

    @Query(value = "select transaction from Transaction transaction where transaction.contractAddress=:#{#contractAddress} and transaction.contractState=:#{#state} and transaction.transType=:#{#transType}")
    Transaction findByContractAddress(@Param("contractAddress") String contractAddress, @Param("state") Integer state, @Param("transType") Integer transType);


    @Query(value = "select transaction from Transaction transaction where transaction.transTo=:#{#transTo} and transaction.transType=:#{#transType} and transaction.contractAddress=:#{#contractAddress}")
    Transaction findByTransFromAndType(@Param("transTo") String transTo, @Param("transType") Integer transType, @Param("contractAddress") String contractAddress);

    @Query(value = "SELECT * FROM TRANSACTION  where TRANS_TYPE =:#{#transType} and TRANS_TO =:#{#transTo}", nativeQuery = true)
    List<Transaction> findByTransTypeAndTransToNative(@Param("transType") long transType, @Param("transTo") String transTo);


    @Modifying
    @Query(value = "update Transaction trans set trans.contractState=:#{#transaction.contractState} where trans.contractAddress =:#{#transaction.contractAddress} and trans.transType=:#{#transaction.transType}")
    void updateTransactionState(@Param("transaction") Transaction transaction);

    @Modifying
    @Query(value = "update Transaction trans set trans.contractState=:#{#transaction.contractState} where trans.contractAddress =:#{#transaction.contractAddress}")
    void updateContranctState(@Param("transaction") Transaction transaction);


    @Query(value = "select trans from Transaction trans where trans.transType=:#{#transType} and trans.transFrom in (:#{#transForms})")
    List<Transaction> findByTransTypeAndTransForm(@Param("transType") Integer transType, @Param("transForms") List<String> transForms, Pageable pageable);

    @Query(value = "select count(trans.transSignature) from Transaction trans where trans.transType=:#{#transType} and trans.transFrom in (:#{#transForms})")
    int findByTransTypeAndTransFormCount(@Param("transType") Integer transType, @Param("transForms") List<String> transForms);


    @Query(value = "select trans from Transaction trans where trans.transType=:#{#transType} and trans.transFrom not in (:#{#transForms}) and trans.contractState=:#{#contractState} and lower( trans.tokenName) like %:#{#tokenName}% and lower(trans.exchengeToken) like %:#{#exchengeToken}%")
    List<Transaction> findByTransTypeAndNotTransForm(@Param("transType") Integer transType, @Param("transForms") List<String> transForms, Pageable pageable, @Param("contractState") int contractState, @Param("tokenName") String tokenName, @Param("exchengeToken") String exchengeToken);

    @Query(value = "select count(trans.transSignature) from Transaction trans where trans.transType=:#{#transType} and trans.transFrom not in (:#{#transForms}) and trans.contractState=:#{#contractState} and trans.tokenName like %:#{#tokenName}% and trans.exchengeToken like %:#{#exchengeToken}%")
    long findByTransTypeAndNotTransFormCount(@Param("transType") Integer transType, @Param("transForms") List<String> transForms, @Param("contractState") int contractState, @Param("tokenName") String tokenName, @Param("exchengeToken") String exchengeToken);


    @Query(value = "SELECT count(TRANS_SIGNATURE) FROM TRANSACTION  where TRANS_TYPE =:#{#transType} and TRANS_FROM  =:#{#transFrom} and CONTRACT_ADDRESS  =:#{#contractAddress}", nativeQuery = true)
    long findByTransTypeAndTransFromAndContractAddressNative(@Param("transType") long transType, @Param("transFrom") String transFrom, @Param("contractAddress") String contractAddress);

    @Query(value = "SELECT ifnull(sum(trans_value)-sum(fee),0) FROM TRANSACTION where trans_type=2", nativeQuery = true)
    long totalMining();

    @Query(value = "SELECT IFNULL(sum(FEE),0)  FROM TRANSACTION where TRANS_FROM =:#{#transFrom} ", nativeQuery = true)
    long findSumFee( @Param("transFrom") String transFrom);

    @Query(value = "SELECT IFNULL(sum(TRANS_VALUE ),0)  FROM TRANSACTION where TRANS_FROM =:#{#transFrom} and lower(TOKEN_NAME) =lower(:#{#tokenName}) AND TRANS_TYPE !=1", nativeQuery = true)
    long findExpenditureValue( @Param("transFrom") String transFrom, @Param("tokenName") String tokenName);

    @Query(value = "SELECT IFNULL(sum(TRANS_VALUE),0)  FROM TRANSACTION where TRANS_TO =:#{#transTo} and lower(TOKEN_NAME) =lower(:#{#tokenName}) ", nativeQuery = true)
    long findIncome( @Param("transTo") String transTo, @Param("tokenName") String tokenName);

}
