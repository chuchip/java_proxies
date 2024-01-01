package com.profesorp.dumb;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DumbImpl implements DumbInterface
{
    private static final Logger logger = LogManager.getLogger(DumbImpl.class);

    @Override
    public void sayHello()
    {
        logger.info("Hello");
    }
    @Override
    public int sum2Numbers(int num1,int num2)
    {
        logger.info("I'm adding the numbers {} {} ",num1,num2);
        return num1+num2;
    }

}