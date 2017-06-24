package com.salmat.pas.dao;

import java.util.List;

import com.salmat.genericdao.GenericDao;
import com.salmat.pas.vo.*;

public interface AdminUserDao<T> extends GenericDao<AdminUser, Long> {
    List<AdminUser> findByUserIdAndPass(String userId, String userPassword);
    List<AdminUser> findByUserId(String userId);
    List<AdminUser> findByUserRole(String userRole);
    List<AdminUser> findById(Long id);
    List<AdminUser> findAllUser();
}