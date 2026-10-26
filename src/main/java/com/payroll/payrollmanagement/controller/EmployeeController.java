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
import com.payroll.payrollmanagement.service.EmployeeService;

/**
 * EMPLOYEE CONTROLLER.
 *
 * Handles all the employee pages:
 *   GET  /employees          - list + search employees
 *   GET  /employee/add       - open the Add Employee form
 *   POST /employee/add       - save a new employee
 *   GET  /employee/edit/{id} - open the Edit Employee form
 *   POST /employee/edit/{id} - update an employee
 *   POST /employee/delete/{id} - delete an employee
 *
 * After a successful add / edit / delete we use RedirectAttributes so the
 * message is shown once on the redirected page (Post/Redirect/Get pattern).
 */
@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * EMPLOYEE LIST + SEARCH PAGE.
     *
     * @param searchBy   "id", "name", "department" or "all" (from the search drop-down)
     * @param keyword    the text typed in the search box
     * @param department optional department filter from the drop-down
     */
    @GetMapping("/employees")
    public String showEmployees(@RequestParam(name = "searchBy", required = false) String searchBy,
                                @RequestParam(name = "keyword", required = false) String keyword,
                                @RequestParam(name = "department", required = false) String department,
                                Model model) {

        model.addAttribute("activePage", "employees");

        String searchOption = (searchBy == null || searchBy.isBlank()) ? "all" : searchBy;
        String searchText = (keyword == null) ? "" : keyword.trim();

        boolean searchActive = !searchText.isEmpty();
        boolean departmentActive = (department != null && !department.isBlank());

        List<Employee> employees;

        if (searchActive) {
            // SEARCH BY EMPLOYEE ID / NAME / DEPARTMENT
            employees = employeeService.searchEmployees(searchOption, searchText);
        } else if (departmentActive) {
            employees = employeeService.getEmployeesByDepartment(department);
        } else {
            employees = employeeService.getAllEmployees();
        }

        // Send the searched list to the page
        model.addAttribute("employees", employees);

        // Send the search box values back so the form still shows them
        model.addAttribute("searchBy", searchOption);
        model.addAttribute("keyword", searchText);
        model.addAttribute("selectedDepartment", (departmentActive ? department : ""));

        // Extra information for the page header
        model.addAttribute("totalEmployees", employeeService.getEmployeeCount());
        model.addAttribute("resultCount", employees.size());
        model.addAttribute("departmentList", employeeService.getDepartmentList());

        return "employees";   // renders templates/employees.html
    }

    /**
     * Opens the Add Employee form with an empty employee object.
     */
    @GetMapping("/employee/add")
    public String showAddEmployeeForm(Model model) {

        model.addAttribute("activePage", "employees");

        // New empty employee object bound to the form fields
        Employee employee = new Employee();
        employee.setEmployeeId(employeeService.getNextEmployeeId());   // suggest the next free ID

        model.addAttribute("employee", employee);
        model.addAttribute("departmentList", employeeService.getDepartmentList());
        model.addAttribute("pageTitle", "Add Employee");
        model.addAttribute("formAction", "/employee/add");
        model.addAttribute("isEdit", false);

        return "employee-form";   // renders templates/employee-form.html
    }

    /**
     * Saves a NEW employee (submitted by the Add Employee form).
     */
    @PostMapping("/employee/add")
    public String addEmployee(@ModelAttribute("employee") Employee employee,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        String error = employeeService.saveEmployee(employee);

        if (error != null) {
            // Something is wrong -> go back to the form and show the message
            model.addAttribute("error", error);
            model.addAttribute("activePage", "employees");
            model.addAttribute("departmentList", employeeService.getDepartmentList());
            model.addAttribute("pageTitle", "Add Employee");
            model.addAttribute("formAction", "/employee/add");
            model.addAttribute("isEdit", false);
            return "employee-form";
        }

        // Success -> go to the employee list and show a green message
        redirectAttributes.addFlashAttribute("success",
                "Employee " + employee.getName() + " (ID " + employee.getEmployeeId() + ") added successfully.");
        return "redirect:/employees";
    }

    /**
     * Opens the Edit Employee form for one employee ID.
     * The employee is found through the HashMap, so it is instant.
     */
    @GetMapping("/employee/edit/{id}")
    public String showEditEmployeeForm(@PathVariable("id") Integer id, Model model,
                                       RedirectAttributes redirectAttributes) {

        Employee employee = employeeService.getEmployeeById(id);

        if (employee == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Employee with ID " + id + " was not found.");
            return "redirect:/employees";
        }

        model.addAttribute("activePage", "employees");
        // A copy is given to the form so that cancelling really cancels
        model.addAttribute("employee", new Employee(employee));
        model.addAttribute("departmentList", employeeService.getDepartmentList());
        model.addAttribute("pageTitle", "Edit Employee");
        model.addAttribute("formAction", "/employee/edit/" + employee.getEmployeeId());
        model.addAttribute("isEdit", true);

        return "employee-form";
    }

    /**
     * Saves the changes of an existing employee.
     */
    @PostMapping("/employee/edit/{id}")
    public String updateEmployee(@PathVariable("id") Integer id,
                                 @ModelAttribute("employee") Employee employee,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        // The Employee ID comes from the URL, so we set it on the object too
        employee.setEmployeeId(id);

        String error = employeeService.updateEmployee(employee);

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("activePage", "employees");
            model.addAttribute("departmentList", employeeService.getDepartmentList());
            model.addAttribute("pageTitle", "Edit Employee");
            model.addAttribute("formAction", "/employee/edit/" + id);
            model.addAttribute("isEdit", true);
            return "employee-form";
        }

        redirectAttributes.addFlashAttribute("success",
                "Employee " + employee.getName() + " (ID " + employee.getEmployeeId() + ") updated successfully.");
        return "redirect:/employees";
    }

    /**
     * Deletes one employee from the ArrayList and the HashMap.
     * (The delete button on the Employees page posts to this URL.)
     */
    @PostMapping("/employee/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Integer id,
                                 RedirectAttributes redirectAttributes) {

        Employee employee = employeeService.getEmployeeById(id);

        if (employee == null || !employeeService.deleteEmployee(id)) {
            redirectAttributes.addFlashAttribute("error",
                    "Could not delete employee with ID " + id + ". It may already be removed.");
        } else {
            redirectAttributes.addFlashAttribute("success",
                    "Employee " + employee.getName() + " (ID " + id + ") deleted successfully.");
        }

        return "redirect:/employees";
    }
}