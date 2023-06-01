package com.geo.easypoint.employee.dto.request;

import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDate;
import java.util.Set;

public record EmployeeUpdateRequest(
        JsonNullable<AddressUpdateRequest> address,
        JsonNullable<PassportInfoUpdateRequest> passportInfo,
        JsonNullable<Long> competencyId,
        JsonNullable<Long> jobTitleId,
        JsonNullable<Set<Long>> administrativeUnits,
        JsonNullable<LocalDate> hiredDate,
        JsonNullable<String> firstName,
        JsonNullable<String> surname,
        JsonNullable<String> patronomic
) {
}