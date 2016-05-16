#include <jni.h>
#include <stddef.h>

JNIEXPORT void JNICALL
Java_de_dk_1s_babymonitor_monitoring_MicRecorder_downsample16To8Bit(JNIEnv *env, jobject instance,
                                                                    jshortArray inputData_,
                                                                    jbyteArray outputData_,
                                                                    jint elementCount) {
    jshort *inputData = (*env)->GetShortArrayElements(env, inputData_, NULL);
    jbyte *outputData = (*env)->GetByteArrayElements(env, outputData_, NULL);

    int i;
    for(i = 0; i < elementCount; i++) {
        outputData[i] = inputData[i] >> 8;
    }

    (*env)->ReleaseShortArrayElements(env, inputData_, inputData, 0);
    (*env)->ReleaseByteArrayElements(env, outputData_, outputData, 0);
}