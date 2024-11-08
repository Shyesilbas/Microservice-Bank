package com.serhat.loan.Repository;

import com.serhat.loan.dto.LoanResponse;
import com.serhat.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan,Integer> {

}
