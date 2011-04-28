
//private static HashMap/*<MemoryObject>*/ arbMemCache = new HashMap();

/** Entry point to C language function: <br> <code> LPVOID glMapBuffer(GLenum target, GLenum access); </code>    */
//if uncommenting this, see the custom C code and remove that too

public static java.nio.ByteBuffer glMapBuffer(int target, int access) {
  final long __addr_ = ((GL4bcProcAddressTable)procAddressTable)._addressof_glMapBuffer;
  if (__addr_ == 0) {
    throw new GLException("Method \"glMapBuffer\" not available");
  }
  //declared as an instance var instead
  //HashMap<MemoryObject> arbMemCache = new HashMap();  
  HashMap arbMemCache = new HashMap();  
  
  //final long sz = bufferSizeTracker.getBufferSize(bufferStateTracker, target, this);
  //we can put this into GLBufferSizeTracker, or even link GLContextManager to call getBufferSize instead
  //GLBufferStateTracker bufferStateTracker = GLContextManager.getCurrentBufferStateTracker();
  //final long sz = GLContextManager.getCurrentBufferSizeTracker().getBufferSize(bufferStateTracker, target);
  final long sz = getMapBufferSize(target);
  
  if (0 == sz) {
    return null;
  }
  final long addr = dispatch_glMapBuffer(target, access, __addr_);
  if (0 == addr) {
    return null;
  }
  ByteBuffer buffer;
  MemoryObject memObj0 = new MemoryObject(addr, sz); // object and key
  MemoryObject memObj1 = MemoryObject.getOrAddSafe(arbMemCache, memObj0);
  if(memObj0 == memObj1) {
    // just added ..
    if(null != memObj0.getBuffer()) {
        throw new InternalError();
    }
    buffer = newDirectByteBuffer(addr, sz);
    Buffers.nativeOrder(buffer);
    memObj0.setBuffer(buffer);
  } else {
    // already mapped
    buffer = memObj1.getBuffer();
    if(null == buffer) {
        throw new InternalError();
    }
  }
  buffer.position(0);
  return buffer;
}


/** Encapsulates function pointer for OpenGL function <br>: <code> LPVOID glMapBuffer(GLenum target, GLenum access); </code>    */
native static private long dispatch_glMapBuffer(int target, int access, long glProcAddress);

/** Entry point to C language function: <code> GLvoid *  {@native glMapNamedBufferEXT}(GLuint buffer, GLenum access); </code> <br>Part of <code>GL_EXT_direct_state_access</code>   */
public static java.nio.ByteBuffer glMapNamedBufferEXT(int bufferName, int access)  {
  final long __addr_ = ((GL4bcProcAddressTable)procAddressTable)._addressof_glMapNamedBufferEXT;
  if (__addr_ == 0) {
    throw new GLException("Method \"glMapNamedBufferEXT\" not available");
  }
  //declared as an instance var instead
  HashMap/*<MemoryObject>*/ arbMemCache = new HashMap();
  //final long sz = bufferSizeTracker.getDirectStateBufferSize(bufferName, this);
  //final long sz = GLContextManager.getCurrentBufferSizeTracker().getDirectStateBufferSize(bufferName);
  final long sz = getDirectMapBufferSize(bufferName);
  
  if (0 == sz) {
    return null;
  }
  final long addr = dispatch_glMapNamedBufferEXT(bufferName, access, __addr_);
  if (0 == addr) {
    return null;
  }
  ByteBuffer buffer;
  MemoryObject memObj0 = new MemoryObject(addr, sz); // object and key
  MemoryObject memObj1 = MemoryObject.getOrAddSafe(arbMemCache, memObj0);
  if(memObj0 == memObj1) {
    // just added ..
    if(null != memObj0.getBuffer()) {
        throw new InternalError();
    }
    buffer = newDirectByteBuffer(addr, sz);
    Buffers.nativeOrder(buffer);
    memObj0.setBuffer(buffer);
  } else {
    // already mapped
    buffer = memObj1.getBuffer();
    if(null == buffer) {
        throw new InternalError();
    }
  }
  buffer.position(0);
  return buffer;
}

private static native long dispatch_glMapNamedBufferEXT(int buffer, int access, long procAddress);

native static private ByteBuffer newDirectByteBuffer(long addr, long capacity);

    public static void glVertexPointer(GLArrayData array) {
      if(array.getComponentNumber()==0) return;
      if(array.isVBO()) {
          glVertexPointer(array.getComponentNumber(), array.getComponentType(), array.getStride(), array.getVBOOffset());
      } else {
          glVertexPointer(array.getComponentNumber(), array.getComponentType(), array.getStride(), array.getBuffer());
      }
    }
    public static void glColorPointer(GLArrayData array) {
      if(array.getComponentNumber()==0) return;
      if(array.isVBO()) {
          glColorPointer(array.getComponentNumber(), array.getComponentType(), array.getStride(), array.getVBOOffset());
      } else {
          glColorPointer(array.getComponentNumber(), array.getComponentType(), array.getStride(), array.getBuffer());
      }

    }
    public static void glNormalPointer(GLArrayData array) {
      if(array.getComponentNumber()==0) return;
      if(array.getComponentNumber()!=3) {
        throw new GLException("Only 3 components per normal allowed");
      }
      if(array.isVBO()) {
          glNormalPointer(array.getComponentType(), array.getStride(), array.getVBOOffset());
      } else {
          glNormalPointer(array.getComponentType(), array.getStride(), array.getBuffer());
      }
    }
    public static void glTexCoordPointer(GLArrayData array) {
      if(array.getComponentNumber()==0) return;
      if(array.isVBO()) {
          glTexCoordPointer(array.getComponentNumber(), array.getComponentType(), array.getStride(), array.getVBOOffset());
      } else {
          glTexCoordPointer(array.getComponentNumber(), array.getComponentType(), array.getStride(), array.getBuffer());
      }
    }

