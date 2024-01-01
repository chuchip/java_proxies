package com.profesorp.dumb;

public class DumbImpl implements DumbInterface
{
    @Override
    public void sayHello()
    {
        System.out.println("Hello");
    }
    @Override
    public int sum2Numbers(int num1,int num2)
    {
        System.out.println("I'm summing two numbers");
        return num1+num2;
    }

}