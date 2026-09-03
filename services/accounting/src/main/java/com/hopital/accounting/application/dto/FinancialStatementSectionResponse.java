package com.hopital.accounting.application.dto;

import java.math.BigDecimal;

public record FinancialStatementSectionResponse(String key, String label, BigDecimal amount) { }
