package com.mindex.challenge.dao;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
@Document("employees")
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    Employee findByEmployeeId(String employeeId);

    @Query(value = "{ employeeId: { $in: ?0 }, directReports: { $exists: true, $ne: [] } }")
    List<Employee> findAllByEmployeeIdsAndDirectReportsNotEmpty(List<String> employeeIds);

    // Aggregation to recursively find all employees in the direct report tree under the given employee
    // Works in Mongo Shell but the fake local implementation created by mongo-java-server does not support $graphLookup
    @Aggregation(pipeline = {"{$match: {\n" +
            "   employeeId: ?0\n" +
            "}}",
            "{$graphLookup: {\n" +
            "   from: 'employees',\n" +
            "   startWith: '$directReports.employeeId',\n" +
            "   connectFromField: 'directReports.employeeId',\n" +
            "   connectToField: 'employeeId',\n" +
            "   as: 'allReports'\n" +
            "}}",
            "{$project: {\n" +
            "   employeeId: '$employeeId',\n" +
            "   numberOfReports: { $size: '$allReports' }\n" +
            "}}"})
    AggregationResults<ReportingStructure> getAllDirectReportsByEmployeeId(String employeeId);
}
