package com.payroll.payrollmanagement.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Repository;

import com.payroll.payrollmanagement.model.Payroll;

/**
 * PAYROLL REPOSITORY.
 *
 * Stores the payroll history in memory (no database).
 *
 * ============================================================
 *  DATA STRUCTURE USED HERE
 * ============================================================
 *
 *  TreeMap<Integer, Payroll> payrollMap
 *
 *  -> The KEY is the Employee ID and the VALUE is that employee's payroll record.
 *  -> IMPORTANT PROPERTY OF TreeMap: it always keeps its keys SORTED (in ascending
 *     order of Employee ID). So when we read all records, they are ALREADY sorted,
 *     without us writing any sorting code.
 *  -> Extra benefit: a TreeMap also gives fast lookup, searching, and range queries
 *     (firstEntry(), lastEntry(), headMap(), floorKey()...).
 *  -> Another TreeMap property: a key cannot be duplicated. So when the same
 *     employee is paid again, the old record is replaced automatically.
 *     That means one live payroll record per employee, which is what we want.
 *
 *  The other two required collections are used in EmployeeRepository:
 *     ArrayList<Employee>              - stores employee records
 *     HashMap<Integer, Employee>       - fast employee lookup by ID
 * ============================================================
 */
@Repository
public class PayrollRepository {

    // ------------------------------------------------------------------
    //  TREE MAP : Employee ID (key)  ->  Payroll (value), always sorted by ID
    // ------------------------------------------------------------------
    private final TreeMap<Integer, Payroll> payrollMap = new TreeMap<>();

    /**
     * Saves (or updates) one payroll record.
     * TreeMap.put() replaces the value if the key already exists.
     */
    public void save(Payroll payroll) {
        payrollMap.put(payroll.getEmployeeId(), payroll);
    }

    /**
     * Finds the payroll record of one employee by Employee ID.
     * TreeMap.get() is very fast because the map is kept sorted internally.
     */
    public Payroll findByEmployeeId(Integer employeeId) {
        if (employeeId == null) {
            return null;
        }
        return payrollMap.get(employeeId);
    }

    /**
     * Returns ALL payroll records.
     * Because the data is inside a TreeMap, the returned records are ALREADY
     * sorted by Employee ID (lowest ID first) - no sorting code needed here.
     */
    public List<Payroll> findAll() {
        return new ArrayList<>(payrollMap.values());
    }

    /** Removes the payroll record of one employee. */
    public boolean deleteByEmployeeId(Integer employeeId) {
        if (employeeId == null) {
            return false;
        }
        return payrollMap.remove(employeeId) != null;
    }

    /** Number of payroll records (TreeMap size). */
    public int count() {
        return payrollMap.size();
    }

    /**
     * TreeMap feature - firstKey() and lastKey() give the smallest and
     * largest Employee ID in the map. Here: the first employee who was paid.
     */
    public Integer getFirstEmployeeId() {
        return payrollMap.isEmpty() ? null : payrollMap.firstKey();
    }

    /** TreeMap feature - the largest Employee ID stored in the map. */
    public Integer getLastEmployeeId() {
        return payrollMap.isEmpty() ? null : payrollMap.lastKey();
    }

    /**
     * TreeMap feature - headMap() returns all records with an Employee ID
     * SMALLER than the given ID. Used by the dashboard statistics.
     */
    public List<Payroll> findRecordsBelowEmployeeId(Integer employeeId) {
        List<Payroll> result = new ArrayList<>();
        if (employeeId == null) {
            return result;
        }
        result.addAll(payrollMap.headMap(employeeId).values());
        return result;
    }

    /**
     * Total money paid to all employees (sum of every NET salary).
     */
    public double totalNetSalaryPaid() {
        double total = 0;
        for (Payroll payroll : payrollMap.values()) {
            total += (payroll.getNetSalary() == null) ? 0 : payroll.getNetSalary();
        }
        return total;
    }

    /** Total of all deductions made in the year. */
    public double totalDeducted() {
        double total = 0;
        for (Payroll payroll : payrollMap.values()) {
            total += (payroll.getTotalDeduction() == null) ? 0 : payroll.getTotalDeduction();
        }
        return total;
    }

    /**
     * Highest paid employee record (the employee with the biggest net salary).
     */
    public Payroll getHighestPaidRecord() {
        Payroll highest = null;
        for (Payroll payroll : payrollMap.values()) {
            if (highest == null
                    || (payroll.getNetSalary() != null && payroll.getNetSalary() > highest.getNetSalary())) {
                highest = payroll;
            }
        }
        return highest;
    }

    /** Lowest paid employee record. */
    public Payroll getLowestPaidRecord() {
        Payroll lowest = null;
        for (Payroll payroll : payrollMap.values()) {
            if (lowest == null
                    || (payroll.getNetSalary() != null && payroll.getNetSalary() < lowest.getNetSalary())) {
                lowest = payroll;
            }
        }
        return lowest;
    }

    /**
     * Average net salary of all payroll records.
     */
    public double averageNetSalary() {
        if (payrollMap.isEmpty()) {
            return 0;
        }
        return totalNetSalaryPaid() / payrollMap.size();
    }

    /**
     * RECENT PAYROLLS for the dashboard.
     *
     * The TreeMap is sorted by Employee ID, but the dashboard wants the most
     * RECENT records, so we:
     *   step 1: copy the TreeMap values into an ArrayList
     *   step 2: sort that ArrayList by payroll date (newest first)
     *   step 3: return only the first "limit" records
     *
     * This is a nice example of using a TreeMap together with an ArrayList.
     */
    public List<Payroll> getRecentPayrolls(int limit) {
        List<Payroll> recent = new ArrayList<>(payrollMap.values());

        // reversed() = newest date first. A record without a date is treated
        // as the oldest date so that it never causes an error.
        recent.sort(Comparator.comparing(
                (Payroll payroll) -> payroll.getPayrollDate() == null
                        ? LocalDate.MIN : payroll.getPayrollDate()).reversed());

        if (recent.size() > limit) {
            return new ArrayList<>(recent.subList(0, limit));
        }
        return recent;
    }

    /** Sum of net salary only for the employees of the given department. */
    public double totalNetSalaryOfDepartment(String department) {
        double total = 0;
        for (Map.Entry<Integer, Payroll> entry : payrollMap.entrySet()) {  // entrySet() gives key + value
            Payroll payroll = entry.getValue();
            if (payroll.getDepartment() != null
                    && payroll.getDepartment().equalsIgnoreCase(department)) {
                total += (payroll.getNetSalary() == null) ? 0 : payroll.getNetSalary();
            }
        }
        return total;
    }

    /** How many employees belong to each department (key = department, value = count). */
    public Map<String, Integer> getDepartmentWiseCount() {
        Map<String, Integer> summary = new java.util.LinkedHashMap<>();
        for (Payroll payroll : payrollMap.values()) {
            if (payroll.getDepartment() != null) {
                summary.merge(payroll.getDepartment(), 1, Integer::sum);
            }
        }
        return Collections.unmodifiableMap(summary);
    }

    /** Removes every payroll record (used only when re-loading sample data). */
    public void clear() {
        payrollMap.clear();
    }
}