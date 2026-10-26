package com.payroll.payrollmanagement.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.payroll.payrollmanagement.model.Employee;
import com.payroll.payrollmanagement.repository.EmployeeRepository;

/**
 * EMPLOYEE SERVICE.
 *
 * The service layer sits between the controller (web layer) and the repository
 * (data layer). It contains:
 *   - the business rules
 *   - the VALIDATION of the form data
 *   - loading of sample data
 *
 * The controller stays thin and the repository stays simple.
 */
@Service
public class EmployeeService {

    /** The repository that owns our ArrayList and HashMap. */
    private final EmployeeRepository employeeRepository;

    /** List of departments used by the department drop-down on the form. */
    public static final List<String> DEPARTMENTS = List.of(
            "IT", "Human Resources", "Finance", "Marketing", "Operations", "Sales", "Support");

    /** Constructor injection - Spring passes the repository automatically. */
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // ------------------------------------------------------------------
    //  SAMPLE DATA - runs once when the application starts
    // ------------------------------------------------------------------
    public void loadSampleData() {
        // If employees already exist, do not load them twice.
        if (employeeRepository.count() > 0) {
            return;
        }

        employeeRepository.save(new Employee(101, "Rahul Sharma",
                "rahul.sharma@company.com", "9876543210", "IT",
                "Java Developer", 55000.00));

        employeeRepository.save(new Employee(102, "Priya Nair",
                "priya.nair@company.com", "9876543211", "Human Resources",
                "HR Executive", 38000.00));

        employeeRepository.save(new Employee(103, "Amit Verma",
                "amit.verma@company.com", "9876543212", "Finance",
                "Accounts Officer", 42000.00));

        employeeRepository.save(new Employee(104, "Sneha Iyer",
                "sneha.iyer@company.com", "9876543213", "Marketing",
                "Marketing Executive", 45000.00));

        employeeRepository.save(new Employee(105, "Vikram Singh",
                "vikram.singh@company.com", "9876543214", "Operations",
                "Operations Manager", 60000.00));

        employeeRepository.save(new Employee(106, "Ananya Das",
                "ananya.das@company.com", "9876543215", "IT",
                "Frontend Developer", 51000.00));

        System.out.println("Loaded " + employeeRepository.count() + " sample employees "
                + "(ArrayList + HashMap).");
    }

    // ------------------------------------------------------------------
    //  READ OPERATIONS
    // ------------------------------------------------------------------

    /** All employees (read from the ArrayList, sorted by ID). */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /** One employee by ID - this is the HashMap lookup. */
    public Employee getEmployeeById(Integer employeeId) {
        return employeeRepository.findById(employeeId);
    }

