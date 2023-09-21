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

    //We are establishing a Many-to-One relationship: many employee can have the same role.
    //This will make a field for the FK in the employees table
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "roleId") //This is how we specify the PK that this FK points to
    //The name attribute of @JoinColumn will name the column in your table
    //IMPORTANT NOTE: using @Column here will break this. @JoinColumn takes the role of @Column here
    private Role role;

    //TODO: Ben will see if there's an easier way to insert employee (without helper variable)

    /* WHAT are fetch and cascade?

    fetch - sets whether the dependency (Role) is eagerly or lazily loaded
    (typically we want eager loading so the relationship is ready whenever we need it)

    cascade - sets how changes in a table CASCADE to dependent records
    (with CascadeType.ALL, if a Role is updated/deleted, that operation will cascade to any Employees) */

    //boilerplate--------------------------------

    public Employee() {
    }

    public Employee(int employeeId, String username, String password, Role role) {
        this.employeeId = employeeId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Employee(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    //This constructor will be used for inserts.
    //The ID is auto generated...
    //and the Role will be added with setRole() before saving to the DB (see the Service method)
    public Employee(String username, String password) {
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId=" + employeeId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }
}
