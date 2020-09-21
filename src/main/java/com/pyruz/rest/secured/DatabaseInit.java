package com.pyruz.rest.secured;

import com.pyruz.rest.secured.model.entity.Api;
import com.pyruz.rest.secured.model.enums.HttpMethods;
import com.pyruz.rest.secured.repository.ApiRepository;
import com.pyruz.rest.secured.repository.UserRepository;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class DatabaseInit implements ApplicationRunner, ApplicationContextAware {

    static List<Api> apis = new ArrayList<>();
    final Pattern pattern = Pattern.compile("^[$][{].*[}]$");


    final UserRepository userRepository;
    final ApiRepository apiRepository;

    public DatabaseInit(UserRepository userRepository, ApiRepository apiRepository) {
        this.userRepository = userRepository;
        this.apiRepository = apiRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        apiRepository.saveAll(apis);

/*
        Access access = Access.builder()
                .apis()
                .build();

        User user = User.builder()
                .firstName("Pyruz")
                .lastName("Janbaaz")
                .email("Pyruz.Janbaaz@gmail.com")
                .accesses()
                .build();
*/

    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> restControllers = applicationContext.getBeansWithAnnotation(RestController.class);
        for (String key : restControllers.keySet()) {
            String fullPackageName = restControllers.get(key).toString();
            String classPath = fullPackageName.substring(0, fullPackageName.indexOf('@'));
            Method[] methods = new Method[0];
            Class<?> restController;
            try {
                restController = Class.forName(classPath);
                methods = restController.getMethods();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
            for (Method method : methods) {
                String name = "";
                String role = "";
                String value = "";
                HttpMethods httpMethod;


                if (method.isAnnotationPresent(PostMapping.class)) {
                    name = method.getAnnotation(PostMapping.class).name();
                    value = method.getAnnotation(PostMapping.class).value().length > 0 ? method.getAnnotation(PostMapping.class).value()[0] : "";
                    httpMethod = HttpMethods.POST;
                } else if (method.isAnnotationPresent(GetMapping.class)) {
                    name = method.getAnnotation(GetMapping.class).name();
                    value = method.getAnnotation(GetMapping.class).value().length > 0 ? method.getAnnotation(GetMapping.class).value()[0] : "";
                    httpMethod = HttpMethods.GET;
                } else if (method.isAnnotationPresent(PutMapping.class)) {
                    name = method.getAnnotation(PutMapping.class).name();
                    value = method.getAnnotation(PutMapping.class).value().length > 0 ? method.getAnnotation(PutMapping.class).value()[0] : "";
                    httpMethod = HttpMethods.PUT;
                } else if (method.isAnnotationPresent(DeleteMapping.class)) {
                    name = method.getAnnotation(DeleteMapping.class).name();
                    value = method.getAnnotation(DeleteMapping.class).value().length > 0 ? method.getAnnotation(DeleteMapping.class).value()[0] : "";
                    httpMethod = HttpMethods.DELETE;
                } else if (method.isAnnotationPresent(PatchMapping.class)) {
                    name = method.getAnnotation(PatchMapping.class).name();
                    value = method.getAnnotation(PatchMapping.class).value().length > 0 ? method.getAnnotation(PatchMapping.class).value()[0] : "";
                    httpMethod = HttpMethods.PATCH;
                } else if (method.isAnnotationPresent(RequestMapping.class)) {
                    name = method.getAnnotation(RequestMapping.class).name();
                    value = method.getAnnotation(RequestMapping.class).value().length > 0 ? method.getAnnotation(RequestMapping.class).value()[0] : "";
                    RequestMethod requestMethod = method.getAnnotation(RequestMapping.class).method().length > 0 ? method.getAnnotation(RequestMapping.class).method()[0] : RequestMethod.GET;
                    httpMethod = HttpMethods.valueOf(requestMethod.toString().toUpperCase());
                } else continue;

                if (pattern.matcher(name).matches()) {
                    name = name.replaceFirst("^\\$\\{", "").replaceFirst("}$", "");
                }
                if (method.isAnnotationPresent(PreAuthorize.class)) {
                    role = method.getAnnotation(PreAuthorize.class).value();
                    int from = role.indexOf("'");
                    int to = role.lastIndexOf("'");
                    role = role.substring(from, to);
                }

                name = StringUtils.isEmpty(name) ? "-" : name;
                String url = "/" + value.replaceFirst("^/", "");
                apis.add(
                        Api.builder()
                                .method(httpMethod.toString())
                                .role(role)
                                .url(url)
                                .title(name)
                                .build()
                );
            }
            apis.forEach(item -> System.out.println(item.getUrl() + ' ' + item.getTitle()));

        }
    }
}