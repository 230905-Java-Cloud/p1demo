package com.revature.controllers;

import com.revature.daos.EmployeeDAO;
import com.revature.models.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController //a subset of the @Controller stereotype annotation. makes a class a bean, plus MVC stuff!
@RequestMapping("/employee") //every request to 5000/p1/employee will go to this Class
@CrossOrigin() //Configuring this annotation allows us to take in HTTP requests from different origins (FE?)
public class EmployeeController {

    //we need to AUTOWIRE the DAO, to inject it as a dependency of the controller
    //(since we need to use its methods)
    private EmployeeDAO eDAO;

    @Autowired //remember, spring boot will automagically inject an eDAO thanks to this annotation
    public EmployeeController(EmployeeDAO eDAO){
        this.eDAO = eDAO;
    }

    //HTTP REQUESTS--------------------------

    //this method will get all employees. it will be reached by a GET request to /employee
    @GetMapping
    public ResponseEntity<ArrayList<Employee>> getAllEmployees(){

        //call on the DAO to get our Employees
        ArrayList<Employee> employees = eDAO.getAllEmployees();

        //return a ResponseEntity, set the status code to 200 (OK), and set the response body data
        return ResponseEntity.status(200).body(employees);

        //no error handling in this, see methods below

    }



}
