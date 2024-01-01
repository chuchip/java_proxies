package com.profesorp.proxies.controller;

import com.profesorp.proxies.configuration.ProxyDataSource;
import com.profesorp.proxies.entities.City;
import com.profesorp.proxies.repositories.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {
    final CityRepository cityRepository;

    @GetMapping("connections")
    public String getNumConnections()
    {
        return "Number of connections: "+ProxyDataSource.getNumConnections()+"\n";
    }
    @GetMapping()
    public Iterable<City> getCities()    throws Exception
    {
        Iterable<City> cities=cityRepository.findAll();
        Thread.sleep(4000);

        return cities;
    }
}
