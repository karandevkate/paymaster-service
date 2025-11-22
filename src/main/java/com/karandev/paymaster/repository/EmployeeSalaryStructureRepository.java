package com.karandev.paymaster.repository;

import com.karandev.paymaster.entity.EmployeeSalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeSalaryStructureRepository extends JpaRepository<EmployeeSalaryStructure, UUID> {
    Optional<EmployeeSalaryStructure> findByEmployee_EmployeeIdAndCompany_CompanyId(UUID employeeId, UUID companyId);
}
