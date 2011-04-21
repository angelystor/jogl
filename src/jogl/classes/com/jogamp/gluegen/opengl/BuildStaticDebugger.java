/**
 * Copyright 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
/**
 * Copyright 2011 Animoto Productions
 */
package com.jogamp.gluegen.opengl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.gluegen.JavaType;

//import com.animoto.opengl.GL4Static;
/**
 * @author angelystor
 *
 */
public class BuildStaticDebugger
{
    //1.4 friendly, 5 will be 1.5
    private enum SourceLevel { FOUR, FIVE };
    private final SourceLevel sourceLevel = SourceLevel.FOUR;
    
    private String outputDir;
    private String outputPackage;
    private String outputName;
    private Class<?> clazz;
    
    public BuildStaticDebugger(String outputDir, String outputPackage, String outputName, Class<?> clazz)
    {
        this.outputDir      = outputDir;
        this.outputPackage  = outputPackage;
        this.outputName     = outputName;
        this.clazz          = clazz;
    }
    
    private List<Method> getMethodsStartingWithGL()
    {
        List<Method> methods = new ArrayList<Method>();
        
        Method[] clazzMethods = clazz.getMethods();
        
        for (Method m : clazzMethods)
        {
            if (m.getName().startsWith("gl"))
            {
                methods.add(m);
            }
        }
        
        return methods;
    }
    
    private void emitImports(PrintWriter out)
    {
        out.println("import " + clazz.getName() + ";");
        //out.println("import java.util.logging.Logger;");
    }

    private void emitClassFields(PrintWriter out)
    {
        Field[] fields = clazz.getFields();
        
        for (Field f : fields)
        {
            emitAField(out, f);
            
            out.flush();
        }
    }
    
