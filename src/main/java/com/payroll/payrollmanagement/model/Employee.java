package com.payroll.payrollmanagement.model;

/**
 * EMPLOYEE MODEL CLASS (also called a POJO / Entity / Bean).
 *
 * This class simply describes ONE employee of the company.
 * It only holds data (fields) and simple getters/setters - no business logic.
 *
 * Note: employeeId and basicSalary are wrapper types (Integer, Double) instead of
 * primitive types (int, double) on purpose. If the user leaves the input box empty,
 * Spring sends an empty text value, and a wrapper type safely becomes null so we
 * can show a friendly validation message instead of getting a crash.
 */
public class Employee {

    private Integer employeeId;   // Unique employee id, e.g. 101
    private String name;          // Employee full name
    private String email;         // Email address
    private String phone;         // Phone number
    private String department;    // Department, e.g. IT / HR / Finance
    private String designation;   // Job title, e.g. Developer
    private Double basicSalary;   // Basic (starting) salary

    /** Default constructor - required by Spring / Thymeleaf form binding. */
    public Employee() {
    }

    /** Parameterized constructor - used when we create a new employee. */
    public Employee(Integer employeeId, String name, String email, String phone,
                    String department, String designation, Double basicSalary) {
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.designation = designation;
        this.basicSalary = basicSalary;
    }

    /** Copy constructor - used to make a safe copy before editing an employee. */
    public Employee(Employee other) {
        this.employeeId = other.employeeId;
        this.name = other.name;
        this.email = other.email;
        this.phone = other.phone;
        this.department = other.department;
        this.designation = other.designation;
        this.basicSalary = other.basicSalary;
    }

    // ---------------- GETTERS AND SETTERS ----------------

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(Double basicSalary) {
        this.basicSalary = basicSalary;
    }

    /** Returns the first letters of the name, e.g. "Rahul Sharma" -> "RS". */
    public String getInitials() {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            initials.append(part.charAt(0));
            if (initials.length() == 2) {
                break;
            }
        }
        return initials.toString().toUpperCase();
    }

    /** Very small helper used for clean text output in the console / tables. */
    public String getFormattedBasicSalary() {
        if (basicSalary == null) {
            return "-";
        }
        return String.format("%,.2f", basicSalary);
    }

    @Override
    public String toString() {
        return "Employee{employeeId=" + employeeId + ", name='" + name + "', department='" + department
                + "', basicSalary=" + basicSalary + "}";
    }
}
