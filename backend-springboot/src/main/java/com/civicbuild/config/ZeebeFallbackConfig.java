package com.civicbuild.config;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Configuration
public class ZeebeFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(ZeebeClient.class)
    public ZeebeClient zeebeClient() {
        return createMockProxy(ZeebeClient.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createMockProxy(Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("toString")) {
                            return "MockZeebeClientProxy";
                        }
                        if (method.getName().equals("equals")) {
                            return proxy == (args != null && args.length > 0 ? args[0] : null);
                        }
                        if (method.getName().equals("hashCode")) {
                            return System.identityHashCode(proxy);
                        }
                        Class<?> returnType = method.getReturnType();
                        if (returnType.equals(void.class)) {
                            return null;
                        }
                        if (returnType.isInterface()) {
                            return createMockProxy(returnType);
                        }
                        if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
                            return false;
                        }
                        if (returnType.isPrimitive()) {
                            return 0;
                        }
                        return null;
                    }
                }
        );
    }
}
