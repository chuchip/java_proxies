# Proxies en Java

####  ¿ Qué son y para qué sirven  ?

En el contexto de Java, un "proxy" se refiere a un objeto que actúa como intermediario o representante de otro objeto. El proxy permite  controlar el acceso al objeto principal y puede agregar funcionalidades  adicionales, como la seguridad, la manipulación de los datos o la  realización de tareas antes o después de que se llame a ciertos métodos  del objeto principal.

Nosotros vamos a hablar de proxies **dinámicos** que son los que nos permiten escribir modificar el comportamiento de un código escrito por terceros sin modificar las clases externas.

#### Ejemplo práctico

De acuerdo, como siempre, creo que esto se entenderá mejor con un ejemplo, que podéis encontrar en mi repositorio de github: https://github.com/chuchip/java_proxies

Este proyecto esta compuesto por dos módulos, siendo `dumb` el que funcionara como librería que luego el módulo `spring`usara.

Si nos fijamos, en el directorio **dumb** tenemos solo dos clases.

- **DumbImpl**
- **DumbInterface**

`DumbImpl`es la clase que implementa el interface `DumbInterface`que como se puede ver es bien sencilla:

```java
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
        System.out.println("I'm adding two numbers");
        return num1+num2;
    }
}
```

Para el ejemplo, vamos a suponer que no tenemos acceso al código fuente de esta módulo (paquete) y de hecho en el directorio `spring`,  dentro del fichero  **pom.xml** , podemos ver como se incluye como librería

```java
<dependency>
    <groupId>com.profesorp</groupId>
    <artifactId>dumb-library</artifactId>
    <version>1.1-SNAPSHOT</version>
</dependency>
```

Vamos, por lo tanto, a centrarnos en el modulo `spring`.

En la clase `SimpleProxyApp`tenemos este código:

```java
import com.profesorp.dumb.DumbImpl;
import com.profesorp.dumb.DumbInterface;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class SimpleProxyApp {
    public static void main(String[] args) {
        DumbInterface dumbClass= (DumbInterface) Proxy.newProxyInstance(
                DumbInterface.class.getClassLoader(),
                new Class[] {DumbInterface.class},
                new DumbProxy(new DumbImpl()));
        dumbClass.sayHello();
        log.info("Adding number {} to number {}. Result {}",1,2,  dumbClass.sum2Numbers(1,2));
        log.info("Adding number {} to number {}. Result {}",0,2,  dumbClass.sum2Numbers(0,2));
        log.info("Adding number {} to number {}. Result {}",0,0,  dumbClass.sum2Numbers(0,0));
      }
}
```

Voy a explicar el código poco a poco.

Usando la función estática `newProxyInstance`de la clase `java.lang.reflect.Proxy` vamos a crear un objeto que implemente el interface `DumbInterface`, para ello, como parámetros deberemos pasarle los siguientes parámetros:

- El cargador de clases de nuestro Interface: *DumbInterface.class.getClassLoader()*
- La clase de nuestro interfaz. *DumbInterface.class*
- Una instancia  de nuestra clase proxy (*DumbProxy*, la cual veremos un poco más adelante) a la que como parámetro en el constructor le pasaremos una instancia de la clase sobre la que queremos actuar como proxy. En nuestro caso *DumbImpl*

Como se ve en el código con esto tendremos una instancia de una clase que implementa el  interfaz `DumbInterface` . En las líneas siguientes llamamos a las dos funciones disponibles.

