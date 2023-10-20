package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Finding employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure getReportingStructure(String id) {
        Employee employee = employeeRepository.findByEmployeeId(id);
        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        ReportingStructure reporting = new ReportingStructure();
        
        // // More efficient recursive method of getting all reports - doesn't work with local mongo-java-server plugin
        // ReportingStructure aggResults = employeeRepository.getAllDirectReportsByEmployeeId(id).getMappedResults().get(0);
        // reporting.setEmployeeId(aggResults.getEmployeeId());
        // reporting.setNumberOfReports(aggResults.getNumberOfReports());

        // Get IDs of each employee in the directReports array for the selected parent employee (ID is the only field present on these employee objects)
        List<String> allDirectReportIds = new ArrayList<String>(employee.getDirectReports().stream().map(e -> e.getEmployeeId()).collect(Collectors.toList()));
        List<String> employeeIdsToCheckNesting = new ArrayList<String>(allDirectReportIds);

        // As long as there are more employeeIds that haven't been checked for nesting, keep traversing through levels
        while (!employeeIdsToCheckNesting.isEmpty()) {
            // Get full employee object for each ID that needs to be checked for nesting
            // Only get the full object if that employee actually has a populated directReports array - if there is no more nesting, this will return empty
            List<Employee> employeesWithNesting = employeeRepository.findAllByEmployeeIdsAndDirectReportsNotEmpty(employeeIdsToCheckNesting);

            // Empty this list to refill with the next layer of nesting (if needed)
            employeeIdsToCheckNesting.clear();

            // If more nesting was found, add the newly-found nested employee IDs to the full list
            // Also add them to the "checkNesting" array so that this process can be repeated to check for another layer of nesting
            if (!employeesWithNesting.isEmpty()) {
                List<String> nextLevelEmployeeIds = new ArrayList<String>();
                employeesWithNesting.forEach(empl -> {
                    empl.getDirectReports().forEach(dr -> nextLevelEmployeeIds.add(dr.getEmployeeId()));
                });
                allDirectReportIds.addAll(nextLevelEmployeeIds);
                employeeIdsToCheckNesting.addAll(nextLevelEmployeeIds);
            }
        }

        reporting.setEmployeeId(employee.getEmployeeId());
        reporting.setNumberOfReports(allDirectReportIds.size());

        return reporting;
    }
}
