package com.karandev.paymaster.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ManualPayrollRequestDto {
    private UUID employeeId;
    private UUID companyId;
    private Integer month;
    private Integer year;
    private Integer daysPaid; // Optional: If not provided, it will take default month days
}
