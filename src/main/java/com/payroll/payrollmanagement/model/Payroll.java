package com.payroll.payrollmanagement.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * PAYROLL MODEL CLASS.
 *
 * Holds ONE payroll record (the salary slip of one employee for one month).
 *
 * INPUT fields (typed by the user):
 *   basicSalary, allowance (HRA), bonus, taxDeduction, otherDeduction
 *
 * OUTPUT fields (calculated by PayrollService):
 *   grossSalary, totalDeduction, netSalary
 *
 * The record also keeps employeeId + employeeName so the Payroll History page
 * can be shown without looking up the employee again, and a payrollDate stamp.
 */
public class Payroll {

    // ---------- inputs ----------
    private Integer employeeId;
    private String employeeName;
    private Double basicSalary;
    private Double allowance;       // HRA / Allowance
    private Double bonus;
    private Double taxDeduction;   // TDS / Income tax
    private Double otherDeduction; // PF, insurance, loan recovery etc.

    // ---------- calculated results ----------
    private Double grossSalary;    // basicSalary + allowance + bonus
    private Double totalDeduction; // taxDeduction + otherDeduction
    private Double netSalary;      // grossSalary - totalDeduction

    // ---------- extra info ----------
    private String department;
    private String designation;

    /**
     * The date this payroll was created. We store a real LocalDate (so we can
     * sort the history by date properly) and print it as readable text using
     * getPayrollDateText(), e.g. "04 Oct 2026".
     */
    private LocalDate payrollDate;

    public Payroll() {
    }

    public Payroll(Integer employeeId, String employeeName, Double basicSalary, Double allowance,
                   Double bonus, Double taxDeduction, Double otherDeduction) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.basicSalary = basicSalary;
        this.allowance = allowance;
        this.bonus = bonus;
        this.taxDeduction = taxDeduction;
        this.otherDeduction = otherDeduction;
    }

    // ---------------- GETTERS AND SETTERS ----------------

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(Double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public Double getAllowance() {
        return allowance;
    }

    public void setAllowance(Double allowance) {
        this.allowance = allowance;
    }

    public Double getBonus() {
        return bonus;
    }

    public void setBonus(Double bonus) {
        this.bonus = bonus;
    }

    public Double getTaxDeduction() {
        return taxDeduction;
    }

    public void setTaxDeduction(Double taxDeduction) {
        this.taxDeduction = taxDeduction;
    }

    public Double getOtherDeduction() {
        return otherDeduction;
    }

    public void setOtherDeduction(Double otherDeduction) {
        this.otherDeduction = otherDeduction;
    }

    public Double getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(Double grossSalary) {
        this.grossSalary = grossSalary;
    }

    public Double getTotalDeduction() {
        return totalDeduction;
    }

    public void setTotalDeduction(Double totalDeduction) {
        this.totalDeduction = totalDeduction;
    }

    public Double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(Double netSalary) {
        this.netSalary = netSalary;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public LocalDate getPayrollDate() {
        return payrollDate;
    }

    public void setPayrollDate(LocalDate payrollDate) {
        this.payrollDate = payrollDate;
    }

    /**
     * Readable version of the date for the HTML pages, e.g. "04 Oct 2026".
     * Thymeleaf prints this directly, so no extra date library is needed.
     */
    public String getPayrollDateText() {
        if (payrollDate == null) {
            return "-";
        }
        return payrollDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    /**
     * Sets today's date as the payroll date, e.g. 04 Oct 2026.
     */
    public void setPayrollDateToToday() {
        this.payrollDate = LocalDate.now();
    }

    /** Amount of money in words, e.g. "Fifty Thousand Rupees Only". Nice for the salary slip card. */
    public String getNetSalaryInWords() {
        if (netSalary == null) {
            return "";
        }
        return NumberToWordsUtil.convert(netSalary.longValue());
    }

@Override
    public String toString() {
        return "Payroll{employeeId=" + employeeId + ", employeeName='" + employeeName
                + "', grossSalary=" + grossSalary + ", totalDeduction=" + totalDeduction
                + ", netSalary=" + netSalary + ", payrollDate=" + payrollDate + "}";
    }
}
