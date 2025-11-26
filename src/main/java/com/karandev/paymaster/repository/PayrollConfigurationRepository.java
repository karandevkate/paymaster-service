package com.karandev.paymaster.repository;

import com.karandev.paymaster.entity.PayrollConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollConfigurationRepository extends JpaRepository<PayrollConfiguration, UUID> {
    Optional<PayrollConfiguration> findByCompany_CompanyId(UUID companyId);


    Optional<PayrollConfiguration> findByCompany_CompanyIdAndIsActiveTrue(UUID companyId);

    Optional<PayrollConfiguration> findFirstByCompany_CompanyIdAndIsActiveTrueOrderByCreatedAtDesc(UUID companyId);
}
