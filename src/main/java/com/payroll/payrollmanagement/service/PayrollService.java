package com.payroll.payrollmanagement.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.payroll.payrollmanagement.model.Employee;
import com.payroll.payrollmanagement.model.Payroll;
import com.payroll.payrollmanagement.repository.PayrollRepository;

/**
 * PAYROLL SERVICE.
 *
 * Contains the salary calculation business rules, the validation of the payroll
 * form and the statistics needed by the dashboard and the history page.
 *
 * THE FORMULA (exactly as required by the project):
 *   Gross Salary   = Basic Salary + HRA / Allowance + Bonus
 *   Total Deduction = Tax Deduction + Other Deduction
 *   Net Salary     = Gross Salary - Total Deduction
 */
@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeService employeeService;

    public PayrollService(PayrollRepository payrollRepository, EmployeeService employeeService) {
        this.payrollRepository = payrollRepository;
        this.employeeService = employeeService;
    }

    // ------------------------------------------------------------------
    //  SALARY CALCULATION
    // ------------------------------------------------------------------

    /**
     * Calculates Gross Salary, Total Deduction and Net Salary.
     * This method only does the maths - validation is done separately so that
     * the same calculation can be reused from anywhere.
     */
    public void calculateSalary(Payroll payroll) {
        double basic = safeValue(payroll.getBasicSalary());
        double allowance = safeValue(payroll.getAllowance());
        double bonus = safeValue(payroll.getBonus());
        double tax = safeValue(payroll.getTaxDeduction());
        double other = safeValue(payroll.getOtherDeduction());

        // Gross Salary = Basic Salary + HRA / Allowance + Bonus
        double grossSalary = basic + allowance + bonus;
        payroll.setGrossSalary(round(grossSalary));

        // Total Deduction = Tax Deduction + Other Deduction
        double totalDeduction = tax + other;
        payroll.setTotalDeduction(round(totalDeduction));

        // Net Salary = Gross Salary - Total Deduction
        double netSalary = grossSalary - totalDeduction;
        payroll.setNetSalary(round(netSalary));
    }

    /**
     * Fills the employee name, department and designation automatically.
     * This uses the HashMap lookup in EmployeeRepository, so it is instant.
     */
    public void fillEmployeeDetails(Payroll payroll) {
        Employee employee = employeeService.getEmployeeById(payroll.getEmployeeId());
        if (employee != null) {
            payroll.setEmployeeName(employee.getName());
            payroll.setDepartment(employee.getDepartment());
            payroll.setDesignation(employee.getDesignation());
        }
    }

    // ------------------------------------------------------------------
    //  VALIDATION
    // ------------------------------------------------------------------

    /**
     * Checks the payroll form and then calculates the salary.
     *
     * @return null when everything is fine, otherwise the error message.
     */
    public String validateAndCalculate(Payroll payroll) {
        String error = validate(payroll);
        if (error != null) {
            return error;
        }
        calculateSalary(payroll);
        return null;
    }

    /**
     * Checks every field of the payroll form.
     */
    private String validate(Payroll payroll) {

        if (payroll == null) {
            return "No payroll data was received. Please try again.";
        }

        // ---- Employee ID ----
        if (payroll.getEmployeeId() == null) {
            return "Employee ID is required. Please select or type an Employee ID.";
        }
        if (payroll.getEmployeeId() <= 0) {
            return "Employee ID must be a positive number.";
        }

        // The employee must exist in the HashMap of employee records
        if (employeeService.getEmployeeById(payroll.getEmployeeId()) == null) {
            return "No employee found with Employee ID " + payroll.getEmployeeId()
                    + ". Please add the employee first or check the ID.";
        }

        // ---- Basic Salary ----
        if (payroll.getBasicSalary() == null) {
            return "Basic Salary is required.";
        }
        if (payroll.getBasicSalary() < 0) {
            return "Basic Salary cannot be negative.";
        }

        // ---- Allowance (HRA) ----
        if (payroll.getAllowance() == null) {
            return "HRA / Allowance is required (enter 0 if there is none).";
        }
        if (payroll.getAllowance() < 0) {
            return "HRA / Allowance cannot be negative.";
        }

        // ---- Bonus ----
        if (payroll.getBonus() == null) {
            return "Bonus is required (enter 0 if there is none).";
        }
        if (payroll.getBonus() < 0) {
            return "Bonus cannot be negative.";
        }

        // ---- Tax Deduction ----
        if (payroll.getTaxDeduction() == null) {
            return "Tax Deduction is required (enter 0 if there is none).";
        }
        if (payroll.getTaxDeduction() < 0) {
            return "Tax Deduction cannot be negative.";
        }

        // ---- Other Deduction ----
        if (payroll.getOtherDeduction() == null) {
            return "Other Deduction is required (enter 0 if there is none).";
        }
        if (payroll.getOtherDeduction() < 0) {
            return "Other Deduction cannot be negative.";
        }

        // A salary where more is deducted than earned does not make sense
        double totalDeduction = payroll.getTaxDeduction() + payroll.getOtherDeduction();
        if (totalDeduction > payroll.getBasicSalary() + payroll.getAllowance() + payroll.getBonus()) {
            return "Total Deduction cannot be greater than the Gross Salary. "
                    + "Please check the deduction amounts.";
        }

        return null;   // no error
    }

    // ------------------------------------------------------------------
    //  SAVE / READ / DELETE
    // ------------------------------------------------------------------

    /**
     * Saves one payroll record into the TreeMap.
     *
     * @return null on success, otherwise the error message.
     */
    public String savePayroll(Payroll payroll) {
        String error = validateAndCalculate(payroll);
        if (error != null) {
            return error;
        }

        // Stamp the payroll with today's date and save it.
        payroll.setPayrollDateToToday();
        payrollRepository.save(payroll);
        return null;
    }

    /** All payroll records, already sorted by Employee ID (TreeMap order). */
    public List<Payroll> getAllPayrolls() {
        return payrollRepository.findAll();
    }

    /** One payroll record by Employee ID. */
    public Payroll getPayrollByEmployeeId(Integer employeeId) {
        return payrollRepository.findByEmployeeId(employeeId);
    }

    public boolean deletePayroll(Integer employeeId) {
        return payrollRepository.deleteByEmployeeId(employeeId);
    }

    public int getPayrollCount() {
        return payrollRepository.count();
    }

    public double getTotalNetSalaryPaid() {
        return round(payrollRepository.totalNetSalaryPaid());
    }

    public double getTotalGrossSalary() {
        double total = 0;
        for (Payroll payroll : payrollRepository.findAll()) {
            total += safeValue(payroll.getGrossSalary());
        }
        return round(total);
    }

    public double getTotalDeducted() {
        return round(payrollRepository.totalDeducted());
    }

    public double getAverageNetSalary() {
        return round(payrollRepository.averageNetSalary());
    }

    public Payroll getHighestPaidRecord() {
        return payrollRepository.getHighestPaidRecord();
    }

    public Payroll getLowestPaidRecord() {
        return payrollRepository.getLowestPaidRecord();
    }

    public List<Payroll> getRecentPayrolls(int limit) {
        return payrollRepository.getRecentPayrolls(limit);
    }

    public Integer getFirstEmployeeId() {
        return payrollRepository.getFirstEmployeeId();
    }

    public Integer getLastEmployeeId() {
        return payrollRepository.getLastEmployeeId();
    }

    /** Search payroll history by employee ID or by employee name. */
    public List<Payroll> searchPayrolls(String searchBy, String keyword) {
        List<Payroll> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPayrolls();
        }
        String text = keyword.trim().toLowerCase();
        String mode = (searchBy == null || searchBy.isBlank()) ? "all" : searchBy.toLowerCase();

        for (Payroll payroll : payrollRepository.findAll()) {   // records come sorted by ID
            boolean matches = false;

            if (mode.equals("id")) {
                try {
                    matches = payroll.getEmployeeId().equals(Integer.valueOf(keyword.trim()));
                } catch (NumberFormatException e) {
                    matches = false;
                }
            } else if (mode.equals("name")) {
                matches = payroll.getEmployeeName() != null
                        && payroll.getEmployeeName().toLowerCase().contains(text);
            } else {
                matches = (payroll.getEmployeeName() != null
                           && payroll.getEmployeeName().toLowerCase().contains(text))
                        || String.valueOf(payroll.getEmployeeId()).equals(keyword.trim());
            }

            if (matches) {
                result.add(payroll);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    //  SAMPLE PAYROLL DATA
    // ------------------------------------------------------------------

    /**
     * Creates a few payroll records on startup so the Payroll History page is
     * not empty when the project is demonstrated.
     */
    public void loadSamplePayrolls() {
        if (payrollRepository.count() > 0) {
            return;
        }

        // employeeId, HRA, bonus, tax deduction, other deduction (PF etc.)
        createSample(101, 11000, 5000, 2750, 2200);
        createSample(102, 7600, 3000, 1900, 1520);
        createSample(103, 8400, 4000, 2100, 1680);
        createSample(104, 9000, 4500, 2250, 1800);
        createSample(105, 12000, 7000, 3000, 2400);

        System.out.println("Loaded " + payrollRepository.count() + " sample payroll records (TreeMap).");
    }

    /** Helper that builds one sample payroll record and saves it. */
    private void createSample(int employeeId, double allowance, double bonus,
                              double tax, double otherDeduction) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee == null) {
            return;
        }
        Payroll payroll = new Payroll(employeeId, employee.getName(),
                employee.getBasicSalary(), allowance, bonus, tax, otherDeduction);
        payroll.setDepartment(employee.getDepartment());
        payroll.setDesignation(employee.getDesignation());
        savePayroll(payroll);
    }

    // ------------------------------------------------------------------
    //  SMALL HELPERS
    // ------------------------------------------------------------------

    /** Treats null as 0 so we never get a NullPointerException while adding numbers. */
    private double safeValue(Double value) {
        return value == null ? 0 : value;
    }

    /** Rounds a money amount to 2 decimal places. */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}