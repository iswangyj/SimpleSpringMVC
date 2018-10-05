package com.test.servlet;

import com.test.annotation.SimpleController;
import com.test.annotation.SimpleRequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author wyj
 * @date 2018/9/14
 */
public class SimpleDispatcherServlet extends HttpServlet {
    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<String>();

    private Map<String, Object> ioc = new HashMap<String, Object>();

    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    private Map<String, Object> controllerMap = new HashMap<String, Object>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 2.初始化所有相关联的类，扫描设定的包内所有的类
        doScanner(properties.getProperty("scanPackage"));

        // 3.拿到扫描到的类，通过反射机制实例化，并且放到ioc容器中去（key-value beanName-bean,其中beanName首字母默认小写）
        doInstance();

        // 4.初始化HandlerMapping（将url和method相对应）
        initHandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            //处理请求
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }

    }

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (handlerMapping.isEmpty()) {
            return;
        }

        String url = request.getRequestURI();
        String contextPath = request.getContextPath();

        url = url.replace(contextPath, "").replaceAll("/+", "/");

        if (!this.handlerMapping.containsKey(url)) {
            response.getWriter().write("HttpStatus 404, NOT FOUND!");
            return;
        }

        Method method = this.handlerMapping.get(url);

        // 获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();

        // 获取请求参数
        Map<String, String[]> parameterMap = request.getParameterMap();

        // 保存参数值
        Object[] paramValues = new Object[parameterTypes.length];

        // 方法的参数列表
        for (int i = 0; i < parameterTypes.length; i++) {
            // 根据参数名称做某些处理
            String requestParam = parameterTypes[i].getSimpleName();

            if (requestParam.equals("HttpServletRequest")) {
                // 确定参数类型，强转
                paramValues[i] = request;
                continue;
            }

            if (requestParam.equals("HttpServletResponse")) {
                paramValues[i] = response;
                continue;
            }

            if (requestParam.equals("String")) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue())
                            .replaceAll("\\[|\\]", "")
                            .replaceAll(",\\s", ",");
                    paramValues[i] = value;
                }
            }

            // 利用反射机制调用
            try {
                // 第一个参数是method所对应的实例，在ioc容器中
                method.invoke(this.controllerMap.get(url), paramValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void doLoadConfig(String location) {
        // web.xml文件中的contextConfigLoacation对应的value值加载到流
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);

        try {
            // 加载（.properties）文件内容
            properties.load(resourceAsStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String packageName) {
        // 将"."替换成"/"
        URL url = this.getClass().getResource("/"+packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                // 递归读取包
                doScanner(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    public void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }

        for (String className : classNames) {
            try {
                // 类的反射实例化（只有@SimpleController需要实例化）
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(SimpleController.class)) {
                    ioc.put(toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Class<? extends Object> clazz = entry.getValue().getClass();
                if (!clazz.isAnnotationPresent(SimpleController.class)) {
                    continue;
                }
                // 拼接url
                String baseUrl = "";
                if (clazz.isAnnotationPresent(SimpleRequestMapping.class)) {
                    SimpleRequestMapping annotation = clazz.getAnnotation(SimpleRequestMapping.class);
                    baseUrl = annotation.value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(SimpleRequestMapping.class)) {
                        continue;
                    }
                    SimpleRequestMapping annotation = method.getAnnotation(SimpleRequestMapping.class);
                    String url = annotation.value();

                    url = (baseUrl + "/" + url).replaceAll("/+", "/");
                    handlerMapping.put(url, method);
                    controllerMap.put(url, clazz.newInstance());
                    System.out.println(url + "," + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 字符串首字母小写
     *
     * @param name
     * @return
     */
    private String toLowerFirstWord(String name) {
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }
}
