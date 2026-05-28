package com.igovern.data.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProgramDtos {

    public static class ProgramRequest {
        @NotBlank @Size(max = 200)
        private String name;

        @Size(max = 1000)
        private String description;

        @NotNull
        private LocalDate startDate;

        private LocalDate endDate;

        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal budget;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public BigDecimal getBudget() { return budget; }
        public void setBudget(BigDecimal budget) { this.budget = budget; }
    }

    public static class ParticipantRequest {
        @NotBlank @Size(max = 100)
        private String firstName;

        @NotBlank @Size(max = 100)
        private String lastName;

        @NotNull @Past
        private LocalDate dateOfBirth;

        @NotNull
        private LocalDate enrollmentDate;

        @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax(value = "1000.0")
        private Double weightKg;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public LocalDate getEnrollmentDate() { return enrollmentDate; }
        public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
        public Double getWeightKg() { return weightKg; }
        public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    }
}
