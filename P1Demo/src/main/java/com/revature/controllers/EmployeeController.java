package com.revature.controllers;

import com.revature.daos.EmployeeDAO;
import com.revature.models.Employee;
import com.revature.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController //a subset of the @Controller stereotype annotation. makes a class a bean, plus MVC stuff!
@RequestMapping("/employee") //every request to 5000/p1/employee will go to this Class
@CrossOrigin() //Configuring this annotation allows us to take in HTTP requests from different origins (FE?)
public class EmployeeController {

    //we need to AUTOWIRE the DAO, to inject it as a dependency of the controller
    //(since we need to use its methods)
    private EmployeeDAO eDAO;
    private EmployeeService es;

    @Autowired //remember, spring boot will automagically inject an eDAO thanks to this annotation
    public EmployeeController(EmployeeDAO eDAO, EmployeeService es) {
        this.eDAO = eDAO;
        this.es = es;
    }

    //HTTP REQUESTS--------------------------

    //this method will get all employees. it will be reached by a GET request to /employee
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(){

        //we will call on the DAO within our return to get our Employees

        //return a ResponseEntity, set the status code to 200 (OK), and set the response body data
        return ResponseEntity.status(200).body(eDAO.findAll());

        //no error handling in this, see methods below

    }


    @PostMapping //this method accept HTTP POST requests ending in /employee
    public ResponseEntity<Employee> addEmployee(@RequestBody Employee e){

        //thanks to @RequestBody in the parameter of this method...
        //the body of the request will get automatically converted into an Employee object called e

        //save the incoming employee to the DB, and save the return in a variable (for error handling)
        Employee newEmp = eDAO.save(e);

        //if insert fails, newEmp will be null
        //TODO: try/catch instead of null check
        if(newEmp == null){
            //return a 400 status code (BAD REQUEST) and no response body (.build())
            return ResponseEntity.badRequest().build();
        }

        //return a 202 (ACCEPTED) status code, as well as the new user in the response body
        return ResponseEntity.accepted().body(newEmp);
        //accepted() is a shorthand of .status(202). They do the same thing

    }

    //this method takes in an id in the request params and returns the Employee with that id
    @GetMapping("/id/{id}") //get requests to /employee/SOME-VALUE will be here
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") int id){

        //@PathVariable will allow us to get the user-inputted PATH VARIABLE sent in the request

        //if the user sends in a invalid id, send a bad request (400) status code and no response body
        if(id <= 0){
            return ResponseEntity.badRequest().build(); //build is used to send no data back
        }

        /*findById() from JpaRepository returns an "Optional"
        Optionals lend to code flexibility because it MAY or MAY NOT contain the request object
        This helps us avoid NullPointerExceptions */
        Optional<Employee> e = eDAO.findById(id);

        //we can check if the optional has our value with .isPresent() or .isEmpty()
        if(e.isPresent()){
            //we can extract the Optional's data with .get()
            return ResponseEntity.ok().body(e.get()); //return the Employee with a 200 status code
        } else {
            return ResponseEntity.noContent().build(); //otherwise, return no content
        }
    }

    //get by username - talked about "ambiguous handler mapping" error
    @GetMapping("/username/{username}")
    public ResponseEntity<Object> getEmployeeByUsername(@PathVariable("username") String username){

        try {
            Employee employee = es.findByEmployeeUsername(username);
            return ResponseEntity.ok().body(employee);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }


    //This method will update an ENTIRE employee record (PUT request)
    @PutMapping
    public ResponseEntity<Object> updateEntireEmployee(@RequestBody Employee employee){

        try{
            //if all goes well, return the updated employee with a 202 status code
            return ResponseEntity.accepted().body(es.updateEntireEmployee(employee));
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    //TODO: patch for update username

}