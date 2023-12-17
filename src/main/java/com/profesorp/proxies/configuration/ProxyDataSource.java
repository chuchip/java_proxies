package com.profesorp.proxies.configuration;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

@Slf4j
public class ProxyDataSource extends DelegatingDataSource {

    public static int numConnections=0;
    @Autowired
    @Qualifier("tenantHikariDataSource")
    HikariDataSource hikariDataSource;
    public ProxyDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }
    public synchronized static void setNumConnections(int connections) {
        numConnections=connections;
    }

    public synchronized static int getNumConnections() {
        return numConnections;
    }
    @Override
    public Connection getConnection() throws SQLException {
        final Connection connection = Objects.requireNonNull(getTargetDataSource()).getConnection();
        Connection c= getTenantAwareConnectionProxy(connection);
        setNumConnections(hikariDataSource.getHikariPoolMXBean().getActiveConnections());
        log.info("Create connection");
        return c;
    }
    protected Connection getTenantAwareConnectionProxy(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                ConnectionProxy.class.getClassLoader(),
                new Class[] {ConnectionProxy.class},
                new TenantAwareInvocationHandler(connection));
    }
    private class TenantAwareInvocationHandler implements InvocationHandler {
        private final Connection target;

        public TenantAwareInvocationHandler(Connection target) {
            this.target = target;
        }

        @Nullable
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "Tenant-aware proxy for target Connection [" + this.target.toString() + "]";
                case "unwrap":
                    if (((Class) args[0]).isInstance(proxy)) {
                        return proxy;
                    } else {
                        return method.invoke(target, args);
                    }
                case "isWrapperFor":
                    if (((Class) args[0]).isInstance(proxy)) {
                        return true;
                    } else {
                        return method.invoke(target, args);
                    }
                case "getTargetConnection":
                    return target;
                default:
                    if (method.getName().equals("close")) {
                        setNumConnections(hikariDataSource.getHikariPoolMXBean().getActiveConnections());
                        log.info("Closing connections. Active connections: "+numConnections);
                        //Thread.sleep(5000);
                    }
                    return method.invoke(target, args);
            }
        }
    }
}