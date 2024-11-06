package com.serhat.expenses.Repository;

import com.serhat.expenses.entity.Expenses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpensesRepository extends JpaRepository<Expenses,Integer> {
}