    /** Employee search by ID / name / department. */
    public List<Employee> searchEmployees(String searchBy, String keyword) {
        return employeeRepository.search(searchBy, keyword);
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            return getAllEmployees();
        }
        return employeeRepository.findByDepartment(department.trim());
    }

    public int getEmployeeCount() {
        return employeeRepository.count();
    }

    /** Size of the HashMap - shown on the dashboard to prove both are in sync. */
    public int getEmployeeMapCount() {
        return employeeRepository.mapCount();
    }

    /** Next free Employee ID for the Add Employee form. */
    public int getNextEmployeeId() {
        return employeeRepository.generateNextEmployeeId();
    }

    /** The department drop-down options. */
    public List<String> getDepartmentList() {
        return DEPARTMENTS;
    }

    /** Total basic salary of all employees (small extra dashboard statistic). */
    public double totalBasicSalary() {
        double total = 0;
        for (Employee employee : employeeRepository.findAll()) {
            total += (employee.getBasicSalary() == null) ? 0 : employee.getBasicSalary();
        }
        return total;
    }

    /** How many employees are in each department (used on the dashboard). */
    public Map<String, Integer> getDepartmentWiseHeadCount() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        for (Employee employee : employeeRepository.findAll()) {
            if (employee.getDepartment() != null) {
                summary.merge(employee.getDepartment(), 1, Integer::sum);
            }
        }
        return summary;
    }

    /** The newest employees (highest IDs) for the dashboard "recent employees" list. */
    public List<Employee> getRecentEmployees(int limit) {
        List<Employee> all = new ArrayList<>(employeeRepository.findAll()); // already sorted by ID
        if (all.size() > limit) {
            return new ArrayList<>(all.subList(all.size() - limit, all.size()));
        }
        return all;
    }

    // ------------------------------------------------------------------
    //  WRITE OPERATIONS
    // ------------------------------------------------------------------

    /**
     * Adds a NEW employee.
     *
     * @return null if the employee was saved successfully,
     *         or a friendly error message if something is wrong.
     */
    public String saveEmployee(Employee employee) {
        String error = validate(employee, false);
        if (error != null) {
            return error;
        }
        employeeRepository.save(employee);
        return null;
    }

    /**
     * Updates an EXISTING employee.
     *
     * @return null on success, otherwise the error message.
     */
    public String updateEmployee(Employee employee) {
        String error = validate(employee, true);
        if (error != null) {
            return error;
        }
        employeeRepository.update(employee);
        return null;
    }

    /** Deletes an employee from both the ArrayList and the HashMap. */
    public boolean deleteEmployee(Integer employeeId) {
        return employeeRepository.deleteById(employeeId);
    }

    // ------------------------------------------------------------------
    //  VALIDATION
    // ------------------------------------------------------------------

    /**
     * Checks every field of the employee form and returns the FIRST error found.
     * Returning null means the data is valid.
     *
     * @param editing true when we are updating an existing employee,
     *                false when we are adding a new one.
     */
    private String validate(Employee employee, boolean editing) {

        if (employee == null) {
            return "No employee data was received. Please try again.";
        }

        // ---- Employee ID ----
        if (employee.getEmployeeId() == null) {
            return "Employee ID is required.";
        }
        if (employee.getEmployeeId() <= 0) {
            return "Employee ID must be a positive number.";
        }
        // Unique ID check using the HashMap (containsKey is instant)
        if (!editing && employeeRepository.existsById(employee.getEmployeeId())) {
            return "Employee ID " + employee.getEmployeeId()
                    + " is already used by another employee. Please enter a unique Employee ID.";
        }
        if (editing && !employeeRepository.existsById(employee.getEmployeeId())) {
            return "Employee with ID " + employee.getEmployeeId() + " does not exist any more.";
        }

        // ---- Name ----
        if (isBlank(employee.getName())) {
            return "Employee Name is required.";
        }
        if (employee.getName().trim().length() < 3) {
            return "Employee Name must be at least 3 characters long.";
        }

        // ---- Email ----
        if (isBlank(employee.getEmail())) {
            return "Email is required.";
        }
        if (!isValidEmail(employee.getEmail())) {
            return "Invalid email format. Please enter an email like name@company.com";
        }

        // ---- Phone ----
        if (isBlank(employee.getPhone())) {
            return "Phone number is required.";
        }
        if (!isValidPhone(employee.getPhone())) {
            return "Invalid phone number. Please enter a 10 digit mobile number, e.g. 9876543210";
        }

        // ---- Department ----
        if (isBlank(employee.getDepartment())) {
            return "Department is required.";
        }

        // ---- Designation ----
        if (isBlank(employee.getDesignation())) {
            return "Designation is required.";
        }

        // ---- Basic Salary ----
        if (employee.getBasicSalary() == null) {
            return "Basic Salary is required.";
        }
        if (employee.getBasicSalary() < 0) {
            return "Basic Salary cannot be negative.";
        }
        if (employee.getBasicSalary() > 10000000) {
            return "Basic Salary looks too large. Please enter a valid amount.";
        }

        return null;   // no error found
    }

    /** Simple email check: text, then @, then domain, then a dot and more text. */
    private boolean isValidEmail(String email) {
        String trimmed = email.trim();
        return trimmed.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Phone check: we remove spaces, dashes and a leading +91,
     * then we must be left with exactly 10 digits (an Indian mobile number).
     */
    private boolean isValidPhone(String phone) {
        String digits = phone.trim().replaceAll("[\\s\\-()]", "");
        if (digits.startsWith("+91")) {
            digits = digits.substring(3);
        } else if (digits.startsWith("91") && digits.length() == 12) {
            digits = digits.substring(2);
        }
        return digits.matches("^[6-9]\\d{9}$");   // Indian mobiles start with 6,7,8 or 9
    }

    /** True when the text is null, empty or only spaces. */
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}