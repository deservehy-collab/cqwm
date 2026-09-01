package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增员工
     * @param employee
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into employee (username, name, password, phone, sex, id_Number, status, create_Time, update_Time, create_User, update_User) " +
            "values" +
            "(#{username}, #{name}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);

    //分页查询员工
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageDTO);

    //更新员工信息
    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    //根据员工id查询员工信息
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}