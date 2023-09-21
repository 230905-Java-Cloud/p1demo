package com.revature.services;

import com.revature.daos.EmployeeDAO;
import com.revature.models.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class EmployeeService {

    private EmployeeDAO eDAO;

    @Autowired //Constructor Injection! EmployeeDAO Bean will get automagically injected into this service
    public EmployeeService(EmployeeDAO eDAO) {
        this.eDAO = eDAO;
    }

    //TODO: get all, insert, findById

    //getEmployeeByUsername
    public Employee findByEmployeeUsername(String username){

        if(username.equals(null) || username.equals("")){
            throw new IllegalArgumentException("Can't find a user without a username!");
        }

        //attempt the find the employee, and store it in an optional
        Optional<Employee> e = eDAO.findByUsername(username);

        //if the returned Employee is present in the Optional...
        if(e.isPresent()){
            return e.get(); //return the employee
        } else {
            throw new IllegalArgumentException("Username not found!");
        }

    }

    //here's why I don't love PUTs and prefer patches.
    //we need to insert an ENTIRE employee, with an entire role, to keep our data intact
    //with patch, we can just go in, and update one piece
    public Employee updateEntireEmployee(Employee employee){

        if(employee.getUsername().equals(null) || employee.getUsername().equals("")){
            throw new IllegalArgumentException("username must not update to null!");
        }

        if(employee.getPassword().equals(null) || employee.getPassword().equals("")){
            throw new IllegalArgumentException("password must not update to null!");
        }

        //updating in spring data uses the save() method. It's not just for inserts!
        //is save() is used on an existing record, it will update instead of create a new one.

        //The ID should never change, so this is what we'll use to gather the employee (Thanks Nick)
        Optional<Employee> empFromDatabase = eDAO.findById(employee.getEmployeeId());

        //if the employee is present, perform the update
        if(empFromDatabase.isPresent()){
            return eDAO.save(employee); //perform the update!
            //the employee exists, so this will update instead of make a new record
        } else {
            throw new IllegalArgumentException("Employee was not found! Aborting update");
        }

    }

    //PATCH request (updating only the username of an employee)
    public Employee updateEmployeeUsername(int empId, String username){

        //TODO: we could add username checks here similar to the PUT method

        //Gather the employee by id
        Optional<Employee> originalEmployee = eDAO.findById(empId);

        if(originalEmployee.isPresent()){

            Employee empToUpdate = originalEmployee.get();
            empToUpdate.setUsername(username); //change the username to what was given in the params

            return eDAO.save(empToUpdate); //perform the update

        } else {
            throw new IllegalArgumentException("Employee was not found! Aborting update.");
        }

    }

}
