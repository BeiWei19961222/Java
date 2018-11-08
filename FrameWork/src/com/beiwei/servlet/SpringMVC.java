package com.beiwei.servlet;

import com.beiwei.annotation.MyController;
import com.beiwei.annotation.MyRequestMapping;
import com.beiwei.annotation.MyRequestParameter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class SpringMVC extends javax.servlet.http.HttpServlet {
    private Properties properties = new Properties(); // 1.存放配置文件信息,扫描包路径;
    private List<String> className = new ArrayList<String>(); // 2.存放Controller类的完全限定名称;  com.beiwei.action.GameAction
    private Map<String, Object> ioc = new HashMap<String, Object>(); // 3.存放对象托管.@MyController注解标签;<beanName,bean>  -- scope 作用域操作的对象;
    private Map<String, Object> controllerMapping = new HashMap<String, Object>(); // 4.存放<URL,class对象>
    private Map<String, Method> handleMapping = new HashMap<String, Method>(); // 5.存放<URL,method方法>   4.5的url都指的是方法的访问路径!

    /**
     * 总结:
     * 1.springmvc的玩法:通过servlet进行转发的,是将对Controller请求特殊化,比如'.do'后缀被servlet拦截,进行转发.而静态资源不会请求到.
     * 2.struts2的玩法:通过过滤器拦截所有的请求,通过配置文件struts2-application-config来配置文件请求的url.struts2的是把所有请求都拦截掉,然后
     * 如果是controller,就走handle流程,静态资源外部请求直接通过配置路径进行拦截器放行.
     * 3.request.getRequestDispatcher().forward方法是内部转发,不会经过拦截器,但是会被服务器配置的web.xml中的servlet拦截下来,也就是说它走了服务器的
     * 内部'web.xml'拦截.
     *
     * Springmvc总结
     * 整个MVC流程是:用户发起请求,经过服务器的拦截器,到DispatcherServlet控制器,获取到HandleMapping中的controller层,调用相应的方法,然后返回视图层url,在调用对应的
     * 视图层,经过拦截器,反馈给用户;
     *
     * springmvc整个工作机制:
     *   在服务器启动时加载DispatcherServlet,初始化步骤中完成: 1.读取配置文件,获取扫描包路径; 2.通过包路径,获取到当前包下面所有文件的完全限定名(类加载器+迭代),
     * 3.通过完全限定名,获取class对象,进行controller筛选,保存好当前beanName和实例;(反射) 4.通过实例反射到类,获取到当前类的RequestMapping请求路径Url,保存好<url,类>. 5.获取到类
     * 中所有方法的访问路径url,然后拼接好当前访问方法的url.<url,method>.
     *   用户所有请求带特定的后缀,比如'.do',经过当前servlet,根据URI定位到handleMapping中的Method,调用Method,获取view视图层的url,然后转发,返回给用户;
     *
     * @param config
     * @throws ServletException
     */

    @Override  // servletConfig表示当前servlet的配置信息,而servletContext就是applicatoin,它包含了所有的servlet全局信息;
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // 1.加载配置文件,获取扫描路径;
        doLoadConfig(config.getInitParameter("ContextConfigLocation"));
        // 2.将扫描路径下的所有Controller,将类的完全限定名保存下来;
        doScanner(properties.getProperty("scanPackage"));
        // 3.将controller类按<beanName,bean>保存下来,其中beanName首字母小写;
        doInstance();
        // 4.将托管对象中的url与method的相对应,此操作将<url,method>放入到mapping中,url为class url+method url;
        initHandleMapping();

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        doPost(request, response);
    }

    // 进行servlet转发;DispatchServlet;
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        doDispatch(request, response);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private void doLoadConfig(String location) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 总结:File是根据system.getProperties("user.dir")决定的.相对路径跟用户工作目录相关;
    private void doScanner(String packgeName) {
        //通过路径来扫描到当前包;包下面进行文件扫描,获取到最底层;

        // 1.替换掉'.'为'/';
        if (!packgeName.endsWith("class")) {
            packgeName = packgeName.replaceAll("\\.", "/");
        }
        // 2.通过类加载器获取文件资源定位;
        URL resource = this.getClass().getClassLoader().getResource(packgeName);
        // 3.创建文件;
        File file = new File(resource.getFile());

        // 递归扫描
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File obj : files) {
                // 传递一个 '相对路径'+'/'+'文件名or目录名';
                doScanner(packgeName + "/" + obj.getName());
            }
        } else {
            className.add(packgeName.replaceAll("/", ".").replace(".class", ""));
        }
    }


    // 总结: char可以加一个string,是因为new StringBuilder.append(char).append(string).toString();
    private void doInstance() {
        /**
         * 1.获取list中所有类的完全限定名;
         * 2.通过class反射机制获取到当前类的实例;
         * 3.获取当前类的注解,如果包含'@MyController'注解,则将当前以<beanName,bean>保存到Map中;(beanName首字母小写)
         */

        try {
            for (String className : className) {
                // 获取当前类;
                Class<?> clazz = Class.forName(className);
                Annotation annotation = clazz.getAnnotation(MyController.class);
                if (annotation != null) {
                    // 获取当前类实例,并且将名称保存下来; ioc
                    Object instance = clazz.newInstance();
                    // 获取当前类名;(类名)
                    String name = clazz.getSimpleName();
                    // 首字母小写;
                    char firstWord = name.toLowerCase().charAt(0);
                    name = String.valueOf(firstWord) + name.substring(1);
                    // 存放到ioc中;
                    ioc.put(name, instance);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void initHandleMapping() {
        /**
         * 1.将ioc中的实例获取到class类.
         * 2.通过类获取到类注解@MyRequestMapping,获取到当前类请求的相对路径.
         * 3.将<类路径url,类>实例保存到controller中.
         * 4.通过反射获取当前类下的所有方法,获取每个方法上的注解@myRequestMapping.
         * 5.将<class url + method url,method>保存到handleMapping中;
         *
         * 问题:
         *    如果名称相同,注意可能会出现覆盖现象;(class请求路径/method请求路径)
         */
        Set<String> set = ioc.keySet();
        for (String key : set) {
            Object clazz = ioc.get(key);
            MyRequestMapping annotation = clazz.getClass().getAnnotation(MyRequestMapping.class);
            String classPath = null;
            String methodPath = null;
            if (annotation != null) {
                classPath = annotation.value();
            }
            Method[] methods = clazz.getClass().getMethods();
            for (Method method : methods) {
                MyRequestMapping mapping = method.getAnnotation(MyRequestMapping.class);
                // 只有类方法有@MyRequestMapping标签的类方法才会放到handleMapping中;
                if (mapping != null) {
                    methodPath = mapping.value();
                    // 将当前方法路径保存下来;(支持多个'/'),这样兼容性比较好;
                    String url = (classPath + "/" + methodPath).replaceAll("/+", "/"); // 将当前类路径保存下来;
                    controllerMapping.put(url, clazz);
                    handleMapping.put(url, method);
                   // System.out.println(url + "," + method);
                }
            }
        }

    }


    // 进行转发
    private void doDispatch(HttpServletRequest request, HttpServletResponse response){
        /**
         * 1.路径:获取到当前请求的URL,以及前缀ContextPath;(通过request来获取);
         * 2.根据路径在handleMapping中找到对应的method;
         * 3.参数: (1).请求参数A; (2).方法参数B;  (3).传递给方法的参数C;
         * 4.根据方法参数,跟请求参数一一匹配,存放到C中;
         * 5.从controllerMapping中,调用方法;Method.invoke(obj实例,参数数组);
         *
         * URI:指的'相对路径',相对资源地位,不包括ip,端口号,只有请求项目路径; '/test/ts'
         * URL:指的'绝对路径',访问的全地址; 'https:localhost:8080/test/ts'
         * contextPath:表示当前项目的路径; '/test'
         *
         * request.getRequestDispatcher().forward("","");
         * 内部转发,虽然不会经过拦截器,但它还是会再次请求服务器,根据web.xml配置文件进行访问.
         */

        if(handleMapping.isEmpty()){
            return;  // 如果扫描为空,则要么Init初始化servlet出问题,要么就是项目本身就没有访问路径;
        }
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = requestURI.replace(contextPath,"").replaceAll("/+","/").replace(".do","");
        // 如果映射路径handleMapping中不包含此路径,则直接报错;
        if(!this.handleMapping.containsKey(url)){
            PrintWriter writer = null;
            try {
                writer = response.getWriter();
                writer.write("404 not found!");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 获取当前url在handleMapping中对应的method;
        Method method = handleMapping.get(url);
        // 进行参数读取;
        // A参数:请求参数;
        Map<String, String[]> parameterMap = request.getParameterMap();
        // B参数:Method形参;(如果没有,则长度为0的数组)
        Class<?>[] parameterTypes = method.getParameterTypes();
        // C参数:存放传递给方法的参数;
        Object[] objects = new Object[parameterTypes.length];

        // B中参数有4种类型: 1.request. 2.response. 3.String. 4.自定义对象.  其中3,4存在有注解@MyRequestParameter;
        for(int i=0;i<parameterTypes.length;i++){
            // 获取当前参数的简单名称
            String name = parameterTypes[i].getSimpleName();
            // 进行参数转换;
            if(name.equals("HttpServletRequest")){
                objects[i] = request;
            }else if(name.equals("HttpServletResponse")){
                objects[i] = response;
            }else if(name.equals("String")){
                // 1.前台可能传递不止一个值,有很多name,则前台在消息体里面拼接成数组;
                // 2.java自带的反射无法获取到方法的参数名称,则无法一一对应放入.
                MyRequestParameter annotation = method.getAnnotation(MyRequestParameter.class);
            }else{
                // 对bean操作;
                try {
                    // class对象,获取当前对象中的域;
                    Field[] fields = parameterTypes[i].getFields();
                    for(Field field:fields){
                        System.out.println(field.getName());
                    }
                    Object o = parameterTypes[i].newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Object result = method.invoke(controllerMapping.get(url), objects);
            request.getRequestDispatcher(result.toString()).forward(request,response);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
