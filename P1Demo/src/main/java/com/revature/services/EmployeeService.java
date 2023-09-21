package com.revature.services;

import com.revature.daos.EmployeeDAO;
import com.revature.daos.RoleDAO;
import com.revature.models.Employee;
import com.revature.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private EmployeeDAO eDAO;
    private RoleDAO roleDAO;

    @Autowired //Constructor Injection! The DAO Beans will get automagically injected into this service
    public EmployeeService(EmployeeDAO eDAO, RoleDAO roleDAO) {
        this.eDAO = eDAO;
        this.roleDAO = roleDAO;
    }

    //not gonna bother error handling this one - it takes no parameters so there's little room for user error
    public List<Employee> getAllEmployees(){

        return eDAO.findAll();

    }


    //insert employee
    //NOTE: This takes in an employee with only a username and password.
    //the roleId variable is obtained through the path variable of the POST request.
    //Also note we needed to constructor inject the RoleDAO above. (@Autowired)
    public Employee insertEmployee(Employee e, int roleId){

        //TODO: some checks could go here to check for invalid employee fields and roleId > 0.

        //First, we must get the Role associated with the given roleId
        Optional<Role> role = roleDAO.findById(roleId);

        //if the given Role is found, we can proceed with insertion
        if(role.isPresent()){
            e.setRole(role.get()); //extract the Role object, and assign it to the Employee's Role
            return eDAO.save(e); //save the Employee to the DB, and return the saved Employee
        } else {
            throw new IllegalArgumentException("Role could not be found! Aborting Insert");
        }


    }

    //get Employee by id
    public Employee findByEmployeeId(int id){

        if(id <= 0){
            throw new IllegalArgumentException("Employees with an id of 0 or less surely can't exist!");
        }

        /*findById() from JpaRepository returns an "Optional"
        Optionals lend to code flexibility because it MAY or MAY NOT contain the requested object
        This helps us avoid NullPointerExceptions */
        Optional<Employee> employee = eDAO.findById(id);

        //we can check if the optional has our value with .isPresent() or .isEmpty()
        if(employee.isPresent()){
            return employee.get(); //we can extract the Optional's data with .get()
        } else {
            throw new IllegalArgumentException("Employee id " + id + " does not exist!");
        }

    }

    //get Employee by Username
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
