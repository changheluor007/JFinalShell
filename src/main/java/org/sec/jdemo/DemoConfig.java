package org.sec.jdemo;

import com.jfinal.config.*;
import com.jfinal.core.ActionHandler;
import com.jfinal.core.ActionMapping;
import com.jfinal.template.Engine;

public class DemoConfig extends JFinalConfig {
    @Override
    public void configConstant(Constants me) {

    }

    @Override
    public void configRoute(Routes me) {
        me.add("/hello", HelloController.class);
    }

    @Override
    public void configEngine(Engine me) {

    }

    @Override
    public void configPlugin(Plugins me) {

    }

    @Override
    public void configInterceptor(Interceptors me) {

    }

    @Override
    public void configHandler(Handlers me) {

    }
}
