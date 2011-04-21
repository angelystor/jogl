//COPIED/MODIFIED FROM gl-impl-CustomJavaCode-gl4bc
// Tracks glBegin/glEnd calls to determine whether it is legal to
// query Vertex Buffer Object state
private static boolean inBeginEndPair;

private static ProcAddressTable procAddressTable = new GL4bcProcAddressTable();

static
{
	//"com.jogamp.opengl.impl.gl4.GL4bcProcAddressTable"
	//TODO make this discoverable by reflection instead
	//TODO, we're getting GLProfile.getDefault, but this may not be the case all the time in case the user land app wants to target another profile
	//TODO 0 implies we're using non ES1/ES2,EGL/ES profiles, while this is fine for desktop cases, this (obviously) isn't fine for embedded GLs
	procAddressTable = (ProcAddressTable)ReflectionUtil.createInstance("jogamp.opengl.gl4.GL4bcProcAddressTable", 
					  new Class[] { FunctionAddressResolver.class },
					  new Object[] { new GLProcAddressResolver() }, 
					  GL4Static.class.getClassLoader());
	GLDrawableFactoryImpl factory = (GLDrawableFactoryImpl) GLDrawableFactory.getFactory(GLProfile.getMaxProgrammable());

	procAddressTable.reset(factory.getGLDynamicLookupHelper(0));
}

//shifted from cfg file
public static void glFrustumf(float left, float right, float bottom, float top, float zNear, float zFar) 
{
	glFrustum((double)left, (double)right, (double)bottom, (double)top, (double)zNear, (double)zFar);
}

public static void glOrthof(float left, float right, float bottom, float top, float zNear, float zFar)
{
	glOrtho((double)left, (double)right, (double)bottom, (double)top, (double)zNear, (double)zFar); 
}

/* FIXME: refactor dependence on Java 2D / JOGL bridge

// Tracks creation and destruction of server-side OpenGL objects when
// the Java2D/OpenGL pipeline is enabled and it is using frame buffer
// objects (FBOs) to do its rendering
private GLObjectTracker tracker;

public void setObjectTracker(GLObjectTracker tracker) {
  this.tracker = tracker;
}

*/


/**
 * Provides platform-independent access to the wglAllocateMemoryNV /
 * glXAllocateMemoryNV extension.
 */
 //TODO refer to (2), https://sites.google.com/site/angelystor/Home/jogl/sven-7-haha-questions
 /*
public java.nio.ByteBuffer glAllocateMemoryNV(int arg0, float arg1, float arg2, float arg3) {
  return _context.glAllocateMemoryNV(arg0, arg1, arg2, arg3);
}*/

//
// Helpers for ensuring the correct amount of texture data
//

/** Returns the number of bytes required to fill in the appropriate
    texture. This is computed as closely as possible based on the
    pixel pack or unpack parameters. The logic in this routine is
    based on code in the SGI OpenGL sample implementation. */

private static int imageSizeInBytes(int format, int type, int w, int h, int d,
                             boolean pack) {
  int elements = 0;
  int esize = 0;
  
  if (w < 0) return 0;
  if (h < 0) return 0;
  if (d < 0) return 0;
  switch (format) {
  case GL_COLOR_INDEX:
  case GL_STENCIL_INDEX:
    elements = 1;
    break;
  case GL_RED:
  case GL_GREEN:
  case GL_BLUE:
  case GL_ALPHA:
  case GL_LUMINANCE:
  case GL_DEPTH_COMPONENT:
    elements = 1;
    break;
  case GL_LUMINANCE_ALPHA:
    elements = 2;
    break;
  case GL_RGB:
  case GL_BGR:
    elements = 3;
    break;
  case GL_RGBA:
  case GL_BGRA:
  case GL_ABGR_EXT:
    elements = 4;
    break;
  /* FIXME ?? 
   case GL_HILO_NV:
    elements = 2;
    break; */
  default:
    return 0;
  }
  switch (type) {
  case GL_BITMAP:
    if (format == GL_COLOR_INDEX) {
      return (d * (h * ((w+7)/8)));
    } else {
      return 0;
    }
  case GL_BYTE:
  case GL_UNSIGNED_BYTE:
    esize = 1;
    break;
  case GL_UNSIGNED_BYTE_3_3_2:
  case GL_UNSIGNED_BYTE_2_3_3_REV:
    esize = 1;
    elements = 1;
    break;
  case GL_SHORT:
  case GL_UNSIGNED_SHORT:
    esize = 2;
    break;
  case GL_UNSIGNED_SHORT_5_6_5:
  case GL_UNSIGNED_SHORT_5_6_5_REV:
  case GL_UNSIGNED_SHORT_4_4_4_4:
  case GL_UNSIGNED_SHORT_4_4_4_4_REV:
  case GL_UNSIGNED_SHORT_5_5_5_1:
  case GL_UNSIGNED_SHORT_1_5_5_5_REV:
    esize = 2;
    elements = 1;
    break;
  case GL_INT:
  case GL_UNSIGNED_INT:
  case GL_FLOAT:
    esize = 4;
    break;
  case GL_UNSIGNED_INT_8_8_8_8:
  case GL_UNSIGNED_INT_8_8_8_8_REV:
  case GL_UNSIGNED_INT_10_10_10_2:
  case GL_UNSIGNED_INT_2_10_10_10_REV:
    esize = 4;
    elements = 1;
    break;
  default:
    return 0;
  }
  return imageSizeInBytes(elements * esize, w, h, d, pack);
}

