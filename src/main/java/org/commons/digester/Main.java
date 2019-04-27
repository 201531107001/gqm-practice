package org.commons.digester;

import java.io.IOException;

import org.apache.commons.digester3.Digester;
import org.commons.digester.pojo.Bar;
import org.commons.digester.pojo.Foo;
import org.xml.sax.SAXException;

public class Main {
    public static void main(String[] args) {

        try {
            // 1、创建Digester对象实例
            Digester digester = new Digester();

            // 2、配置属性值
            digester.setValidating(false);

            // 3、push对象到对象栈
            // digester.push(new Foo());

            // 4、设置匹配模式、规则
            digester.addObjectCreate("foo", "org.commons.digester.pojo.Foo");
            digester.addSetProperties("foo");
            digester.addObjectCreate("foo/bar", "org.commons.digester.pojo.Bar");
            digester.addSetProperties("foo/bar");
            digester.addSetNext("foo/bar", "addBar", "org.commons.digester.pojo.Bar");

            // 5、开始解析
            Foo foo = digester.parse(Main.class.getClassLoader().getResourceAsStream("org/commons/digester/example.xml"));

            // 6、打印解析结果
            System.out.println(foo.getName());
            for (Bar bar : foo.getBarList()) {
                System.out.println(bar.getId() + "," + bar.getTitle());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}
