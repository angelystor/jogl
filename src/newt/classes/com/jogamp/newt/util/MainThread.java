/*
 * Copyright (c) 2009 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright (c) 2010 JogAmp Community. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

package com.jogamp.newt.util;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlContext;
import java.security.AccessController;

import javax.media.nativewindow.NativeWindowFactory;

import com.jogamp.common.os.Platform;
import com.jogamp.common.util.ReflectionUtil;

import jogamp.newt.Debug;
import jogamp.newt.NEWTJNILibLoader;

/**
 * NEWT Utility class MainThread<P>
 *
 * <p>
 * FIXME: Update this documentation! 
 * This class just provides a main-thread utility, forking of a main java class
 * on another thread while being able to continue doing platform specific things
 * on the main-thread. The latter is essential for eg. MacOSX, where we continue
 * to run NSApp.run().
 * </p>
 * 
 * This class provides a startup singleton <i>main thread</i>,
 * from which a new thread with the users main class is launched.<br>
 *
 * Such behavior is necessary for native windowing toolkits,
 * where the windowing management must happen on the so called
 * <i>main thread</i> e.g. for Mac OS X !<br>
 *
 * Utilizing this class as a launchpad, now you are able to
 * use a NEWT multithreaded application with window handling within the different threads,
 * even on these restricted platforms.<br>
 *
 * To support your NEWT Window platform, 
 * you have to pass your <i>main thread</i> actions to {@link #invoke invoke(..)},
 * have a look at the {@link com.jogamp.newt.macosx.MacWindow MacWindow} implementation.<br>
 * <i>TODO</i>: Some hardcoded dependencies exist in this implementation, 
 * where you have to patch this code or factor it out. <P>
 * 
 * If your platform is not Mac OS X, but you want to test your code without modifying
 * this class, you have to set the system property <code>newt.MainThread.force</code> to <code>true</code>.<P>
 *
 * The code is compatible with all other platform, which support multithreaded windowing handling.
 * Since those platforms won't trigger the <i>main thread</i> serialization, the main method 
 * will be simply executed, in case you haven't set <code>newt.MainThread.force</code> to <code>true</code>.<P>
 *
 * Test case on Mac OS X (or any other platform):
 <PRE>
    java -XstartOnFirstThread com.jogamp.newt.util.MainThread demos.es1.RedSquare -GL2 -GL2 -GL2 -GL2
 </PRE>
 * Which starts 4 threads, each with a window and OpenGL rendering.<br>
 */
public class MainThread {
    private static final String MACOSXDisplayClassName = "jogamp.newt.driver.macosx.MacDisplay";
    private static final Platform.OSType osType;
    private static final boolean isMacOSX;
        
    
    /** if true, use the main thread EDT, otherwise AWT's EDT */
    public static final boolean  HINT_USE_MAIN_THREAD;
    
    static {
        final AccessControlContext localACC = AccessController.getContext();
        NativeWindowFactory.initSingleton(true);
        NEWTJNILibLoader.loadNEWT();
        HINT_USE_MAIN_THREAD = !NativeWindowFactory.isAWTAvailable() || 
                                Debug.getBooleanProperty("newt.MainThread.force", true, localACC);
        osType = Platform.getOSType();
        isMacOSX = osType == Platform.OSType.MACOS;                
    }
    
    public static boolean useMainThread = false;
    
    protected static final boolean DEBUG = Debug.debug("MainThread");

    private static final MainThread singletonMainThread = new MainThread(); // one singleton MainThread

    static class UserApp extends Thread {
        private String mainClassName;
        private String[] mainClassArgs;

        private Method mainClassMain;

        public UserApp(String mainClassName, String[] mainClassArgs) {
            this.mainClassName=mainClassName;
            this.mainClassArgs=mainClassArgs;
        }

        @Override
        public void run() {
            setName(getName()+"-UserApp");
            
            if(DEBUG) System.err.println("MainAction.run(): "+Thread.currentThread().getName()+" start");
            // start user app ..
            try {
                Class<?> mainClass = ReflectionUtil.getClass(mainClassName, true, getClass().getClassLoader());
                if(null==mainClass) {
                    throw new RuntimeException(new ClassNotFoundException("MainAction couldn't find main class "+mainClassName));
                }
                try {
                    mainClassMain = mainClass.getDeclaredMethod("main", new Class[] { String[].class });
                    mainClassMain.setAccessible(true);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
                if(DEBUG) System.err.println("MainAction.run(): "+Thread.currentThread().getName()+" invoke "+mainClassName);
                mainClassMain.invoke(null, new Object[] { mainClassArgs } );
            } catch (InvocationTargetException ite) {
                ite.getTargetException().printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }

            if(DEBUG) System.err.println("MainAction.run(): "+Thread.currentThread().getName()+" user app fin");

            if ( useMainThread ) {
                if(isMacOSX) {
                    try {
                        if(DEBUG) {
                            System.err.println("MainAction.main(): "+Thread.currentThread()+" MainAction fin - stopNSApp.0"); 
                        }
                        ReflectionUtil.callStaticMethod(MACOSXDisplayClassName, "stopNSApplication", 
                            null, null, MainThread.class.getClassLoader());
                        if(DEBUG) {
                            System.err.println("MainAction.main(): "+Thread.currentThread()+" MainAction fin - stopNSApp.X"); 
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if(DEBUG) System.err.println("MainAction.run(): "+Thread.currentThread().getName()+" MainAction fin - System.exit(0)");
                    System.exit(0);                
                }   
            }
        }
    }
    private static UserApp mainAction;

    /** Your new java application main entry, which pipelines your application */
    public static void main(String[] args) {
        final Thread cur = Thread.currentThread();
        
        useMainThread = HINT_USE_MAIN_THREAD;

        if(DEBUG) {
            System.err.println("MainThread.main(): "+cur.getName()+
                ", useMainThread "+ useMainThread +
                ", HINT_USE_MAIN_THREAD "+ HINT_USE_MAIN_THREAD +
                ", isAWTAvailable " + NativeWindowFactory.isAWTAvailable() + ", ostype "+osType+", isMacOSX "+isMacOSX);
        }

        if(!useMainThread && !NativeWindowFactory.isAWTAvailable()) {
            throw new RuntimeException("!USE_MAIN_THREAD and no AWT available");
        }
        
        if(args.length==0) {
            return;
        }

        String mainClassName=args[0];
        String[] mainClassArgs=new String[args.length-1];
        if(args.length>1) {
            System.arraycopy(args, 1, mainClassArgs, 0, args.length-1);
        }

        mainAction = new UserApp(mainClassName, mainClassArgs);

        if(isMacOSX) {
            ReflectionUtil.callStaticMethod(MACOSXDisplayClassName, "initSingleton", 
                null, null, MainThread.class.getClassLoader());
        }

        if ( useMainThread ) {
            try {
                cur.setName(cur.getName()+"-MainThread");
            } catch (Exception e) {}
            
            // dispatch user's main thread ..
            mainAction.start();
            
            if(isMacOSX) {
                try {
                    if(DEBUG) {
                        System.err.println("MainThread.main(): "+cur.getName()+"- runNSApp"); 
                    }
                    ReflectionUtil.callStaticMethod(MACOSXDisplayClassName, "runNSApplication", 
                        null, null, MainThread.class.getClassLoader());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }   
            if(DEBUG) { System.err.println("MainThread - wait until last non daemon thread ends ..."); }            
        } else {
            // run user's main in this thread 
            mainAction.run();
        }
    }

    public static MainThread getSingleton() {
        return singletonMainThread;
    }

}


