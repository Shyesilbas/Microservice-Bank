package com.serhat.expenses.Repository;

import com.serhat.expenses.dto.ProcessResponse;
import com.serhat.expenses.entity.Category;
import com.serhat.expenses.entity.Expenses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpensesRepository extends JpaRepository<Expenses,Integer> {
    List<Expenses> findByCustomerId(Integer customerId);

    List<Expenses> findByCardNumber(String cardNumber);

    List<Expenses> findExpensesByCardNumberAndCategory(String cardNumber ,Category category);
}
