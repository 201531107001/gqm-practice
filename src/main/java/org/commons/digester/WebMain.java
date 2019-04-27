package org.commons.digester;

import java.io.IOException;

import org.apache.commons.digester3.Digester;
import org.commons.digester.pojo.ServletBean;
import org.xml.sax.SAXException;

public class WebMain {
    public static void main(String[] args) {
        try {
            // 1、创建Digester对象实例
            Digester digester = new Digester();

            // 2、配置属性值
            digester.setValidating(false);

            // 3、push对象到对象栈

            // 4、设置匹配模式、规则
            digester.addObjectCreate("web-app/servlet", "org.commons.digester.pojo.ServletBean");
            digester.addCallMethod("web-app/servlet/servlet-name", "setServletName", 0);
            digester.addCallMethod("web-app/servlet/servlet-class", "setServletClass", 0);
            digester.addCallMethod("web-app/servlet/init-param", "addInitParam", 2);
            digester.addCallParam("web-app/servlet/init-param/param-name", 0);
            digester.addCallParam("web-app/servlet/init-param/param-value", 1);

            // 5、开始解析
            ServletBean servletBean = digester.parse(Main.class.getClassLoader().getResourceAsStream("org/commons/digester/web.xml"));

            // 6、打印解析结果
            System.out.println(servletBean.getServletName());
            System.out.println(servletBean.getServletClass());
            for (String key : servletBean.getInitParams().keySet()) {
                System.out.println(key + ": " + servletBean.getInitParams().get(key));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}
