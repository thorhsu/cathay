package com.salmat.pas.dao;

import java.util.List;

import com.salmat.genericdao.GenericDao;
import com.salmat.pas.vo.AdmPageUserAuthority;

public interface AdmPageUserAuthorityDao<T> extends GenericDao<AdmPageUserAuthority, Long> {
    List<AdmPageUserAuthority> findByRoleNotAccess(String role);
    List<AdmPageUserAuthority> findByAllEnable();
}

