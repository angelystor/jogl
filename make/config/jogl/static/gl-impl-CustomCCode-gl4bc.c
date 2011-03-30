/*   Java->C glue code:
 *   Java package: com.jogamp.opengl.impl.gl4.GL4bcImpl
 *    Java method: long dispatch_glMapBuffer(int target, int access)
 *     C function: void * glMapBuffer(GLenum target, GLenum access);
 * todo: dunno why it doesn't auto generate this .....
 */
 
JNIEXPORT jlong JNICALL 
Java_jogamp_opengl_gl4_GL4Static_dispatch_1glMapBuffer(JNIEnv *env, jobject _unused, jint target, jint access, jlong glProcAddress) {
  PFNGLMAPBUFFERPROC ptr_glMapBuffer;
  void * _res;
  ptr_glMapBuffer = (PFNGLMAPBUFFERPROC) (intptr_t) glProcAddress;
  assert(ptr_glMapBuffer != NULL);
  _res = (* ptr_glMapBuffer) ((GLenum) target, (GLenum) access);
  return (jlong) (intptr_t) _res;
}

/*   Java->C glue code:
 *   Java package: com.jogamp.opengl.impl.gl4.GL4bcImpl
 *    Java method: long dispatch_glMapNamedBufferEXT(int target, int access)
 *     C function: void * glMapNamedBufferEXT(GLenum target, GLenum access);
 */
JNIEXPORT jlong JNICALL 
Java_jogamp_opengl_gl4_GL4Static_dispatch_1glMapNamedBufferEXT(JNIEnv *env, jobject _unused, jint target, jint access, jlong glProcAddress) {
  PFNGLMAPNAMEDBUFFEREXTPROC ptr_glMapNamedBufferEXT;
  void * _res;
  ptr_glMapNamedBufferEXT = (PFNGLMAPNAMEDBUFFEREXTPROC) (intptr_t) glProcAddress;
  assert(ptr_glMapNamedBufferEXT != NULL);
  _res = (* ptr_glMapNamedBufferEXT) ((GLenum) target, (GLenum) access);
  return (jlong) (intptr_t) _res;
}

/*   Java->C glue code:
 *   Java package: com.jogamp.opengl.impl.gl4.GL4bcImpl
 *    Java method: ByteBuffer newDirectByteBuffer(long addr, long capacity);
 *     C function: jobject newDirectByteBuffer(jlong addr, jlong capacity);
 */
JNIEXPORT jobject JNICALL
Java_jogamp_opengl_gl4_GL4Static_newDirectByteBuffer(JNIEnv *env, jobject _unused, jlong addr, jlong capacity) {
  return (*env)->NewDirectByteBuffer(env, (void*) (intptr_t) addr, capacity);
}
