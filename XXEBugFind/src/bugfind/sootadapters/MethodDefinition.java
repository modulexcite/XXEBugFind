/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bugfind.sootadapters;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

/**
 *
 * @author Mikosh
 */
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class MethodDefinition {
    @XmlElement (name = "ClassName")
    private String className;
    
    @XmlElement (name = "MethodName")
    private String methodName;    
    
    @XmlElement (name = "MethodParameter")
    private List<MethodParameter> parameterList;
    
    @XmlElement (name = "ReturnType")
    private String returnType;

    public MethodDefinition() {}

    public MethodDefinition(String className, String methodName, List<MethodParameter> parameterList, String returnType) {
        this.className = className;
        this.methodName = methodName;
        this.parameterList = (parameterList == null) ? new ArrayList<MethodParameter>() : parameterList;
        this.returnType = returnType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<MethodParameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<MethodParameter> parameterList) {
        this.parameterList = parameterList;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    
    
    
    
    
    
    
    @XmlRootElement
    @XmlAccessorType (XmlAccessType.FIELD)
    public static class MethodParameter {
        @XmlElement (name = "ParameterType")
        String type;
        
        @XmlElement (name = "ParameterName")
        String name;

        public MethodParameter() {
        }

        public MethodParameter(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    
    public static MethodDefinition getMethodDefinition(String methodSignature) throws Exception {        
        if (methodSignature == null || methodSignature.trim().isEmpty()) {
            throw new Exception("The method signuature given cannot be null or empty. It should have the format "
                    + "ClassName.methodName(ParameterType1, ParameterType2) "
                    + "e.g. org.ucl.MyClass.amethod(java.lang.String, int, org.rmx.Options)" );            
        }
        if (!methodSignature.contains(".") || !methodSignature.contains("(") || !methodSignature.contains(")")) {
            throw new Exception("Invalid method signature given. It should have the format "
                    + "ClassName.methodName(ParameterType1, ParameterType2) "
                    + "e.g. org.ucl.MyClass.amethod(java.lang.String, int, org.rmx.OptionsList[])" );            
        }
        
        int lastDotIndexBeforeParenthesis = methodSignature.substring(0, methodSignature.indexOf("(")).lastIndexOf(".");
        int firstIndexOfParen = methodSignature.indexOf("(");
        String classname = methodSignature.substring(0, lastDotIndexBeforeParenthesis);
        String methodName = methodSignature.substring(lastDotIndexBeforeParenthesis + 1, firstIndexOfParen);
        String argumentStr =  methodSignature.substring(firstIndexOfParen + 1, methodSignature.lastIndexOf(")")).trim();
        
        if (argumentStr.length() == 0) {
            return new MethodDefinition(classname, methodName, null, null);
        }
        List<MethodParameter> parameterList = new ArrayList<>();
        String[] parameters = argumentStr.split(",");
        for (String parameter : parameters) {
            if (parameter.trim().contains(" ")) { // use a more wider range to cacth traps
                throw new Exception("Invalid method parameter '" + parameter + "' given" );           
            }
            else {
                parameterList.add(new MethodParameter(parameter.trim(), null));
            }
        }
                    
        return new MethodDefinition(classname, methodName, parameterList, null);
    }
    
    public static SootMethod getSootMethod(SootClass sc, MethodDefinition md) {
        List<SootMethod> list = new SootClassWrapper(sc).getAllMethodsDeclaredIncludingInherited();//sc.getMethods();
        Scene.v().getSootClass("org.jdom2.input.SAXBuilder").getMethods();
        for (SootMethod meth : list) { 
            if (meth.getName().equals(md.getMethodName())) {
                if (meth.getParameterCount() == md.getParameterList().size()) {       
                    if (meth.getParameterCount() == 0) { // special case of a method with no arguments
                        return meth;
                    }
                    
                    // method has parameters
                    List<Type> parameterTypeList = meth.getParameterTypes();
                    List<MethodParameter> methParameterList = md.getParameterList();
                    boolean allThesame = true;
                    for (int i=0; i<meth.getParameterCount(); ++i) {
                        if (!parameterTypeList.get(i).toString().equals(methParameterList.get(i).getType())) {
                            allThesame = false;
                            break;//return meth;
                        }
                    }
                    if (allThesame) {
                        return meth;
                    }
                }
            }
        }
        
        //SootClass sc1 = Scene.v().loadClassAndSupport("org.apache.xerces.parsers.SAXParser");//Scene.v().getSootClass("org.​apache.​xerces.​parsers.SAXParser"); 
        //sc1.setApplicationClass();
        
     //  sc.getMethodByName("parse")
        // code must not reach here// reaching here means no method of the md signature exists
        throw new RuntimeException("Couldn't find method signature " + md + " in " + sc);
    }

    public static SootMethod getOverrideSootMethod(SootClass sc, SootMethod baseMethod) {
        List<SootMethod> list = new SootClassWrapper(sc).getAllMethodsDeclaredIncludingInherited();//sc.getMethods();
        for (SootMethod meth : list) { 
            if (meth.getName().equals(baseMethod.getName())) {
                if (meth.getParameterCount() == baseMethod.getParameterCount()) {       
                    if (meth.getParameterCount() == 0) { // special case of a method with no arguments
                        return meth;
                    }
                    
                    // method has parameters
                    List<Type> parameterTypeList = meth.getParameterTypes();
                    List<Type> parameterTypeListParent = baseMethod.getParameterTypes();
                    //List<MethodParameter> methParameterList = md.getParameterList();
                    boolean allThesame = true;
                    for (int i=0; i<meth.getParameterCount(); ++i) {
                        if (!parameterTypeList.get(i).toString().equals(parameterTypeListParent.get(i).toString())) {
                            allThesame = false;
                            break;//return meth;
                        }
                    }
                    if (allThesame) {
                        return meth;
                    }
                }
            }
        }
        
        //SootClass sc1 = Scene.v().loadClassAndSupport("org.apache.xerces.parsers.SAXParser");//Scene.v().getSootClass("org.​apache.​xerces.​parsers.SAXParser"); 
        //sc1.setApplicationClass();
        
     //  sc.getMethodByName("parse")
        // code must not reach here// reaching here means no method of the md signature exists
        throw new RuntimeException("Couldn't find method that override " + baseMethod + " in " + sc);
    }
    
    @Override
    public String toString() {
        StringBuilder sb  = new StringBuilder(getClassName());
        sb.append(".").append(getMethodName()).append("(");
        for (MethodParameter mp: getParameterList()) {
            sb.append(mp.getType()).append(", ");
        }
        if (sb.toString().endsWith(", ")) {
            sb.replace(sb.lastIndexOf(", "), sb.length(), "");
        }
        sb.append(");");
        
        return sb.toString();
    }
    
    
}