Resaltar que los proxis siempre se deben crear sobre un interfaz. Si la clase a la que le queremos poner un proxy no implementa un interfaz, deberíamos usar librerías como Byte Buddy (https://bytebuddy.net/)  o si estas trabajando con versiones de Java inferiores a la 17, podrías usar CGLIB (https://github.com/cglib/cglib).   Esto te puede dar una pista del porque siempre se recomienda usar interfaces en Spring Boot ?  ¡Exacto!. Spring Boot, usará  java.lang.reflect.Proxy siempre que pueda, ya que el utilizar otras librerías es mas lento y costoso.

Entonces, por recapitular, la llamada para crear un proxy, se hará pasando los primeros parámetros  haciendo referencia al **interface** y a nuestra clase proxy siempre hay que pasarle como parámetro  una instancia de la clase.

A continuación veremos que tiene la clase proxy.

```java
@Slf4j
class DumbProxy implements InvocationHandler
{
    Object target;
    public DumbProxy(Object target)
    {
        this.target=target;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("----");
        Object obj=null;
        switch (method.getName()) {
            case "sayHello":
                log.info("'sayHello' function was invoked");
                method.invoke(target);
                break;
            case "sum2Numbers":
                log.info("'sum2Numbers' function was invoked with numbers {}, {} ",args[0],args[1] );
                if ((Integer) args[0]==0)
                    args[0]=10; // Change the number 0 to 10.
                if ((Integer) args[1]==0)
                    obj=-1;
                else
                    obj=method.invoke(target,args);
                break;
            default:
                log.info("I don't know what function is this: "+method.getName());
        }
        return obj;
    }
```

Lo primero que debemos tener en cuenta es que la clase proxy debe implementar el interfaz `InvocationHandler`. Este interfaz es un interfaz funcional (por lo cual podríamos usar un lambda) con una única función:

```
Object invoke(Object proxy, Method method, Object[] args) throws Throwable 
```

El primer parámetro recibido (*proxy*) será la instancia del objeto del que hacemos de proxy (en nuestro caso `DumbImpl`). El segundo parámetro será el método al que se esta llamando y el tercer los parámetros con los que se le esta llamando al método.

Como se puede observar la clase `DumbProxy`tiene el constructor que recibe el objeto al que va hacer de proxy.

Como se puede ver fácilmente en el código, dependiendo del nombre de la función  invocada  y de los parámetros pasados podemos tomar determinadas acciones. Finalmente, si ejecutamos la función `invoke` de la clase `Method`recibida ejecutaremos el código de la clase sobre la que estamos haciendo de proxy.

Cuando iniciemos la aplicación sobre la clase DumbProxy, obtendremos la siguiente salida:

```html
----
06:39:58.425 [main] INFO com.profesorp.proxies.DumbProxy -- 'sayHello' function was invoked
06:39:58.432 [main] INFO com.profesorp.dumb.DumbImpl -- Hello
----
06:39:58.432 [main] INFO com.profesorp.proxies.DumbProxy -- 'sum2Numbers' function was invoked with numbers 1, 2 
06:39:58.441 [main] INFO com.profesorp.dumb.DumbImpl -- I'm adding the numbers 1 2 
06:39:58.441 [main] INFO com.profesorp.proxies.SimpleProxyApp -- Adding number 1 to number 2. Result 3
----
06:39:58.441 [main] INFO com.profesorp.proxies.DumbProxy -- 'sum2Numbers' function was invoked with numbers 0, 2 
06:39:58.441 [main] INFO com.profesorp.dumb.DumbImpl -- I'm adding the numbers 10 2 
06:39:58.441 [main] INFO com.profesorp.proxies.SimpleProxyApp -- Adding number 0 to number 2. Result 12
----
06:39:58.441 [main] INFO com.profesorp.proxies.DumbProxy -- 'sum2Numbers' function was invoked with numbers 0, 0 
06:39:58.441 [main] INFO com.profesorp.proxies.SimpleProxyApp -- Adding number 0 to number 0. Result -1

Process finished with exit code 0
```

Como se puede observar, antes de llamar a los métodos de la clase `DumbImpl` el código de la función `invoke`es ejecutado y nuestro proxy toma el control.

#### Proxies en Spring Boot

Como he comentado, los proxies son utilizados internamente a menudo  en el framework **Spring**. Por ejemplo cuando añadimos una etiqueta **@transacional** a una función de un servicio, Spring creara un proxy para poder iniciar y cerrar la transacción antes y después de llamar a nuestra función.

Si seguimos la clase `SimpleProxyApp` veremos un ejemplo de como sacar un mensaje de log que muestra el número de conexiones activas cuando se cierra una conexión a la base de datos en Spring Boot, usando JPA .

Como es un poco avanzado no voy a explicar los detalles, pero si miramos el código de la clase `ProxyDataSource` veremos como se hace uso de un proxy que invoca a la clase `TenantAwareInvocationHandler` la cual, al invocar al método '**close**' recupera el numero de conexiones en el pool Hikari (el que tiene por defecto Spring Boot) y la muestra en el log.



Si ejecutamos el siguiente código en una consola de **bash** podremos ver como el numero de conexiones es mostrado:

```bash
bash -c "for i in {1..5}; do   curl -s http://localhost:8080  & 
sleep 1
done " >>  /dev/null
```

En la salida de nuestro programa veremos lo siguiente

```
2024-01-01T21:24:42.295+01:00  INFO 19604 --- [nio-8080-exec-2] c.p.p.configuration.ProxyDataSource      : Create connection
2024-01-01T21:24:43.343+01:00  INFO 19604 --- [nio-8080-exec-9] c.p.p.configuration.ProxyDataSource      : Create connection
2024-01-01T21:24:44.404+01:00  INFO 19604 --- [nio-8080-exec-1] c.p.p.configuration.ProxyDataSource      : Create connection
2024-01-01T21:24:45.464+01:00  INFO 19604 --- [nio-8080-exec-6] c.p.p.configuration.ProxyDataSource      : Create connection
2024-01-01T21:24:46.307+01:00  INFO 19604 --- [nio-8080-exec-2] c.p.p.configuration.ProxyDataSource      : Closing connections. Active connections: 4
2024-01-01T21:24:46.525+01:00  INFO 19604 --- [io-8080-exec-10] c.p.p.configuration.ProxyDataSource      : Create connection
2024-01-01T21:24:47.352+01:00  INFO 19604 --- [nio-8080-exec-9] c.p.p.configuration.ProxyDataSource      : Closing connections. Active connections: 4
2024-01-01T21:24:48.408+01:00  INFO 19604 --- [nio-8080-exec-1] c.p.p.configuration.ProxyDataSource      : Closing connections. Active connections: 3
2024-01-01T21:24:49.469+01:00  INFO 19604 --- [nio-8080-exec-6] c.p.p.configuration.ProxyDataSource      : Closing connections. Active connections: 2
2024-01-01T21:24:50.543+01:00  INFO 19604 --- [io-8080-exec-10] c.p.p.configuration.ProxyDataSource      : Closing connections. Active connections: 1
```



Con lo cual queda demostrado que mostramos el número de conexiones activas y cuando se crea una conexión.



Espero que os haya picado la curiosidad y que os haya gustado este articulo.

Saludos, el profe.



