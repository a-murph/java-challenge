package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compUrl;
    private String compIdUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compUrl = "http://localhost:" + port + "/compensation";
        compIdUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    public void testCreateReadUpdate() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Compensation testComp = new Compensation();
        testComp.setEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");
        testComp.setSalary(80000.00);
        testComp.setEffectiveDate(formatter.parse("2022-12-01"));

        // Create checks
        Compensation createdComp = restTemplate.postForEntity(compUrl, testComp, Compensation.class).getBody();

        assertNotNull(createdComp.getEmployeeId());
        assertCompensationEquivalence(testComp, createdComp);

        // Read checks
        Compensation readComp = restTemplate.getForEntity(compIdUrl, Compensation.class, createdComp.getEmployeeId()).getBody();
        assertEquals(createdComp.getEmployeeId(), readComp.getEmployeeId());
        assertCompensationEquivalence(createdComp, readComp);
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployeeId(), actual.getEmployeeId());
        assertEquals(expected.getSalary(), actual.getSalary(), 0.1);
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }
}
