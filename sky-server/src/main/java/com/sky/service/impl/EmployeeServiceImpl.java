package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.result.PageResult;
import org.springframework.beans.BeanUtils;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import javassist.expr.NewArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import ch.qos.logback.core.joran.util.beans.BeanUtil;  // ❌ 错误！



@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        //对前端传过来的密码进行md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    //新增员工
    @Override
    public void insert(EmployeeDTO employeeDTO) {
        //将DTO employeeDTO转换为Employee实体对象
        Employee employee = new Employee();
        //对象属性转换
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置默认值
        //密码：默认密码为123456，进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //状态：默认1是启用状态，0是锁定状态
        employee.setStatus(StatusConstant.ENABLE);
        //创建时间和更新时间、创建人和更新人：由AOP切面自动填充
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateUser(BaseContext.getCurrentId());
        //新增员工
        employeeMapper.insert(employee);
    }

    //分页查询员工
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageDTO) {
        //开始分页查询
        PageHelper.startPage(employeePageDTO.getPage(), employeePageDTO.getPageSize());
        //查询数据库
        Page<Employee> page = employeeMapper.pageQuery(employeePageDTO);
        //封装分页结果
        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(page.getResult());
        return pageResult;
    }

    //员工账号的启用/禁用
    @Override
    public void startOrStop(Long id, Integer status) {
        //更新员工状态
        Employee employee = new Employee();
        employee.setId(id);
        employee.setStatus(status);
        //updateTime和updateUser由AOP切面自动填充
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(BaseContext.getCurrentId());
        //更新员工状态
        employeeMapper.update(employee);
    }

    //根据员工id查询员工信息
    @Override
    public Employee getById(Long id) {
        //根据员工id查询员工信息
        Employee employee = employeeMapper.getById(id);
        //密码：屏蔽密码字段
        employee.setPassword("****");
        //返回员工信息
        return employee;
    }

    //修改员工信息
    @Override
    public void update(EmployeeDTO employeeDTO) {
        //将DTO employeeDTO转换为Employee实体对象
        Employee employee = new Employee();
        //对象属性转换
        BeanUtils.copyProperties(employeeDTO, employee);
        //updateTime和updateUser由AOP切面自动填充
        //employee.setUpdateUser(BaseContext.getCurrentId());
        //employee.setUpdateTime(LocalDateTime.now());
        //更新员工信息
        employeeMapper.update(employee);
    }

}