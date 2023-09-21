package com.revature.services;

import com.revature.daos.EmployeeDAO;
import com.revature.models.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        Optional<Employee> e = eDAO.findByEmployeeUsername(username);

        //if the returned Employee is present in the Optional...
        if(e.isPresent()){
            return e.get(); //return the employee
        } else {
            throw new IllegalArgumentException("Username not found!");
        }

    }



}
