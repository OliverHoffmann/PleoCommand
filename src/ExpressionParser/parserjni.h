#ifndef PARSERJNI_H
#define PARSERJNI_H

#include <jni.h>

JNIEXPORT jlong JNICALL Java_pleocmd_api_ExpressionParser_parse(JNIEnv *,
        jobject, jstring);

JNIEXPORT jdouble JNICALL Java_pleocmd_api_ExpressionParser_execute(JNIEnv *,
        jobject, jlong, jdoubleArray);

JNIEXPORT jint JNICALL Java_pleocmd_api_ExpressionParser_getLastError(JNIEnv *,
        jobject, jlong);

JNIEXPORT jint JNICALL Java_pleocmd_api_ExpressionParser_getLastErrorPos(
        JNIEnv *, jobject, jlong);

JNIEXPORT jstring JNICALL Java_pleocmd_api_ExpressionParser_getInstructions(JNIEnv *,
        jobject, jlong);

JNIEXPORT void JNICALL Java_pleocmd_api_ExpressionParser_freeHandle
(JNIEnv *, jobject, jlong);

#endif
