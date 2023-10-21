package com.mindex.challenge.data;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class Compensation {
    private String employeeId;
    private double salary;
    private @DateTimeFormat(pattern = "yyyy-MM-dd") Date effectiveDate;

    public Compensation() {
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