    private void emitAField(PrintWriter out, Field f)
    {
        out.print("\t" + Modifier.toString(f.getModifiers()));
        out.print(" ");
        out.print(JavaType.createForClass(f.getType()));
        out.print(" ");
        try
        {
            //TODO convert to hex if integer? or should we just extend the base class for these fields?
            //technically speaking we don't need to convert to hex, just to look nicer nia?
            out.println(f.getName() + "= " + f.get(null) +";");
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void emitMethods(PrintWriter out)
    {
        List<Method> methods = getMethodsStartingWithGL();
        
        for (Method m : methods)
        {
            emitAMethod(out, m);
            
            out.flush();
        }
    }
    

    private void emitAMethod(PrintWriter out, Method m)
    {
        emitMethodSignature(out, m);
        emitMethodBody(out, m);
    }
    
    private void emitMethodSignature(PrintWriter out, Method m)
    {
        //accessors/modifiers
        out.print("\t" + Modifier.toString(m.getModifiers()));
        out.print(" ");
        //returns
        out.print(JavaType.createForClass(m.getReturnType()).getName());
        //out.print(m.getReturnType().getName());
        out.print(" ");
        //method name
        out.print(m.getName());
        
        //arguments
        out.print("(");
        out.print(getArgListAsString(m, true, true));       
        out.println(")");       
    }
    
    private String getArgListAsString(Method m, boolean includeArgTypes, boolean includeArgNames)
    {
        StringBuilder buf = new StringBuilder(256);
        if (!includeArgNames && !includeArgTypes) { throw new IllegalArgumentException("Cannot generate arglist without both arg types and arg names"); }

        Class<?>[] argTypes = m.getParameterTypes();
        for (int i = 0; i < argTypes.length; ++i)
        {
            if (includeArgTypes)
            {
                buf.append(JavaType.createForClass(argTypes[i]).getName());
                //buf.append(argTypes[i].getName());
                buf.append(' ');
            }

            if (includeArgNames)
            {
                buf.append("arg");
                buf.append(i);
            }
            if (i < argTypes.length - 1)
            {
                buf.append(',');
            }
        }

        return buf.toString();
    }

    
    private void emitMethodBody(PrintWriter out, Method m)
    {
        out.println("\t{");
        
        out.println("\t\t" + debugPrologue(m));
        
        if (m.getGenericReturnType() == Void.TYPE)
        {
            //print out the invocation for the method
            out.println("\t\t" + getInvocation(m));
            
            out.println("\t\t" + debugEpilogue(m));
        }
        else
        {
            out.print("\t\t" + JavaType.createForClass(m.getReturnType()).getName());
            //out.print("\t\t" + m.getReturnType().getName());
            out.print(" _res = ");
            out.println(getInvocation(m));
            
            out.println("\t\t" + debugEpilogue(m));
            
            out.println("\t\treturn _res;");
        }
        
        
        out.println("\t}");     
    }
    
    private String getInvocation(Method m)
    {
        StringBuffer res = new StringBuffer();
        
        res.append(clazz.getSimpleName());
        res.append(".");
        res.append(m.getName());
        res.append("(");
        res.append(getArgListAsString(m, false, true));
        res.append(");");
        
        return res.toString();
    }
    
    private String debugPrologue(Method m)
    {
        StringBuffer res = new StringBuffer();
        
        res.append("String prologue = ");
        
        res.append("\"INFO \" +");
        //res.append("\"Thread: \" + " + "Thread.currentThread().getName()" + " + \"\\n\" +");
        res.append("\"" + m.getName() + "(\" ");        
        
        Class<?>[] params = m.getParameterTypes();
            
        for (int i = 0; params != null && i < params.length; i++)
        {
            res.append(" + ");
            
            res.append(debugArgument(params[i], "arg" + i));
            
            if (i < params.length - 1)
            {
                res.append("   + \", \" ");
            }
        }
        
        res.append("+ \");\";\n");
        
        //print out the string
        res.append("System.err.println(prologue);");
        
        return res.toString();
    }
    
    private String debugArgument(Class<?> param, String argName)
    {
        StringBuffer res = new StringBuffer();
        
        if (param.equals(int.class))
        {
            //print types if log level is debug
            if (sourceLevel == SourceLevel.FIVE)
            {
                res.append("(logLevel == LogLevel.DEBUG ? \"<" + param.getName() + "> \" : \"\" ) + ");
            }
            else
            {
                res.append("(logLevel == DEBUG ? \"<" + param.getName() + "> \" : \"\" ) + ");
            }
            res.append("\" 0x\"+Integer.toHexString(" + argName + ").toUpperCase()");
        }   
        else
        if (param.getName().charAt(0) == '[') //isArray is slower
        {
            res.append("\"[");
            
            if (sourceLevel == SourceLevel.FIVE)
            {
                res.append("\" + (logLevel == LogLevel.DEBUG ? \"<" + param.getComponentType() + "> \" : \"\" ) + \"");
            }
            else
            {
                res.append("\" + (logLevel == DEBUG ? \"<" + param.getComponentType() + "> \" : \"\" ) + \"");              
            }
            
            res.append("]");
            
            res.append("\" + " + argName); //isn't really a pt to be printing out the arg, but to be consistent..
        }
        else
        {
            if (sourceLevel == SourceLevel.FIVE)
            {
                res.append("(logLevel == LogLevel.DEBUG ? \"<" + param.getName() + "> \" : \"\" ) + ");
            }
            else
            {
                res.append("(logLevel == DEBUG ? \"<" + param.getName() + "> \" : \"\" ) + ");              
            }
            res.append(argName);
        }
        
        return res.toString();
    }
    
    private String debugEpilogue(Method m)
    {
        StringBuffer res = new StringBuffer();
        
        res.append("boolean errorARB = isFunctionAvailable(\"glGetDebugMessageLogARB\");\n");
        
        // for debugging: lets print result of glGetError AND glDebugMessageLog
        //res.append("if (!errorARB) {\n");
        res.append("int error = " + clazz.getSimpleName() + ".glGetError();\n");
        
        res.append("String epilogue = ");       
        res.append("(error == GL_NO_ERROR ? \"INFO\" : \"WARN\") + \" glGetError(): \" + error;\n");
        
        res.append("System.err.println(epilogue);");        
        
        // for debugging
        //res.append("}\n");
        //res.append("else {\n");       
        res.append("if (errorARB) {\n");
        
        //int count, int bufsize, int[] sources, int sources_offset, int[] types, int types_offset, int[] ids, int ids_offset, int[] severities, int severities_offset, int[] lengths,
        //int lengths_offset, byte[] messageLog, int messageLog_offset)  {

        res.append(clazz.getSimpleName() + ".glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);\n");
        res.append(clazz.getSimpleName() + ".glDebugMessageControlARB(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, null, true);\n");
        
        res.append("int[] count_buf = new int[1];\n");
        
        //get the maximum size of messages stored
        res.append(clazz.getSimpleName() + ".glGetIntegerv(GL_DEBUG_LOGGED_MESSAGES_ARB, count_buf, 0);\n");
        //res.append("int count = count_buf[0];\n");
        
        //default to 1
        res.append("int count = 1;\n");
        
        //get largest message size stored
        res.append(clazz.getSimpleName() + ".glGetIntegerv(GL_MAX_DEBUG_MESSAGE_LENGTH_ARB, count_buf, 0);\n");
        res.append("int bufsize = count_buf[0];\n");        
        
        res.append("int[] sources = new int[count];\n");
        res.append("int sources_offset = 0;\n");
        res.append("int[] types = new int[count];\n");
        res.append("int types_offset = 0;\n");
        res.append("int[] ids = new int[count];\n");
        res.append("int ids_offset = 0;\n");
        res.append("int[] severities = new int[count];\n");
        res.append("int severities_offset = 0;\n");
        res.append("int[] lengths = new int[count];\n");
        res.append("int lengths_offset = 0;\n");
        res.append("byte[] messageLog = new byte[4096];\n");
        res.append("int messageLog_offset = 0;\n");
        
        res.append("if (count != 0) {\n");
        
        res.append("int arberror = " + clazz.getSimpleName() + ".glGetDebugMessageLogARB(count, bufsize," +
                                                                                     "sources, sources_offset," +
                                                                                     "types, types_offset," +
                                                                                     "ids, ids_offset," +
                                                                                     "severities, severities_offset," +
                                                                                     "lengths, lengths_offset," +
                                                                                     "messageLog, messageLog_offset);\n");
        
        res.append("System.err.println(arberror + \" \" + bufsize);\n");
        
        res.append("}\n");
        res.append("}\n");
        
        return res.toString();      
    }
    
    private void emitLogInitialisation(PrintWriter out)
    {
        //init logging levels
        //this is 1.5 stuff, this class should be compilable for older jvms
        if (sourceLevel == SourceLevel.FIVE)
        {
            out.println("public enum LogLevel {DEBUG, INFO};");
            out.println("private static LogLevel logLevel = LogLevel.INFO;");
            
            out.println("public static void setLogLevel(LogLevel ll) { logLevel = ll;}");
        }
        else
        {
            out.println("public static final byte DEBUG = 1;");
            out.println("public static final byte INFO = 2;");
            out.println("private static byte logLevel = INFO;");
            
            out.println("public static void setLogLevel(byte b) {logLevel = b;}");
        }
        
        
        //init logger thru static block
        //out.println("private static final Logger log;");
        //out.println("static { log = Logger.getLogger(\"DEBUG_GL4_STATIC\"); }");              
    }
        
    private void emit(PrintWriter out)
    {
        //package
        out.println("package " + outputPackage + ";");
        
        //imports
        emitImports(out);
        
        out.println("public class " + outputName);
        out.println(" extends " + clazz.getName());
        out.println("{");
        
        emitLogInitialisation(out);
        
        //emitClassFields(out);
        
        emitMethods(out);
        
        out.println("}");
    }
        
    public void emit() throws IOException
    {
        File file = new File(outputDir + File.separatorChar + outputName + ".java");
        String parentDir = file.getParent();
        if (parentDir != null) 
        {
            File pDirFile = new File(parentDir);
            pDirFile.mkdirs();
        }
        
        PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        
        emit(output);
        
        output.flush();
        output.close();
    }


    /**
     * @param args
     */
    public static void main(String[] args) throws Throwable
    {
        BuildStaticDebugger debugger = new BuildStaticDebugger(args[0], args[1], args[2], Class.forName(args[3], false, BuildStaticDebugger.class.getClassLoader()));  //BuildComposablePipeline.getClass(args[3]));
        
        //BuildStaticDebugger debugger = new BuildStaticDebugger("/Users/angelystor/", "test", "TestGL", GL4StaticBase.class);
        
        debugger.emit();
    }

}

