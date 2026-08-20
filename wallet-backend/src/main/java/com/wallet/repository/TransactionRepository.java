package com.wallet.repository;

import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByReferenceId(String referenceId);

    @Query("""
            SELECT t FROM Transaction t
            LEFT JOIN FETCH t.relatedWallet
            WHERE t.wallet.id = :walletId
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findRecentByWalletId(@Param("walletId") Long walletId, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.wallet.id = :walletId
              AND t.type = :type
              AND t.status = :status
            """)
    BigDecimal sumAmountByWalletIdAndTypeAndStatus(
            @Param("walletId") Long walletId,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status
    );

    @Query("""
            SELECT DISTINCT t FROM Transaction t
            JOIN FETCH t.wallet w
            JOIN FETCH w.user
            LEFT JOIN FETCH t.relatedWallet
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findAllForAdmin();
}
