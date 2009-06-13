/*
 * Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
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
 */

package com.sun.javafx.newt.x11;

import com.sun.javafx.newt.*;
import com.sun.javafx.newt.impl.*;
import javax.media.nativewindow.*;
import javax.media.nativewindow.x11.*;

public class X11Window extends Window {
    private static final String WINDOW_CLASS_NAME = "NewtWindow";
    // non fullscreen dimensions ..
    private int nfs_width, nfs_height, nfs_x, nfs_y;

    static {
        NativeLibLoader.loadNEWT();

        if (!initIDs()) {
            throw new NativeWindowException("Failed to initialize jmethodIDs");
        }
    }

    public X11Window() {
    }

    protected void createNative(Capabilities caps) {
        config = GraphicsConfigurationFactory.getFactory(getScreen().getDisplay().getGraphicsDevice()).chooseGraphicsConfiguration(caps, null, getScreen().getGraphicsScreen());
        if (config == null) {
            throw new NativeWindowException("Error choosing GraphicsConfiguration creating window: "+this);
        }
        X11GraphicsConfiguration x11config = (X11GraphicsConfiguration) config;
        long visualID = x11config.getVisualID();
        long w = CreateWindow(getDisplayHandle(), getScreenIndex(), visualID, x, y, width, height);
        if (w == 0 || w!=windowHandle) {
            throw new NativeWindowException("Error creating window: "+w);
        }
        windowHandleClose = windowHandle;
        displayHandleClose = getDisplayHandle();
    }

    protected void closeNative() {
        if(0!=displayHandleClose && 0!=windowHandleClose) {
            CloseWindow(displayHandleClose, windowHandleClose);
            windowHandleClose = 0;
            displayHandleClose = 0;
        }
    }

    protected void windowDestroyed() {
        windowHandleClose = 0;
        displayHandleClose = 0;
        super.windowDestroyed();
    }

    public void setVisible(boolean visible) {
        if(this.visible!=visible) {
            this.visible=visible;
            setVisible0(getDisplayHandle(), windowHandle, visible);
            clearEventMask();
        }
    }

    public void setSize(int width, int height) {
        if(!fullscreen) {
            nfs_width=width;
            nfs_height=height;
        }
        setSize0(getDisplayHandle(), getScreenIndex(), windowHandle, x, y, width, height, 0, visible);
    }

    public void setPosition(int x, int y) {
        if(!fullscreen) {
            nfs_x=x;
            nfs_y=y;
        }
        setPosition0(getDisplayHandle(), windowHandle, x, y);
    }

    public boolean setFullscreen(boolean fullscreen) {
        if(this.fullscreen!=fullscreen) {
            int x,y,w,h;
            this.fullscreen=fullscreen;
            if(fullscreen) {
                x = 0; y = 0;
                w = screen.getWidth();
                h = screen.getHeight();
            } else {
                x = nfs_x;
                y = nfs_y;
                w = nfs_width;
                h = nfs_height;
            }
            if(DEBUG_IMPLEMENTATION || DEBUG_WINDOW_EVENT) {
                System.err.println("X11Window fs: "+fullscreen+" "+x+"/"+y+" "+w+"x"+h);
            }
            setSize0(getDisplayHandle(), getScreenIndex(), windowHandle, x, y, w, h, fullscreen?-1:1, visible);
        }
        return fullscreen;
    }

    protected void dispatchMessages(int eventMask) {
        DispatchMessages(getDisplayHandle(), windowHandle, eventMask, windowDeleteAtom);
    }

    //----------------------------------------------------------------------
    // Internals only
    //

    private static native boolean initIDs();
    private        native long CreateWindow(long display, int screen_index, 
                                            long visualID, int x, int y, int width, int height);
    private        native void CloseWindow(long display, long windowHandle);
    private        native void setVisible0(long display, long windowHandle, boolean visible);
    private        native void DispatchMessages(long display, long windowHandle, int eventMask, long windowDeleteAtom);
    private        native void setSize0(long display, int screen_index, long windowHandle, 
                                        int x, int y, int width, int height, int decorationToggle, boolean isVisible);
    private        native void setPosition0(long display, long windowHandle, int x, int y);

    private void sizeChanged(int newWidth, int newHeight) {
        width = newWidth;
        height = newHeight;
        if(!fullscreen) {
            nfs_width=width;
            nfs_height=height;
        }
        sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED);
    }

    private void positionChanged(int newX, int newY) {
        x = newX;
        y = newY;
        if(!fullscreen) {
            nfs_x=x;
            nfs_y=y;
        }
        sendWindowEvent(WindowEvent.EVENT_WINDOW_MOVED);
    }

    private void windowCreated(long windowHandle, long windowDeleteAtom) {
        this.windowHandle = windowHandle;
        this.windowDeleteAtom=windowDeleteAtom;
    }

    private long   windowHandleClose;
    private long   displayHandleClose;
    private long   windowDeleteAtom;
}
