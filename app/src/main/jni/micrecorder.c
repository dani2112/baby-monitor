#include <jni.h>
#include <stddef.h>
#include <math.h>

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

JNIEXPORT jfloat JNICALL
Java_de_dk_1s_babymonitor_monitoring_BabyVoiceMonitor_computeNoiseLevel(JNIEnv *env,
                                                                        jobject instance,
                                                                        jshortArray audioData_,
                                                                        jint elementCount) {
    jshort *audioData = (*env)->GetShortArrayElements(env, audioData_, NULL);

    long long overallLevel = 0;
    int i;
    for(i = 0; i < elementCount; i++) {
        overallLevel = overallLevel + audioData[i] * audioData[i];
    }
    (*env)->ReleaseShortArrayElements(env, audioData_, audioData, 0);
    return sqrtf(overallLevel / (float)elementCount);
}