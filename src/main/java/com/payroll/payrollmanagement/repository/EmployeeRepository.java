package com.payroll.payrollmanagement.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.payroll.payrollmanagement.model.Employee;

/**
 * EMPLOYEE REPOSITORY.
 *
 * This is the only class that actually stores employee data in memory.
 * There is NO database in this version of the project, so the data lives only
 * while the application is running.
 *
 * ============================================================
 *  DATA STRUCTURES USED HERE
 * ============================================================
 *
 *  1) ArrayList<Employee> employeeList
 *     -> Stores ALL employee records in insertion order.
 *     -> Used when we DISPLAY the employee table (need to keep all records in a list).
 *
 *  2) HashMap<Integer, Employee> employeeMap
 *     -> Stores the SAME employees, but keyed by Employee ID.
 *     -> Used when we SEARCH / EDIT / DELETE one employee by ID.
 *     -> HashMap finds a value in O(1) (average) time, which is much faster
 *        than scanning an ArrayList one by one.
 *
 *  Both collections always contain the same employees. We keep them in sync:
 *  when an employee is added, updated or deleted, we update BOTH collections.
 *
 *  BONUS: the HashMap values can also be printed as a whole using keySet(),
 *  values() and entrySet() - see the printAllEmployees() method below.
 * ============================================================
 */
@Repository
public class EmployeeRepository {

    // ------------------------------------------------------------------
    //  ARRAYLIST : holds every employee so we can list / display them all
    // ------------------------------------------------------------------
    private final ArrayList<Employee> employeeList = new ArrayList<>();

    // ------------------------------------------------------------------
    //  HASHMAP : employeeId  ->  Employee   (fast lookup by ID)
    // ------------------------------------------------------------------
    private final HashMap<Integer, Employee> employeeMap = new HashMap<>();

    /**
     * Saves a NEW employee into BOTH collections.
     */
    public void save(Employee employee) {
        // 1) Add to the ArrayList (keeps insertion order for display)
        employeeList.add(employee);

        // 2) Add to the HashMap using Employee ID as the key (fast lookup)
        employeeMap.put(employee.getEmployeeId(), employee);
    }

    /**
     * Updates an EXISTING employee in BOTH collections.
     */
    public void update(Employee employee) {
        // HashMap: put() with an existing key REPLACES the old value automatically.
        employeeMap.put(employee.getEmployeeId(), employee);

        // ArrayList: we must find the object and replace it in the same position.
        for (int i = 0; i < employeeList.size(); i++) {
            if (employeeList.get(i).getEmployeeId().equals(employee.getEmployeeId())) {
                employeeList.set(i, employee);   // set() replaces the item at index i
                break;
            }
        }
    }

    /**
     * HASHMAP LOOKUP - finds an employee by ID in constant time.
     * Returns null when no employee has that ID.
     */
    public Employee findById(Integer employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeMap.get(employeeId);
    }

    /** HASHMAP LOOKUP - returns true if the employee ID is already taken. */
    public boolean existsById(Integer employeeId) {
        return employeeId != null && employeeMap.containsKey(employeeId);
    }

    /**
     * ARRAYLIST READ - returns every employee, sorted by Employee ID.
     * We copy the ArrayList (new ArrayList<>(...)) so that the outside code
     * cannot accidentally change our stored data.
     */
    public List<Employee> findAll() {
        List<Employee> copy = new ArrayList<>(employeeList);
        copy.sort(Comparator.comparing(Employee::getEmployeeId));
        return copy;
    }

    /**
     * ARRAYLIST + HASHMAP - removes an employee from both collections.
     */
    public boolean deleteById(Integer employeeId) {
        // HashMap: remove() returns the removed object, or null if it was not there
        Employee removed = employeeMap.remove(employeeId);

        if (removed == null) {
            return false;
        }

        // ArrayList: remove(Object) deletes that exact object
        employeeList.remove(removed);
        return true;
    }

