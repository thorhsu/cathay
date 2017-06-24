package com.salmat.genericdao.finder.impl;

import org.springframework.aop.support.DefaultIntroductionAdvisor;

@SuppressWarnings("serial")
public class FinderIntroductionAdvisor extends DefaultIntroductionAdvisor
{
    public FinderIntroductionAdvisor()
    {
        super(new FinderIntroductionInterceptor());
    }
}
