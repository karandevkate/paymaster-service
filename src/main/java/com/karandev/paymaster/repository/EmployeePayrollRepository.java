package com.karandev.paymaster.repository;

import com.karandev.paymaster.entity.EmployeePayroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeePayrollRepository extends JpaRepository<EmployeePayroll, UUID> {
    Optional<EmployeePayroll> findByCompany_CompanyId(UUID companyID);
    List<EmployeePayroll> findByCompany_CompanyIdOrderByYearDescMonthDesc(UUID companyId);
    List<EmployeePayroll> findByEmployee_EmployeeIdAndCompany_CompanyIdOrderByYearDescMonthDesc(UUID employeeId, UUID companyId);


    boolean existsByEmployee_EmployeeIdAndMonthAndYear(UUID employeeId, int month, int year);




}
