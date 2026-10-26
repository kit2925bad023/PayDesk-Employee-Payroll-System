package com.payroll.payrollmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.payroll.payrollmanagement.service.EmployeeService;
import com.payroll.payrollmanagement.service.PayrollService;

/**
 * DASHBOARD CONTROLLER.
 *
 * Handles the home page (http://localhost:8080/).
 *
 * The controller only prepares the data and chooses the HTML page.
 * The page itself is "index" (src/main/resources/templates/index.html).
 *
 * @Controller tells Spring this class handles web requests.
 * @GetMapping tells Spring which URL goes with which method.
 */
@Controller
public class DashboardController {

    private final EmployeeService employeeService;
    private final PayrollService payrollService;

    public DashboardController(EmployeeService employeeService, PayrollService payrollService) {
        this.employeeService = employeeService;
        this.payrollService = payrollService;
    }

    /**
     * "/" is the home page and "/dashboard" is a friendly extra URL.
     */
    @GetMapping({ "/", "/dashboard" })
    public String showDashboard(Model model) {

        // Used by the Thymeleaf pages to highlight the active sidebar menu item
        model.addAttribute("activePage", "dashboard");

        // ---- CARD 1 : Total Employees (size of the ArrayList) ----
        model.addAttribute("totalEmployees", employeeService.getEmployeeCount());

        // ---- CARD 2 : Total Payroll Records (size of the TreeMap) ----
        model.addAttribute("totalPayrollRecords", payrollService.getPayrollCount());

        // ---- CARD 3 : Total Salary Paid (sum of all net salaries) ----
        model.addAttribute("totalSalaryPaid", payrollService.getTotalNetSalaryPaid());

        // ---- Extra statistics ----
        model.addAttribute("totalGrossSalary", payrollService.getTotalGrossSalary());
        model.addAttribute("totalDeducted", payrollService.getTotalDeducted());
        model.addAttribute("averageNetSalary", payrollService.getAverageNetSalary());
        model.addAttribute("highestPaid", payrollService.getHighestPaidRecord());

        // ---- Recent information ----
        model.addAttribute("recentPayrolls", payrollService.getRecentPayrolls(5));
        model.addAttribute("recentEmployees", employeeService.getRecentEmployees(5));

        // ---- Department wise employee count ----
        model.addAttribute("departmentCounts", employeeService.getDepartmentWiseHeadCount());

        // ==========================================================
        //  "DATA STRUCTURES USED" section of the dashboard.
        //  We show the live SIZE of each collection so the numbers
        //  on screen really come from our ArrayList / HashMap / TreeMap.
        // ==========================================================
        model.addAttribute("arrayListSize", employeeService.getEmployeeCount());   // ArrayList<Employee>
        model.addAttribute("hashMapSize", employeeService.getEmployeeMapCount());  // HashMap<Integer, Employee>
        model.addAttribute("treeMapSize", payrollService.getPayrollCount());       // TreeMap<Integer, Payroll>

        // TreeMap feature demo : first and last key of the sorted payroll map
        model.addAttribute("firstPayrollEmployeeId", payrollService.getFirstEmployeeId());
        model.addAttribute("lastPayrollEmployeeId", payrollService.getLastEmployeeId());

        return "index";   // renders templates/index.html
    }
}