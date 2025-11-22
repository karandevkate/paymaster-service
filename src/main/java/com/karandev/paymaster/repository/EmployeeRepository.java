package com.karandev.paymaster.repository;

import com.karandev.paymaster.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByEmail(String email);
    List<Employee> findAllByCompany_CompanyId(UUID companyId);
    Optional<Employee> findByPasswordToken(String token);
}
