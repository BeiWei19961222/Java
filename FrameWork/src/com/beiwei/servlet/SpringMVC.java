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
    private Properties properties = new Properties(); // 1.��������ļ���Ϣ,ɨ���·��;
    private List<String> className = new ArrayList<String>(); // 2.���Controller�����ȫ�޶�����;  com.beiwei.action.GameAction
    private Map<String, Object> ioc = new HashMap<String, Object>(); // 3.��Ŷ����й�.@MyControllerע���ǩ;<beanName,bean>  -- scope ����������Ķ���;
    private Map<String, Object> controllerMapping = new HashMap<String, Object>(); // 4.���<URL,class����>
    private Map<String, Method> handleMapping = new HashMap<String, Method>(); // 5.���<URL,method����>   4.5��url��ָ���Ƿ����ķ���·��!

    /**
     * �ܽ�:
     * 1.springmvc���淨:ͨ��servlet����ת����,�ǽ���Controller�������⻯,����'.do'��׺��servlet����,����ת��.����̬��Դ��������.
     * 2.struts2���淨:ͨ���������������е�����,ͨ�������ļ�struts2-application-config�������ļ������url.struts2���ǰ������������ص�,Ȼ��
     * �����controller,����handle����,��̬��Դ�ⲿ����ֱ��ͨ������·����������������.
     * 3.request.getRequestDispatcher().forward�������ڲ�ת��,���ᾭ��������,���ǻᱻ���������õ�web.xml�е�servlet��������,Ҳ����˵�����˷�������
     * �ڲ�'web.xml'����.
     *
     * Springmvc�ܽ�
     * ����MVC������:�û���������,������������������,��DispatcherServlet������,��ȡ��HandleMapping�е�controller��,������Ӧ�ķ���,Ȼ�󷵻���ͼ��url,�ڵ��ö�Ӧ��
     * ��ͼ��,����������,�������û�;
     *
     * springmvc������������:
     *   �ڷ���������ʱ����DispatcherServlet,��ʼ�����������: 1.��ȡ�����ļ�,��ȡɨ���·��; 2.ͨ����·��,��ȡ����ǰ�����������ļ�����ȫ�޶���(�������+����),
     * 3.ͨ����ȫ�޶���,��ȡclass����,����controllerɸѡ,����õ�ǰbeanName��ʵ��;(����) 4.ͨ��ʵ�����䵽��,��ȡ����ǰ���RequestMapping����·��Url,�����<url,��>. 5.��ȡ����
     * �����з����ķ���·��url,Ȼ��ƴ�Ӻõ�ǰ���ʷ�����url.<url,method>.
     *   �û�����������ض��ĺ�׺,����'.do',������ǰservlet,����URI��λ��handleMapping�е�Method,����Method,��ȡview��ͼ���url,Ȼ��ת��,���ظ��û�;
     *
     * @param config
     * @throws ServletException
     */

    @Override  // servletConfig��ʾ��ǰservlet��������Ϣ,��servletContext����applicatoin,�����������е�servletȫ����Ϣ;
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // 1.���������ļ�,��ȡɨ��·��;
        doLoadConfig(config.getInitParameter("ContextConfigLocation"));
        // 2.��ɨ��·���µ�����Controller,�������ȫ�޶�����������;
        doScanner(properties.getProperty("scanPackage"));
        // 3.��controller�ఴ<beanName,bean>��������,����beanName����ĸСд;
        doInstance();
        // 4.���йܶ����е�url��method�����Ӧ,�˲�����<url,method>���뵽mapping��,urlΪclass url+method url;
        initHandleMapping();

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        doPost(request, response);
    }

    // ����servletת��;DispatchServlet;
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

    // �ܽ�:File�Ǹ���system.getProperties("user.dir")������.���·�����û�����Ŀ¼���;
    private void doScanner(String packgeName) {
        //ͨ��·����ɨ�赽��ǰ��;����������ļ�ɨ��,��ȡ����ײ�;

        // 1.�滻��'.'Ϊ'/';
        if (!packgeName.endsWith("class")) {
            packgeName = packgeName.replaceAll("\\.", "/");
        }
        // 2.ͨ�����������ȡ�ļ���Դ��λ;
        URL resource = this.getClass().getClassLoader().getResource(packgeName);
        // 3.�����ļ�;
        File file = new File(resource.getFile());

        // �ݹ�ɨ��
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File obj : files) {
                // ����һ�� '���·��'+'/'+'�ļ���orĿ¼��';
                doScanner(packgeName + "/" + obj.getName());
            }
        } else {
            className.add(packgeName.replaceAll("/", ".").replace(".class", ""));
        }
    }


    // �ܽ�: char���Լ�һ��string,����Ϊnew StringBuilder.append(char).append(string).toString();
    private void doInstance() {
        /**
         * 1.��ȡlist�����������ȫ�޶���;
         * 2.ͨ��class������ƻ�ȡ����ǰ���ʵ��;
         * 3.��ȡ��ǰ���ע��,�������'@MyController'ע��,�򽫵�ǰ��<beanName,bean>���浽Map��;(beanName����ĸСд)
         */

        try {
            for (String className : className) {
                // ��ȡ��ǰ��;
                Class<?> clazz = Class.forName(className);
                Annotation annotation = clazz.getAnnotation(MyController.class);
                if (annotation != null) {
                    // ��ȡ��ǰ��ʵ��,���ҽ����Ʊ�������; ioc
                    Object instance = clazz.newInstance();
                    // ��ȡ��ǰ����;(����)
                    String name = clazz.getSimpleName();
                    // ����ĸСд;
                    char firstWord = name.toLowerCase().charAt(0);
                    name = String.valueOf(firstWord) + name.substring(1);
                    // ��ŵ�ioc��;
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
         * 1.��ioc�е�ʵ����ȡ��class��.
         * 2.ͨ�����ȡ����ע��@MyRequestMapping,��ȡ����ǰ����������·��.
         * 3.��<��·��url,��>ʵ�����浽controller��.
         * 4.ͨ�������ȡ��ǰ���µ����з���,��ȡÿ�������ϵ�ע��@myRequestMapping.
         * 5.��<class url + method url,method>���浽handleMapping��;
         *
         * ����:
         *    ���������ͬ,ע����ܻ���ָ�������;(class����·��/method����·��)
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
                // ֻ���෽����@MyRequestMapping��ǩ���෽���Ż�ŵ�handleMapping��;
                if (mapping != null) {
                    methodPath = mapping.value();
                    // ����ǰ����·����������;(֧�ֶ��'/'),���������ԱȽϺ�;
                    String url = (classPath + "/" + methodPath).replaceAll("/+", "/"); // ����ǰ��·����������;
                    controllerMapping.put(url, clazz);
                    handleMapping.put(url, method);
                   // System.out.println(url + "," + method);
                }
            }
        }

    }


    // ����ת��
    private void doDispatch(HttpServletRequest request, HttpServletResponse response){
        /**
         * 1.·��:��ȡ����ǰ�����URL,�Լ�ǰ׺ContextPath;(ͨ��request����ȡ);
         * 2.����·����handleMapping���ҵ���Ӧ��method;
         * 3.����: (1).�������A; (2).��������B;  (3).���ݸ������Ĳ���C;
         * 4.���ݷ�������,���������һһƥ��,��ŵ�C��;
         * 5.��controllerMapping��,���÷���;Method.invoke(objʵ��,��������);
         *
         * URI:ָ��'���·��',�����Դ��λ,������ip,�˿ں�,ֻ��������Ŀ·��; '/test/ts'
         * URL:ָ��'����·��',���ʵ�ȫ��ַ; 'https:localhost:8080/test/ts'
         * contextPath:��ʾ��ǰ��Ŀ��·��; '/test'
         *
         * request.getRequestDispatcher().forward("","");
         * �ڲ�ת��,��Ȼ���ᾭ��������,�������ǻ��ٴ����������,����web.xml�����ļ����з���.
         */

        if(handleMapping.isEmpty()){
            return;  // ���ɨ��Ϊ��,��ҪôInit��ʼ��servlet������,Ҫô������Ŀ�����û�з���·��;
        }
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = requestURI.replace(contextPath,"").replaceAll("/+","/").replace(".do","");
        // ���ӳ��·��handleMapping�в�������·��,��ֱ�ӱ���;
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
        // ��ȡ��ǰurl��handleMapping�ж�Ӧ��method;
        Method method = handleMapping.get(url);
        // ���в�����ȡ;
        // A����:�������;
        Map<String, String[]> parameterMap = request.getParameterMap();
        // B����:Method�β�;(���û��,�򳤶�Ϊ0������)
        Class<?>[] parameterTypes = method.getParameterTypes();
        // C����:��Ŵ��ݸ������Ĳ���;
        Object[] objects = new Object[parameterTypes.length];

        // B�в�����4������: 1.request. 2.response. 3.String. 4.�Զ������.  ����3,4������ע��@MyRequestParameter;
        for(int i=0;i<parameterTypes.length;i++){
            // ��ȡ��ǰ�����ļ�����
            String name = parameterTypes[i].getSimpleName();
            // ���в���ת��;
            if(name.equals("HttpServletRequest")){
                objects[i] = request;
            }else if(name.equals("HttpServletResponse")){
                objects[i] = response;
            }else if(name.equals("String")){
                // 1.ǰ̨���ܴ��ݲ�ֹһ��ֵ,�кܶ�name,��ǰ̨����Ϣ������ƴ�ӳ�����;
                // 2.java�Դ��ķ����޷���ȡ�������Ĳ�������,���޷�һһ��Ӧ����.
                MyRequestParameter annotation = method.getAnnotation(MyRequestParameter.class);
            }else{
                // ��bean����;
                try {
                    // class����,��ȡ��ǰ�����е���;
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
