package com.revature.controllers;

import com.revature.daos.EmployeeDAO;
import com.revature.models.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping //this method accept HTTP POST requests ending in /employee
    public ResponseEntity<Employee> addEmployee(@RequestBody Employee e){

        //thanks to @RequestBody in the parameter of this method...
        //the body of the request will get automatically converted into an Employee object called e

        //save the incoming employee to the DB, and save the return in a variable (for error handling)
        Employee newEmp = eDAO.insertEmployee(e);

        //return a 202 (ACCEPTED) status code, as well as the new user in the response body
        return ResponseEntity.accepted().body(newEmp);
        //accepted() is a shorthand of .status(202). They do the same thing

    }

    //this method takes in an id in the request params and returns the Employee with that id
    @GetMapping("/{id}") //get requests to /employee/SOME-VALUE will be here
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") int id){

        //@PathVariable will allow us to get the user-inputted PATH VARIABLE sent in the request

        //if the user sends in a invalid id, send a bad request (400) status code and no response body
        if(id <= 0){
            return ResponseEntity.badRequest().build(); //build is used to send no data back
        }

        //get an Employee by id from the DAO
        Employee e = eDAO.getEmployeeById(id);

        //if there is no user associated with the inputted id, send a 204 (no content) and no response body
        if(e == null) {
            return ResponseEntity.noContent().build();
        }

        //if none of the checks get activated, send the user!
        return ResponseEntity.ok().body(e);

    }

}
