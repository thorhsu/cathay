package com.salmat.pas.dao;

import java.util.List;

import com.salmat.genericdao.GenericDao;
import com.salmat.pas.vo.AdmPageList;

public interface AdmPageListDao<T> extends GenericDao<AdmPageList, Long> {
    List<AdmPageList> findById(Long id);
    List<AdmPageList> findByAuthority();
    List<AdmPageList> findAllPage();
}