    /** How many employees we have (ArrayList size). */
    public int count() {
        return employeeList.size();
    }

    /** Size of the HashMap - should always be the same as the ArrayList size. */
    public int mapCount() {
        return employeeMap.size();
    }

    /**
     * SEARCH - searches employees by ID, by name or by department.
     *
     * @param searchBy "id" | "name" | "department" | "all"
     * @param keyword  what the user typed in the search box
     */
    public List<Employee> search(String searchBy, String keyword) {
        List<Employee> result = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String text = keyword.trim().toLowerCase();
        String mode = (searchBy == null) ? "all" : searchBy.toLowerCase();

        for (Employee employee : employeeList) {   // iterate over the ArrayList

            boolean matches = false;

            if (mode.equals("id")) {
                // Searching by ID uses the HashMap (instant result)
                try {
                    Integer id = Integer.valueOf(keyword.trim());
                    matches = employee.getEmployeeId().equals(id);
                } catch (NumberFormatException e) {
                    matches = false;   // user typed letters in an ID box
                }
            } else if (mode.equals("name")) {
                matches = employee.getName() != null
                        && employee.getName().toLowerCase().contains(text);
            } else if (mode.equals("department")) {
                matches = employee.getDepartment() != null
                        && employee.getDepartment().toLowerCase().contains(text);
            } else {
                // "all" - match ID, name OR department
                matches = (employee.getName() != null && employee.getName().toLowerCase().contains(text))
                        || (employee.getDepartment() != null
                            && employee.getDepartment().toLowerCase().contains(text))
                        || String.valueOf(employee.getEmployeeId()).equals(keyword.trim());
            }

            if (matches) {
                result.add(employee);
            }
        }

        result.sort(Comparator.comparing(Employee::getEmployeeId));
        return result;
    }

    /** All employees of one department (used by the department filter). */
    public List<Employee> findByDepartment(String department) {
        List<Employee> result = new ArrayList<>();
        for (Employee employee : employeeList) {
            if (employee.getDepartment() != null
                    && employee.getDepartment().equalsIgnoreCase(department)) {
                result.add(employee);
            }
        }
        result.sort(Comparator.comparing(Employee::getEmployeeId));
        return result;
    }

    /**
     * Suggests the next free Employee ID, e.g. if 101..105 exist it returns 106.
     */
    public int generateNextEmployeeId() {
        int maxId = 100;
        for (Integer id : employeeMap.keySet()) {   // keySet() gives all the HashMap keys
            if (id != null && id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }

    /**
     * EDUCATIONAL METHOD (not used by the website).
     * Shows the three HashMap view methods: keySet(), values() and entrySet().
     * Useful to explain in a college presentation.
     */
    public String printAllEmployeesUsingHashMapViews() {
        StringBuilder output = new StringBuilder("\n--- HashMap keySet() : only the IDs ---\n");
        for (Integer key : employeeMap.keySet()) {
            output.append(key).append(" ");
        }

        output.append("\n\n--- HashMap values() : the employee objects ---\n");
        for (Employee employee : employeeMap.values()) {
            output.append(employee.getName()).append(" ");
        }

        output.append("\n\n--- HashMap entrySet() : ID + employee together ---\n");
        for (HashMap.Entry<Integer, Employee> entry : employeeMap.entrySet()) {
            output.append(entry.getKey()).append(" -> ")
                   .append(entry.getValue().getName()).append("\n");
        }
        return output.toString();
    }

    /** Removes every employee (used only when we re-load sample data). */
    public void clear() {
        employeeList.clear();
        employeeMap.clear();
    }

    /** Returns the ArrayList directly - only used for printing sizes. */
    public int listSize() {
        return employeeList.size();
    }

    /** Read-only view of the employee IDs (kept unmodifiable on purpose). */
    public List<Integer> allIds() {
        return Collections.unmodifiableList(new ArrayList<>(employeeMap.keySet()));
    }
}