//private static GLBufferSizeTracker  bufferSizeTracker = new GLBufferSizeTracker();
//private static GLBufferStateTracker bufferStateTracker = new GLBufferStateTracker();
//private static GLStateTracker       glStateTracker = new GLStateTracker();

/*
private static boolean bufferObjectExtensionsInitialized = false;
private static boolean haveARBPixelBufferObject;
private static boolean haveEXTPixelBufferObject;
private static boolean haveGL15;
private static boolean haveGL21;
private static boolean haveARBVertexBufferObject;
*/

/**
  Call this function only if a context is current
*/
public static boolean isFunctionAvailable(String glFunctionName)
{
	return ((GLContextImpl)GLContext.getCurrent()).isFunctionAvailable(glFunctionName);
//	return GLContextManager.isFunctionAvailable(glFunctionName);
}
/*
private static void initBufferObjectExtensionChecks() {
  if (bufferObjectExtensionsInitialized)
    return;
  bufferObjectExtensionsInitialized = true;

  haveARBPixelBufferObject  = GLContextManager.isExtensionAvailable("GL_ARB_pixel_buffer_object");
  haveEXTPixelBufferObject  = GLContextManager.isExtensionAvailable("GL_EXT_pixel_buffer_object");
  haveGL15                  = GLContextManager.isExtensionAvailable("GL_VERSION_1_5");
  haveGL21                  = GLContextManager.isExtensionAvailable("GL_VERSION_2_1");
  haveARBVertexBufferObject = GLContextManager.isExtensionAvailable("GL_ARB_vertex_buffer_object");
  
}
*/

private static void initBufferObjectExtensionChecks()
{
	/*
	if (GLContextManager.isBufferObjectExtensionsInitialized())
	{
		return;
	}
	
	GLContextManager.initBufferObjectExtensionChecks();
	*/
}

private static boolean checkBufferObject(boolean extension1,
                                  boolean extension2,
                                  boolean extension3,
                                  boolean enabled,
                                  int state,
                                  String kind, boolean throwException) {
                                  /*
  if (inBeginEndPair) {
    throw new GLException("May not call this between glBegin and glEnd");
  }
  
  boolean avail = (extension1 || extension2 || extension3);
  if (!avail) {
    if (!enabled)
      return true;
    if(throwException) {
        throw new GLException("Required extensions not available to call this function");
    }
    return false;
  }
  //int buffer = bufferStateTracker.getBoundBufferObject(state, this);
  int buffer = GLContextManager.getCurrentBufferStateTracker().getBoundBufferObject(state);
  if (enabled) {
    if (buffer == 0) {
      if(throwException) {
          throw new GLException(kind + " must be enabled to call this method");
      }
      return false;
    }
  } else {
    if (buffer != 0) {
      if(throwException) {
          throw new GLException(kind + " must be disabled to call this method");
      }
      return false;
    }
  }
  */
  return true;
  
}  

