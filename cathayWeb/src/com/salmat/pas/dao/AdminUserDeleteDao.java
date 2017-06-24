package com.salmat.pas.dao;

import java.util.List;

import com.salmat.genericdao.GenericDao;
import com.salmat.pas.vo.*;

public interface AdminUserDeleteDao<T> extends GenericDao<AdminUserDelete, Long> {
    List<AdminUserDelete> findById(Long id);
}