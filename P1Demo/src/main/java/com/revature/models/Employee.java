package com.revature.models;

import org.springframework.stereotype.Component;

import javax.persistence.*;

@Entity //This annotation registers this class as a DB table (AKA DB entities)
@Table(name = "employees") //This lets us give the DB table a different name (and other properties)
@Component //This is one of the 4 stereotype annotations
//A stereotype annotation makes a class a bean (so it can inject dependencies and be injected as a dep.)
public class Employee {

    @Id //This will make this field the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) //This makes our PK serial
    private int employeeId;

    @Column(nullable = false, unique = true) //@Column is how we can set constraints!
    private String username;

    //I would want this to also not be null, but I want to demonstrate that you DON'T need @Column
    //this will become a DB column even without @Column
    private String password;

    //TODO: add Role when we talk about Spring Data

    //boilerplate--------------------------------

    public Employee() {
    }

    public Employee(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Employee(int employeeId, String username, String password) {
        this.employeeId = employeeId;
        this.username = username;
        this.password = password;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId=" + employeeId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