private static boolean checkArrayVBODisabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  /*
  return checkBufferObject(GLContextManager.isHaveGL15(),
                    GLContextManager.isHaveARBVertexBufferObject(),
                    false,
                    false,
                    GL_ARRAY_BUFFER,
                    "array vertex_buffer_object", throwException);
  */
  return false;
}

private static boolean checkArrayVBOEnabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  /*
  return checkBufferObject(GLContextManager.isHaveGL15(),
                    GLContextManager.isHaveARBVertexBufferObject(),
                    false,
                    true,
                    GL_ARRAY_BUFFER,
                    "array vertex_buffer_object", throwException);
   */
   return true;
}

private static boolean checkElementVBODisabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  /*
  return checkBufferObject(GLContextManager.isHaveGL15(),
                    GLContextManager.isHaveARBVertexBufferObject(),
                    false,
                    false,
                    GL_ELEMENT_ARRAY_BUFFER,
                    "element vertex_buffer_object", throwException);
    */
    return false;
}

private static boolean checkElementVBOEnabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  /*
  return checkBufferObject(GLContextManager.isHaveGL15(),
                    GLContextManager.isHaveARBVertexBufferObject(),
                    false,
                    true,
                    GL_ELEMENT_ARRAY_BUFFER,
                    "element vertex_buffer_object", throwException);
    */
    return true;
}

private static boolean checkUnpackPBODisabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  /*
  return checkBufferObject(GLContextManager.isHaveARBPixelBufferObject(),
                    GLContextManager.isHaveEXTPixelBufferObject(),
                    GLContextManager.isHaveGL21(),
                    false,
                    GL_PIXEL_UNPACK_BUFFER,
                    "unpack pixel_buffer_object", throwException);
    */
    return false;
}

private static boolean checkUnpackPBOEnabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  /*
  return checkBufferObject(GLContextManager.isHaveARBPixelBufferObject(),
                    GLContextManager.isHaveEXTPixelBufferObject(),
                    GLContextManager.isHaveGL21(),
                    true,
                    GL_PIXEL_UNPACK_BUFFER,
                    "unpack pixel_buffer_object", throwException);
	*/
	return true;
}

private static boolean checkPackPBODisabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  /*
  return checkBufferObject(GLContextManager.isHaveARBPixelBufferObject(),
                    GLContextManager.isHaveEXTPixelBufferObject(),
                    GLContextManager.isHaveGL21(),
                    false,
                    GL_PIXEL_PACK_BUFFER,
                    "pack pixel_buffer_object", throwException);
    */
    return false;
}

private static boolean checkPackPBOEnabled(boolean throwException) { 
  initBufferObjectExtensionChecks();
  /*
  return checkBufferObject(GLContextManager.isHaveARBPixelBufferObject(),
                    GLContextManager.isHaveEXTPixelBufferObject(),
                    GLContextManager.isHaveGL21(),
                    true,
                    GL_PIXEL_PACK_BUFFER,
                    "pack pixel_buffer_object", throwException);
    */
    return true;
}

public static boolean glIsPBOPackEnabled() {
    return checkPackPBOEnabled(false);
}

public static boolean glIsPBOUnpackEnabled() {
    return checkUnpackPBOEnabled(false);
}

/**
 * Taken from GLBufferSizeTracker since we don't have the state to query first
 * Used by glMapBuffer
*/
private static long getMapBufferSize(int target)
{
	// We don't know what's going on in this case; query the GL for an
	// answer
	// FIXME: both functions return 'int' types, which is not suitable,
	// since buffer lenght is 64bit ?
	int[] tmp = new int[1];
	glGetBufferParameteriv(target, GL_BUFFER_SIZE, tmp, 0);

	return (long) tmp[0];
}

/**
 * Taken from GLBufferSizeTracker.getBufferSizeImpl
 */
private static long getDirectMapBufferSize(int buffer)
{
	int[] tmp = new int[1];
	glGetNamedBufferParameterivEXT(buffer, GL_BUFFER_SIZE, tmp, 0);

    if (tmp[0] == 0) {
          // Assume something is wrong rather than silently going along
          throw new GLException("Error: buffer size returned by glGetNamedBufferParameterivEXT was zero; probably application error");
        }
    long sz = (long)tmp[0];
    
    return sz;
}
