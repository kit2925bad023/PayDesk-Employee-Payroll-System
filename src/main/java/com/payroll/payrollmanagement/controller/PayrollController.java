package com.payroll.payrollmanagement.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.payroll.payrollmanagement.model.Employee;
import com.payroll.payrollmanagement.model.Payroll;
import com.payroll.payrollmanagement.service.EmployeeService;
import com.payroll.payrollmanagement.service.PayrollService;

/**
 * PAYROLL CONTROLLER.
 *
 * Handles the salary calculator and the payroll history:
 *   GET  /payroll            - open the salary calculation page
 *   POST /payroll/calculate  - calculate and show the salary summary
 *   POST /payroll/save       - save the record into the TreeMap
 *   GET  /payroll/history    - payroll history sorted by Employee ID
 *   POST /payroll/delete/{id}- delete one payroll record
 */
@Controller
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;

    public PayrollController(PayrollService payrollService, EmployeeService employeeService) {
        this.payrollService = payrollService;
        this.employeeService = employeeService;
    }

    /**
     * Opens the salary calculator page.
     *
     * @param employeeId optional - when the user clicks "Calculate Salary" on the
     *                  Employees page, the ID arrives here and the basic salary
     *                  of that employee is filled in automatically.
     */
    @GetMapping("/payroll")
    public String showPayrollPage(@RequestParam(name = "employeeId", required = false) Integer employeeId,
                                  Model model) {

        model.addAttribute("activePage", "payroll");

        Payroll payroll = new Payroll();

        if (employeeId != null) {
            // HashMap lookup - instant result
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee != null) {
                payroll.setEmployeeId(employee.getEmployeeId());
                payroll.setEmployeeName(employee.getName());
                payroll.setBasicSalary(employee.getBasicSalary());
                payroll.setDepartment(employee.getDepartment());
                payroll.setDesignation(employee.getDesignation());

                // A sensible starting allowance (10% of basic salary)
                payroll.setAllowance(Math.round(employee.getBasicSalary() * 0.10 * 100.0) / 100.0);

                // Existing record is shown in the summary card if there is one
                model.addAttribute("existingRecord", payrollService.getPayrollByEmployeeId(employeeId));
            } else {
                model.addAttribute("error", "No employee found with Employee ID " + employeeId + ".");
            }
        } else {
            // No employee chosen yet -> show the existing record as a hint
            model.addAttribute("existingRecord", null);
        }

        model.addAttribute("payroll", payroll);
        model.addAttribute("employees", employeeService.getAllEmployees());   // for the drop-down
        model.addAttribute("pageTitle", "Salary Calculator");

        return "payroll";   // renders templates/payroll.html
    }

    /**
     * Calculates the salary and shows the summary card.
     * The data is NOT saved yet - the user must press "Save Payroll Record".
     */
    @PostMapping("/payroll/calculate")
    public String calculatePayroll(@ModelAttribute("payroll") Payroll payroll,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        // Fill the employee name / department from the employee records (HashMap)
        payrollService.fillEmployeeDetails(payroll);

        // Validate the inputs and calculate gross, total deduction and net salary
        String error = payrollService.validateAndCalculate(payroll);

        if (error != null) {
            model.addAttribute("error", error);
        }

        model.addAttribute("activePage", "payroll");
        model.addAttribute("payroll", payroll);
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("pageTitle", "Salary Calculator");
        model.addAttribute("calculated", error == null);
        model.addAttribute("existingRecord", null);

        return "payroll";
    }

    /**
     * Saves one payroll record into the TreeMap and goes to the history page.
     */
    @PostMapping("/payroll/save")
    public String savePayroll(@ModelAttribute("payroll") Payroll payroll,
                              RedirectAttributes redirectAttributes) {

        // Fill employee details, validate, calculate and store
        String error = payrollService.savePayroll(payroll);

        if (error != null) {
            // Go back to the calculator with the message and the entered values
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/payroll";
        }

        redirectAttributes.addFlashAttribute("success",
                "Payroll record for " + payroll.getEmployeeName() + " (ID " + payroll.getEmployeeId()
                        + ") saved successfully. Net Salary = Rs. "
                        + String.format("%,.2f", payroll.getNetSalary()));

        return "redirect:/payroll/history";
    }

    /**
     * PAYROLL HISTORY page.
     *
     * The records come from a TreeMap<Integer, Payroll>, so they are ALWAYS
     * displayed sorted by Employee ID (lowest ID first).
     */
    @GetMapping("/payroll/history")
    public String showPayrollHistory(@RequestParam(name = "searchBy", required = false) String searchBy,
                                     @RequestParam(name = "keyword", required = false) String keyword,
                                     Model model) {

        model.addAttribute("activePage", "history");

        String searchOption = (searchBy == null || searchBy.isBlank()) ? "all" : searchBy;
        String searchText = (keyword == null) ? "" : keyword.trim();

        List<Payroll> payrolls;
        if (!searchText.isEmpty()) {
            payrolls = payrollService.searchPayrolls(searchOption, searchText);
        } else {
            // Already sorted by Employee ID thanks to the TreeMap
            payrolls = payrollService.getAllPayrolls();
        }

        model.addAttribute("payrolls", payrolls);
        model.addAttribute("searchBy", searchOption);
        model.addAttribute("keyword", searchText);

        // Statistics for the top of the page
        model.addAttribute("totalRecords", payrollService.getPayrollCount());
        model.addAttribute("resultCount", payrolls.size());
        model.addAttribute("totalNetSalary", payrollService.getTotalNetSalaryPaid());
        model.addAttribute("totalGrossSalary", payrollService.getTotalGrossSalary());
        model.addAttribute("totalDeductions", payrollService.getTotalDeducted());
        model.addAttribute("averageNetSalary", payrollService.getAverageNetSalary());
        model.addAttribute("highestPaid", payrollService.getHighestPaidRecord());

        // TreeMap sorted-order proof for the info box
        model.addAttribute("firstEmployeeId", payrollService.getFirstEmployeeId());
        model.addAttribute("lastEmployeeId", payrollService.getLastEmployeeId());

        return "history";   // renders templates/history.html
    }

    /**
     * Deletes one payroll record from the TreeMap.
     */
    @PostMapping("/payroll/delete/{id}")
    public String deletePayroll(@PathVariable("id") Integer id,
                                RedirectAttributes redirectAttributes) {

        Payroll payroll = payrollService.getPayrollByEmployeeId(id);

        if (payroll == null || !payrollService.deletePayroll(id)) {
            redirectAttributes.addFlashAttribute("error",
                    "Could not delete the payroll record of Employee ID " + id + ".");
        } else {
            redirectAttributes.addFlashAttribute("success",
                    "Payroll record of " + payroll.getEmployeeName() + " (ID " + id + ") deleted successfully.");
        }

        return "redirect:/payroll/history";
    }
}