package com.karandev.paymaster.service;

import com.karandev.paymaster.dto.CompanyResponseDto;
import com.karandev.paymaster.dto.CompanyUpdateDTO;
import com.karandev.paymaster.dto.CompanyRegisterWithAdminDto;
import com.karandev.paymaster.entity.Company;

import java.util.List;
import java.util.UUID;

public interface CompanyService {

    void createCompany(CompanyRegisterWithAdminDto dto);

    CompanyResponseDto getCompanyById(UUID companyId);

    List<CompanyResponseDto> getAllCompanies();

    void updateCompany(UUID companyId, CompanyUpdateDTO dto);

    void deleteCompany(UUID companyId);
}
