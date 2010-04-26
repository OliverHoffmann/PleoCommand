#include "parserjni.h"

#include "exprparser.h"

JNIEXPORT jlong JNICALL Java_pleocmd_api_ExpressionParser_parse(JNIEnv *env,
        jobject this, jstring expression) {
	const char *exp = (*env)->GetStringUTFChars(env, expression, NULL);
	instrlist *res = parse(exp);
	(*env)->ReleaseStringUTFChars(env, expression, exp);
	return (jlong) (int) res;
}

JNIEXPORT jdouble JNICALL Java_pleocmd_api_ExpressionParser_execute(
        JNIEnv *env, jobject this, jlong handle, jdoubleArray channelData) {
	jdouble *cd = (*env)->GetDoubleArrayElements(env, channelData, NULL);
	jdouble res = execute((instrlist *) (long) handle, cd,
	        (*env)->GetArrayLength(env, channelData));
	(*env)->ReleaseDoubleArrayElements(env, channelData, cd, 0);
	return res;
}

JNIEXPORT jint JNICALL Java_pleocmd_api_ExpressionParser_getLastError(
        JNIEnv *env, jobject this, jlong handle) {
	return ((instrlist *) (long) handle)->lastError;
}

JNIEXPORT jint JNICALL Java_pleocmd_api_ExpressionParser_getLastErrorPos(
        JNIEnv *env, jobject this, jlong handle) {
	return ((instrlist *) (long) handle)->lastErrorPos;
}

JNIEXPORT void JNICALL Java_pleocmd_api_ExpressionParser_freeHandle(JNIEnv *env,
		jobject this, jlong handle) {
	freeInstrList((instrlist *) (long) handle);
}
