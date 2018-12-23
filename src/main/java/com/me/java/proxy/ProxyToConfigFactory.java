package com.me.java.proxy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Value {

    /**
     * The actual value expression: e.g. "#{systemProperties.myProp}".
     */
    String value();
}

/**
 * config interfaces, map the config properties file: 
 * db.url = 
 * db.validation =true 
 * db.pool.size = 100
 */
interface IConfig {

    @Value("db.url")
    String dbUrl();

    @Value("db.validation")
    boolean isValidated();

    @Value("db.pool.size")
    int poolSize();

}

//proxy class
public final class ProxyToConfigFactory {

    private ProxyToConfigFactory() {
    }

    public static IConfig create(final InputStream is) 
            throws IOException {

        final Properties properties = new Properties();
        properties.load(is);

        return (IConfig) Proxy.newProxyInstance
                (IConfig.class.getClassLoader(), new Class[] { IConfig.class },
                new PropertyMapper(properties));

    }

    public static final class PropertyMapper implements InvocationHandler {

        private final Properties properties;

        public PropertyMapper(Properties properties) {
            this.properties = properties;
        }

        public Object invoke(Object proxy, Method method, 
                Object[] args) throws Throwable {

            final Value value = method.getAnnotation(Value.class);

            if (value == null)
                return null;

            String property = properties.getProperty(value.value());
            if (property == null)
                return (null);

            final Class<?> returns = method.getReturnType();
            if (returns.isPrimitive()) {
                if (returns.equals(int.class))
                    return (Integer.valueOf(property));
                else if (returns.equals(long.class))
                    return (Long.valueOf(property));
                else if (returns.equals(double.class))
                    return (Double.valueOf(property));
                else if (returns.equals(float.class))
                    return (Float.valueOf(property));
                else if (returns.equals(boolean.class))
                    return (Boolean.valueOf(property));
            }

            return property;
        }

    }

    public static void main(String[] args) 
            throws FileNotFoundException, IOException {

        IConfig config = ProxyToConfigFactory
                        .create(new FileInputStream("doc/db.properties"));
        String dbUrl = config.dbUrl();
        boolean isLoginValidated = config.isValidated();
        int dbPoolSize = config.poolSize();
        System.out.println(dbUrl+" "+dbPoolSize+" "+isLoginValidated);
    }

}
