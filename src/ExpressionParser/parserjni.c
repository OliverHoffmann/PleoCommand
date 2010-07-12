#include "parserjni.h"

#include "exprparser.h"

JNIEXPORT jlong JNICALL Java_pleocmd_api_ExpressionParser_parse(JNIEnv *env,
        jobject this __attribute__ ((unused)), jstring expression) {
	const char *exp = (*env)->GetStringUTFChars(env, expression, NULL);
	instrlist *res = parse(exp);
	(*env)->ReleaseStringUTFChars(env, expression, exp);
	return (jlong) res;
}

JNIEXPORT jdouble JNICALL Java_pleocmd_api_ExpressionParser_execute(
        JNIEnv *env, jobject this __attribute__ ((unused)), jlong handle, jdoubleArray channelData) {
	jdouble *cd = (*env)->GetDoubleArrayElements(env, channelData, NULL);
	jdouble res = execute((instrlist *) (long) handle, cd,
	        (*env)->GetArrayLength(env, channelData));
	(*env)->ReleaseDoubleArrayElements(env, channelData, cd, 0);
	return res;
}

JNIEXPORT jint JNICALL Java_pleocmd_api_ExpressionParser_getLastError(
        JNIEnv *env __attribute__ ((unused)), jobject this __attribute__ ((unused)), jlong handle) {
	return ((instrlist *) handle)->lastError;
}

JNIEXPORT jint JNICALL Java_pleocmd_api_ExpressionParser_getLastErrorPos(
        JNIEnv *env __attribute__ ((unused)), jobject this __attribute__ ((unused)), jlong handle) {
	return ((instrlist *) handle)->lastErrorPos;
}

JNIEXPORT jstring JNICALL Java_pleocmd_api_ExpressionParser_getInstructions(
        JNIEnv *env, jobject this __attribute__ ((unused)), jlong handle) {
	char *pc = printAll((instrlist *) handle);
	jstring res = (*env)->NewStringUTF(env, pc);
	free(pc);
	return res;
}

JNIEXPORT void JNICALL Java_pleocmd_api_ExpressionParser_freeHandle(JNIEnv *env __attribute__ ((unused)),
		jobject this __attribute__ ((unused)), jlong handle) {
	freeInstrList((instrlist *) handle);
}
