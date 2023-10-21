package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String directReportUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        directReportUrl = "http://localhost:" + port + "/employee/reporting/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testReportingStructure() {
        // Create 4 employees in a hierarchical structure, and POST them all to be saved
        Employee testEmployeeGrandchild = new Employee();
        testEmployeeGrandchild.setFirstName("John");
        testEmployeeGrandchild.setLastName("Stevenson");
        testEmployeeGrandchild.setDepartment("Engineering");
        testEmployeeGrandchild.setPosition("Junior Developer");
        String grandchildId = restTemplate.postForEntity(employeeUrl, testEmployeeGrandchild, Employee.class).getBody().getEmployeeId();
        testEmployeeGrandchild.setEmployeeId(grandchildId);

        Employee testEmployeeChild1 = new Employee();
        testEmployeeChild1.setFirstName("Jane");
        testEmployeeChild1.setLastName("Doe");
        testEmployeeChild1.setDepartment("Engineering");
        testEmployeeChild1.setPosition("Senior Developer");
        testEmployeeChild1.setDirectReports(Arrays.asList(testEmployeeGrandchild));
        String child1Id = restTemplate.postForEntity(employeeUrl, testEmployeeChild1, Employee.class).getBody().getEmployeeId();
        testEmployeeChild1.setEmployeeId(child1Id);

        Employee testEmployeeChild2 = new Employee();
        testEmployeeChild2.setFirstName("Steve");
        testEmployeeChild2.setLastName("Johnson");
        testEmployeeChild2.setDepartment("Engineering");
        testEmployeeChild2.setPosition("Senior Developer");
        String child2Id = restTemplate.postForEntity(employeeUrl, testEmployeeChild2, Employee.class).getBody().getEmployeeId();
        testEmployeeChild2.setEmployeeId(child2Id);

        Employee testEmployeeParent = new Employee();
        testEmployeeParent.setFirstName("John");
        testEmployeeParent.setLastName("Doe");
        testEmployeeParent.setDepartment("Engineering");
        testEmployeeParent.setPosition("Development Manager");
        testEmployeeParent.setDirectReports(Arrays.asList(testEmployeeChild1, testEmployeeChild2));
        String parentId = restTemplate.postForEntity(employeeUrl, testEmployeeParent, Employee.class).getBody().getEmployeeId();

        // Check reporting structure for the top-level employee in the structure (should have 3 employees under them)
        ReportingStructure readStructure = restTemplate.getForEntity(directReportUrl, ReportingStructure.class, parentId).getBody();

        assertNotNull(readStructure.getNumberOfReports());
        assertEquals(parentId, readStructure.getEmployeeId());
        assertEquals(3, readStructure.getNumberOfReports());
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
