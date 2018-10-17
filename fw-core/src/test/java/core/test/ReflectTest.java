package core.test;

import core.test.demo.web.UserController;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.lang.reflect.Method;

public class ReflectTest {
    public static void main(String[] args) throws Exception {
        //jdk 原生枚举没法获取参数名称
        Method method = UserController.class.getDeclaredMethod("register", new Class[]{String.class, String.class});
        System.out.println(method.getParameters()[0].getName());
        ClassPool pool = ClassPool.getDefault();
        CtClass ct = pool.get(UserController.class.getName());
        CtMethod ctMethod = ct.getDeclaredMethod("register", new CtClass[]{pool.get(String.class.getName()), pool.get(String.class.getName())});
        MethodInfo methodInfo = ctMethod.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        for (int i = 0; i < ctMethod.getParameterTypes().length; i++) {
            String name = attribute.variableName(i+1);
            System.out.println(name);
        }

    }
}
