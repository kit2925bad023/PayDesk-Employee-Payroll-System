package com.payroll.payrollmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.payroll.payrollmanagement.service.EmployeeService;
import com.payroll.payrollmanagement.service.PayrollService;

/**
 * MAIN CLASS of the application.
 *
 * This is the entry point that starts the Spring Boot application.
 * When we run "mvn spring-boot:run", Spring Boot calls the main() method below.
 */
@SpringBootApplication
public class PayrollManagementApplication {

    public static void main(String[] args) {

        // 1. Start the Spring Boot application (creates the web server on port 8080)
        ConfigurableApplicationContext context = SpringApplication.run(PayrollManagementApplication.class, args);

        // 2. Get the service objects from Spring so we can add sample data.
        //    "No database in this version" -> data is stored only in Java collections,
        //    so we load sample records every time the application starts.
        EmployeeService employeeService = context.getBean(EmployeeService.class);
        PayrollService payrollService = context.getBean(PayrollService.class);

        // 3. Create 5 sample employees in the departments IT, HR, Finance, Marketing, Operations
        employeeService.loadSampleData();

        // 4. Create a few sample payroll records so the Payroll History page is not empty
        payrollService.loadSamplePayrolls();

        System.out.println("==================================================");
        System.out.println(" Employee Payroll Management System started!");
        System.out.println(" Open http://localhost:8080 in your browser");
        System.out.println("==================================================");
    }
}
