package com.ironhack.midterm.repository;

import com.ironhack.midterm.model.AccountHolder;
import com.ironhack.midterm.model.Transaction;
import com.ironhack.midterm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    //@Query(value = "SELECT t FROM Transaction t WHERE (t.debitedAccount.id = :accountId OR t.creditedAccount.id = :accountId) AND t.date >= DATE_ADD(")
    @Query(value = "SELECT * FROM transaction WHERE (debited_account_id = :accountId OR credited_account_id = :accountId) AND date >= DATE_ADD(:currentDate, INTERVAL -1 SECOND)", nativeQuery = true)
    public List<Transaction> findTransactionOneSecondAgo(@Param("accountId") Long accountId, @Param("currentDate") Date currentDate);

    @Query(value = "SELECT MAX(count) FROM (SELECT COUNT(*) count FROM transaction t JOIN account a ON (a.id = t.debited_account_id OR a.id = t.credited_account_id) WHERE (a.primary_owner_id = :accountHolderId OR a.secondary_owner_id = :accountHolderId) AND (DATEDIFF(t.date, :currentDate) < 0) GROUP BY DAY (t.date)) as T", nativeQuery=true )
    public Double findHighestTotalTransactionCountOfOwner(@Param("accountHolderId") Long accountHolderId, @Param("currentDate") Date currentDate);

    @Query(value = "SELECT MAX(count) FROM (SELECT COUNT(*) count FROM transaction t WHERE (t.transaction_maker_id = :userId) AND DATEDIFF(t.date, :currentDate) < 0 GROUP BY DAY (t.date)) as T", nativeQuery = true)
    public Double findHighestTotalTransactionCountOfUser(@Param("userId") Long userId, @Param("currentDate") Date currentDate);

    @Query(value = "SELECT COUNT(*) FROM transaction t JOIN account a ON (a.id = t.debited_account_id OR a.id = t.credited_account_id) WHERE (a.primary_owner_id = :accountHolderId OR a.secondary_owner_id = :accountHolderId) AND (DATEDIFF(t.date, :currentDate) = 0)", nativeQuery=true)
    public Double findCurrentDateTransactionCountOfOwner(@Param("accountHolderId") Long accountHolderId, @Param("currentDate") Date currentDate);

    @Query(value = "SELECT COUNT(*) FROM transaction t WHERE (t.transaction_maker_id = :userId) AND (DATEDIFF(t.date, :currentDate) = 0)", nativeQuery = true)
    public Double findCurrentDateTransactionCountOfUser(@Param("userId") Long userId, @Param("currentDate") Date currentDate);

}
