package com.salmat.pas.dao;

import java.util.List;

import com.salmat.genericdao.GenericDao;
import com.salmat.pas.vo.*;

public interface AdminUserRoleDao<T> extends GenericDao<AdminUserRole, Long> {
    List<AdminUserRole> findByAll();
    List<AdminUserRole> findByUserRole(String userRole);
    //List<AdminUserRole> findById(Long id);
}