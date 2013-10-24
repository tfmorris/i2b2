#include <jni.h>
#include "jcluto_JClutoWrapper.h"
/* put includes for Cluto here */
#include "cluto.h"
#include <stdio.h>

  JNIEXPORT void Java_jcluto_JClutoWrapper_VP_1ClusterDirect
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint crfun, jint rowmodel, 
    jint colmodel, jfloat colprune, jint ntrials, jint niter, 
    jint seed, jint dbglvl, jint nparts, jintArray partA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;

    CLUTO_VP_ClusterDirect(
        nrows, ncols, rowptr, rowind, rowval, simfun, crfun, rowmodel, 
        colmodel, colprune, ntrials, niter, seed, dbglvl, nparts, part);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_VP_1ClusterRB
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint crfun, jint rowmodel, 
    jint colmodel, jfloat colprune, jint ntrials, jint niter, 
    jint seed, jint rbtype, jint kwayrefine, jint dbglvl, 
    jint nparts, jintArray partA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;

    CLUTO_VP_ClusterRB(
        nrows, ncols, rowptr, rowind, rowval, simfun, crfun, rowmodel, 
        colmodel, colprune, ntrials, niter, seed, rbtype, kwayrefine, dbglvl, 
        nparts, part);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_VP_1ClusterRBTree
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint crfun, jint rowmodel, 
    jint colmodel, jfloat colprune, jint ntrials, jint niter, 
    jint seed, jint dbglvl, jint nparts, jintArray partA, 
    jintArray ptreeA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;

    CLUTO_VP_ClusterRBTree(
        nrows, ncols, rowptr, rowind, rowval, simfun, crfun, rowmodel, 
        colmodel, colprune, ntrials, niter, seed, dbglvl, nparts, part, 
        ptree);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_VA_1Cluster
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint crfun, jint rowmodel, 
    jint colmodel, jfloat colprune, jint dbglvl, jint nparts, 
    jintArray partA, jintArray ptreeA, jfloatArray tsimsA, jfloatArray gainsA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;
    jfloat *tsims = tsimsA != NULL ? (*env)->GetFloatArrayElements(env, tsimsA, 0) : NULL;
    jfloat *gains = gainsA != NULL ? (*env)->GetFloatArrayElements(env, gainsA, 0) : NULL;

    CLUTO_VA_Cluster(
        nrows, ncols, rowptr, rowind, rowval, simfun, crfun, rowmodel, 
        colmodel, colprune, dbglvl, nparts, part, ptree, tsims, gains);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
    if (tsimsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, tsimsA, tsims, 0);
    if (gainsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, gainsA, gains, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_VA_1ClusterBiased
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint crfun, jint rowmodel, 
    jint colmodel, jfloat colprune, jint dbglvl, jint pnparts, 
    jint nparts, jintArray partA, jintArray ptreeA, jfloatArray tsimsA, 
    jfloatArray gainsA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;
    jfloat *tsims = tsimsA != NULL ? (*env)->GetFloatArrayElements(env, tsimsA, 0) : NULL;
    jfloat *gains = gainsA != NULL ? (*env)->GetFloatArrayElements(env, gainsA, 0) : NULL;

    CLUTO_VA_ClusterBiased(
        nrows, ncols, rowptr, rowind, rowval, simfun, crfun, rowmodel, 
        colmodel, colprune, dbglvl, pnparts, nparts, part, ptree, tsims, 
        gains);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
    if (tsimsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, tsimsA, tsims, 0);
    if (gainsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, gainsA, gains, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_SP_1ClusterRB
   (JNIEnv *env, jclass cls,
    jint nrows, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint crfun, jint ntrials, jint niter, jint seed, 
    jint cstype, jint kwayrefine, jint dbglvl, jint nparts, 
    jintArray partA) {

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;

    CLUTO_SP_ClusterRB(
        nrows, xadj, adjncy, adjwgt, crfun, ntrials, niter, seed, 
        cstype, kwayrefine, dbglvl, nparts, part);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_SP_1ClusterDirect
   (JNIEnv *env, jclass cls,
    jint nrows, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint crfun, jint ntrials, jint niter, jint seed, 
    jint dbglvl, jint nparts, jintArray partA) {

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;

    CLUTO_SP_ClusterDirect(
        nrows, xadj, adjncy, adjwgt, crfun, ntrials, niter, seed, 
        dbglvl, nparts, part);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_SA_1Cluster
   (JNIEnv *env, jclass cls,
    jint nvtxs, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint crfun, jint dbglvl, jint nparts, jintArray partA, 
    jintArray ptreeA, jfloatArray tsimsA, jfloatArray gainsA) {

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;
    jfloat *tsims = tsimsA != NULL ? (*env)->GetFloatArrayElements(env, tsimsA, 0) : NULL;
    jfloat *gains = gainsA != NULL ? (*env)->GetFloatArrayElements(env, gainsA, 0) : NULL;

    CLUTO_SA_Cluster(
        nvtxs, xadj, adjncy, adjwgt, crfun, dbglvl, nparts, part, 
        ptree, tsims, gains);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
    if (tsimsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, tsimsA, tsims, 0);
    if (gainsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, gainsA, gains, 0);
  }

  JNIEXPORT jint Java_jcluto_JClutoWrapper_VP_1GraphClusterRB
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint grmodel, jint nnbrs, jfloat edgeprune, 
    jfloat vtxprune, jint mincmp, jint ntrials, jint seed, 
    jint rbtype, jint dbglvl, jint nparts, jintArray partA, 
    jfloatArray r_crvalueA) {
    jint rval;

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jfloat *r_crvalue = r_crvalueA != NULL ? (*env)->GetFloatArrayElements(env, r_crvalueA, 0) : NULL;

    rval = 
    CLUTO_VP_GraphClusterRB(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, grmodel, nnbrs, edgeprune, vtxprune, mincmp, ntrials, seed, 
        rbtype, dbglvl, nparts, part, r_crvalue);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (r_crvalueA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, r_crvalueA, r_crvalue, 0);
    return rval;
  }

  JNIEXPORT jint Java_jcluto_JClutoWrapper_SP_1GraphClusterRB
   (JNIEnv *env, jclass cls,
    jint nvtxs, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint nnbrs, jfloat edgeprune, jfloat vtxprune, jint mincmp, 
    jint ntrials, jint seed, jint cstype, jint dbglvl, 
    jint nparts, jintArray partA, jfloatArray r_crvalueA) {
    jint rval;

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jfloat *r_crvalue = r_crvalueA != NULL ? (*env)->GetFloatArrayElements(env, r_crvalueA, 0) : NULL;

    rval = 
    CLUTO_SP_GraphClusterRB(
        nvtxs, xadj, adjncy, adjwgt, nnbrs, edgeprune, vtxprune, mincmp, 
        ntrials, seed, cstype, dbglvl, nparts, part, r_crvalue);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (r_crvalueA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, r_crvalueA, r_crvalue, 0);
    return rval;
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1GetGraph
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint grmodel, jint nnbrs, jint dbglvl, 
    jobjectArray r_xadjA, jobjectArray r_adjncyA, jobjectArray r_adjwgtA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    int *r_xadj;
    int *r_adjncy;
    float *r_adjwgt;

    CLUTO_V_GetGraph(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, grmodel, nnbrs, dbglvl, &r_xadj, &r_adjncy, &r_adjwgt);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (r_xadjA!= NULL) {
        int alen = nrows+1;
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_xadj);
        (*env)->SetObjectArrayElement(env, r_xadjA, 0, array);
    }
    if (r_adjncyA!= NULL) {
        int alen = r_xadj[nrows];
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_adjncy);
        (*env)->SetObjectArrayElement(env, r_adjncyA, 0, array);
    }
    if (r_adjwgtA!= NULL) {
        int alen = r_xadj[nrows];
        jfloatArray array = (jfloatArray)(*env)->NewFloatArray(env, alen );
        (*env)->SetFloatArrayRegion(env, (jfloatArray)array,(jsize)0, alen ,(jfloat*)r_adjwgt);
        (*env)->SetObjectArrayElement(env, r_adjwgtA, 0, array);
    }
    if (r_xadj != NULL) 
      free(r_xadj);
    if (r_adjncy != NULL) 
      free(r_adjncy);
    if (r_adjwgt != NULL) 
      free(r_adjwgt);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_S_1GetGraph
   (JNIEnv *env, jclass cls,
    jint nvtxs, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint grmodel, jint nnbrs, jint dbglvl, jobjectArray r_xadjA, 
    jobjectArray r_adjncyA, jobjectArray r_adjwgtA) {

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    int *r_xadj;
    int *r_adjncy;
    float *r_adjwgt;

    CLUTO_S_GetGraph(
        nvtxs, xadj, adjncy, adjwgt, grmodel, nnbrs, dbglvl, &r_xadj, 
        &r_adjncy, &r_adjwgt);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (r_xadjA!= NULL) {
        int alen = 1;
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_xadj);
        (*env)->SetObjectArrayElement(env, r_xadjA, 0, array);
    }
    if (r_adjncyA!= NULL) {
        int alen = 1;
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_adjncy);
        (*env)->SetObjectArrayElement(env, r_adjncyA, 0, array);
    }
    if (r_adjwgtA!= NULL) {
        int alen = 1;
        jfloatArray array = (jfloatArray)(*env)->NewFloatArray(env, alen );
        (*env)->SetFloatArrayRegion(env, (jfloatArray)array,(jsize)0, alen ,(jfloat*)r_adjwgt);
        (*env)->SetObjectArrayElement(env, r_adjwgtA, 0, array);
    }
    if (r_xadj != NULL) 
      free(r_xadj);
    if (r_adjncy != NULL) 
      free(r_adjncy);
    if (r_adjwgt != NULL) 
      free(r_adjwgt);
  }

  JNIEXPORT jfloat Java_jcluto_JClutoWrapper_V_1GetSolutionQuality
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint crfun, jint rowmodel, 
    jint colmodel, jfloat colprune, jint nparts, jintArray partA) {
    jfloat rval;

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;

    rval = 
    CLUTO_V_GetSolutionQuality(
        nrows, ncols, rowptr, rowind, rowval, simfun, crfun, rowmodel, 
        colmodel, colprune, nparts, part);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    return rval;
  }

  JNIEXPORT jfloat Java_jcluto_JClutoWrapper_S_1GetSolutionQuality
   (JNIEnv *env, jclass cls,
    jint nvtxs, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint crfun, jint nparts, jintArray partA) {
    jfloat rval;

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;

    rval = 
    CLUTO_S_GetSolutionQuality(
        nvtxs, xadj, adjncy, adjwgt, crfun, nparts, part);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    return rval;
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1GetClusterStats
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint nparts, jintArray partA, jintArray pwgtsA, 
    jfloatArray cintsimA, jfloatArray cintsdevA, jfloatArray izscoresA, jfloatArray cextsimA, 
    jfloatArray cextsdevA, jfloatArray ezscoresA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *pwgts = pwgtsA != NULL ? (*env)->GetIntArrayElements(env, pwgtsA, 0) : NULL;
    jfloat *cintsim = cintsimA != NULL ? (*env)->GetFloatArrayElements(env, cintsimA, 0) : NULL;
    jfloat *cintsdev = cintsdevA != NULL ? (*env)->GetFloatArrayElements(env, cintsdevA, 0) : NULL;
    jfloat *izscores = izscoresA != NULL ? (*env)->GetFloatArrayElements(env, izscoresA, 0) : NULL;
    jfloat *cextsim = cextsimA != NULL ? (*env)->GetFloatArrayElements(env, cextsimA, 0) : NULL;
    jfloat *cextsdev = cextsdevA != NULL ? (*env)->GetFloatArrayElements(env, cextsdevA, 0) : NULL;
    jfloat *ezscores = ezscoresA != NULL ? (*env)->GetFloatArrayElements(env, ezscoresA, 0) : NULL;

    CLUTO_V_GetClusterStats(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, nparts, part, pwgts, cintsim, cintsdev, izscores, cextsim, 
        cextsdev, ezscores);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (pwgtsA!= NULL)
        (*env)->ReleaseIntArrayElements(env, pwgtsA, pwgts, 0);
    if (cintsimA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, cintsimA, cintsim, 0);
    if (cintsdevA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, cintsdevA, cintsdev, 0);
    if (izscoresA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, izscoresA, izscores, 0);
    if (cextsimA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, cextsimA, cextsim, 0);
    if (cextsdevA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, cextsdevA, cextsdev, 0);
    if (ezscoresA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, ezscoresA, ezscores, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_S_1GetClusterStats
   (JNIEnv *env, jclass cls,
    jint nvtxs, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint nparts, jintArray partA, jintArray pwgtsA, jfloatArray cintsimA, 
    jfloatArray cintsdevA, jfloatArray izscoresA, jfloatArray cextsimA, jfloatArray cextsdevA, 
    jfloatArray ezscoresA) {

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *pwgts = pwgtsA != NULL ? (*env)->GetIntArrayElements(env, pwgtsA, 0) : NULL;
    jfloat *cintsim = cintsimA != NULL ? (*env)->GetFloatArrayElements(env, cintsimA, 0) : NULL;
    jfloat *cintsdev = cintsdevA != NULL ? (*env)->GetFloatArrayElements(env, cintsdevA, 0) : NULL;
    jfloat *izscores = izscoresA != NULL ? (*env)->GetFloatArrayElements(env, izscoresA, 0) : NULL;
    jfloat *cextsim = cextsimA != NULL ? (*env)->GetFloatArrayElements(env, cextsimA, 0) : NULL;
    jfloat *cextsdev = cextsdevA != NULL ? (*env)->GetFloatArrayElements(env, cextsdevA, 0) : NULL;
    jfloat *ezscores = ezscoresA != NULL ? (*env)->GetFloatArrayElements(env, ezscoresA, 0) : NULL;

    CLUTO_S_GetClusterStats(
        nvtxs, xadj, adjncy, adjwgt, nparts, part, pwgts, cintsim, 
        cintsdev, izscores, cextsim, cextsdev, ezscores);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (pwgtsA!= NULL)
        (*env)->ReleaseIntArrayElements(env, pwgtsA, pwgts, 0);
    if (cintsimA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, cintsimA, cintsim, 0);
    if (cintsdevA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, cintsdevA, cintsdev, 0);
    if (izscoresA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, izscoresA, izscores, 0);
    if (cextsimA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, cextsimA, cextsim, 0);
    if (cextsdevA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, cextsdevA, cextsdev, 0);
    if (ezscoresA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, ezscoresA, ezscores, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1GetClusterFeatures
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint nparts, jintArray partA, jint nfeatures, 
    jintArray internalidsA, jfloatArray internalwgtsA, jintArray externalidsA, jfloatArray externalwgtsA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *internalids = internalidsA != NULL ? (*env)->GetIntArrayElements(env, internalidsA, 0) : NULL;
    jfloat *internalwgts = internalwgtsA != NULL ? (*env)->GetFloatArrayElements(env, internalwgtsA, 0) : NULL;
    jint *externalids = externalidsA != NULL ? (*env)->GetIntArrayElements(env, externalidsA, 0) : NULL;
    jfloat *externalwgts = externalwgtsA != NULL ? (*env)->GetFloatArrayElements(env, externalwgtsA, 0) : NULL;

    CLUTO_V_GetClusterFeatures(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, nparts, part, nfeatures, internalids, internalwgts, externalids, externalwgts);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (internalidsA!= NULL)
        (*env)->ReleaseIntArrayElements(env, internalidsA, internalids, 0);
    if (internalwgtsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, internalwgtsA, internalwgts, 0);
    if (externalidsA!= NULL)
        (*env)->ReleaseIntArrayElements(env, externalidsA, externalids, 0);
    if (externalwgtsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, externalwgtsA, externalwgts, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1BuildTree
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint crfun, jint rowmodel, 
    jint colmodel, jfloat colprune, jint treetype, jint dbglvl, 
    jint nparts, jintArray partA, jintArray ptreeA, jfloatArray tsimsA, 
    jfloatArray gainsA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;
    jfloat *tsims = tsimsA != NULL ? (*env)->GetFloatArrayElements(env, tsimsA, 0) : NULL;
    jfloat *gains = gainsA != NULL ? (*env)->GetFloatArrayElements(env, gainsA, 0) : NULL;

    CLUTO_V_BuildTree(
        nrows, ncols, rowptr, rowind, rowval, simfun, crfun, rowmodel, 
        colmodel, colprune, treetype, dbglvl, nparts, part, ptree, tsims, 
        gains);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
    if (tsimsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, tsimsA, tsims, 0);
    if (gainsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, gainsA, gains, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_S_1BuildTree
   (JNIEnv *env, jclass cls,
    jint nvtxs, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint crfun, jint treetype, jint dbglvl, jint nparts, 
    jintArray partA, jintArray ptreeA, jfloatArray tsimsA, jfloatArray gainsA) {

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;
    jfloat *tsims = tsimsA != NULL ? (*env)->GetFloatArrayElements(env, tsimsA, 0) : NULL;
    jfloat *gains = gainsA != NULL ? (*env)->GetFloatArrayElements(env, gainsA, 0) : NULL;

    CLUTO_S_BuildTree(
        nvtxs, xadj, adjncy, adjwgt, crfun, treetype, dbglvl, nparts, 
        part, ptree, tsims, gains);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
    if (tsimsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, tsimsA, tsims, 0);
    if (gainsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, gainsA, gains, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1GetTreeStats
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint nparts, jintArray partA, jintArray ptreeA, 
    jintArray pwgtsA, jfloatArray cintsimA, jfloatArray cextsimA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;
    jint *pwgts = pwgtsA != NULL ? (*env)->GetIntArrayElements(env, pwgtsA, 0) : NULL;
    jfloat *cintsim = cintsimA != NULL ? (*env)->GetFloatArrayElements(env, cintsimA, 0) : NULL;
    jfloat *cextsim = cextsimA != NULL ? (*env)->GetFloatArrayElements(env, cextsimA, 0) : NULL;

/* fprintf(stderr,">>> CLUTO_V_GetTreeStats\n"); */
    CLUTO_V_GetTreeStats(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, nparts, part, ptree, pwgts, cintsim, cextsim);
/* fprintf(stderr,"<<< CLUTO_V_GetTreeStats\n"); */

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
    if (pwgtsA!= NULL)
        (*env)->ReleaseIntArrayElements(env, pwgtsA, pwgts, 0);
    if (cintsimA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, cintsimA, cintsim, 0);
    if (cextsimA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, cextsimA, cextsim, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1GetTreeFeatures
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint nparts, jintArray partA, jintArray ptreeA, 
    jint nfeatures, jintArray internalidsA, jfloatArray internalwgtsA, jintArray externalidsA, 
    jfloatArray externalwgtsA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;
    jint *internalids = internalidsA != NULL ? (*env)->GetIntArrayElements(env, internalidsA, 0) : NULL;
    jfloat *internalwgts = internalwgtsA != NULL ? (*env)->GetFloatArrayElements(env, internalwgtsA, 0) : NULL;
    jint *externalids = externalidsA != NULL ? (*env)->GetIntArrayElements(env, externalidsA, 0) : NULL;
    jfloat *externalwgts = externalwgtsA != NULL ? (*env)->GetFloatArrayElements(env, externalwgtsA, 0) : NULL;

    CLUTO_V_GetTreeFeatures(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, nparts, part, ptree, nfeatures, internalids, internalwgts, externalids, 
        externalwgts);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
    if (internalidsA!= NULL)
        (*env)->ReleaseIntArrayElements(env, internalidsA, internalids, 0);
    if (internalwgtsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, internalwgtsA, internalwgts, 0);
    if (externalidsA!= NULL)
        (*env)->ReleaseIntArrayElements(env, externalidsA, externalids, 0);
    if (externalwgtsA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, externalwgtsA, externalwgts, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_InternalizeMatrix
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jintArray partA, jintArray r_nrowsA, jintArray r_ncolsA, 
    jobjectArray r_rowptrA, jobjectArray r_rowindA, jobjectArray r_rowvalA, jobjectArray r_rimapA, 
    jobjectArray r_cimapA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *r_nrows = r_nrowsA != NULL ? (*env)->GetIntArrayElements(env, r_nrowsA, 0) : NULL;
    jint *r_ncols = r_ncolsA != NULL ? (*env)->GetIntArrayElements(env, r_ncolsA, 0) : NULL;
    int *r_rowptr;
    int *r_rowind;
    float *r_rowval;
    int *r_rimap;
    int *r_cimap;

/* fprintf(stderr,">>> CLUTO_InternalizeMatrix %d %d\n",nrows,ncols);  */

    CLUTO_InternalizeMatrix(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, part, r_nrows, r_ncols, &r_rowptr, &r_rowind, &r_rowval, 
        &r_rimap, &r_cimap);

/* fprintf(stderr,"<<< CLUTO_InternalizeMatrix %d %d\n", *r_nrows, *r_ncols);  */

    /* Copy arrays to java and release the holds on memory */
    if (r_rowptrA!= NULL) {
        int alen = *r_nrows+1;
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_rowptr);
        (*env)->SetObjectArrayElement(env, r_rowptrA, 0, array);
    }
    if (r_rowindA!= NULL) {
        int alen = r_rowptr[*r_nrows];
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_rowind);
        (*env)->SetObjectArrayElement(env, r_rowindA, 0, array);
    }
    if (r_rowvalA!= NULL) {
        int alen = r_rowptr[*r_nrows]; 
        jfloatArray array = (jfloatArray)(*env)->NewFloatArray(env, alen );
        (*env)->SetFloatArrayRegion(env, (jfloatArray)array,(jsize)0, alen ,(jfloat*)r_rowval);
        (*env)->SetObjectArrayElement(env, r_rowvalA, 0, array);
    }
    if (r_rimapA!= NULL) {
        int alen = nrows;
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_rimap);
        (*env)->SetObjectArrayElement(env, r_rimapA, 0, array);
    }
    if (r_cimapA!= NULL) {
        int alen = ncols;
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_cimap);
        (*env)->SetObjectArrayElement(env, r_cimapA, 0, array);
    }
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (r_nrowsA!= NULL)
        (*env)->ReleaseIntArrayElements(env, r_nrowsA, r_nrows, 0);
    if (r_ncolsA!= NULL)
        (*env)->ReleaseIntArrayElements(env, r_ncolsA, r_ncols, 0);
    if (r_rowptr != NULL) 
      free(r_rowptr); 
    if (r_rowind != NULL) 
      free(r_rowind); 
    if (r_rowval != NULL) 
      free(r_rowval); 
    if (r_rimap != NULL) 
      free(r_rimap); 
    if (r_cimap != NULL) 
      free(r_cimap); 
  }

#if(0)
  JNIEXPORT void Java_jcluto_JClutoWrapper_S_1TreeReorderInternal
   (JNIEnv *env, jclass cls,
    jint nrows, jintArray rwgtsA, jfloatArray smatA, jint memflag, 
    jint dbglvl, jintArray ptreeA, jobjectArray ftreeA) {

    /* Get the pointers to the actual array locations */
    jint *rwgts = rwgtsA != NULL ? (*env)->GetIntArrayElements(env, rwgtsA, 0) : NULL;
    jfloat *smat = smatA != NULL ? (*env)->GetFloatArrayElements(env, smatA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;
    jint **ftree = NULL;
    if (ftreeA != NULL) {
      int j;
      jsize alen = (*env)->GetArrayLength(env, ftreeA);
      ftree = (int **)calloc(alen,sizeof(int*));
      for(j=0; j<alen; j++) {
        jintArray iarray = (jintArray)(*env)->GetObjectArrayElement(env, ftreeA, j);
        if (iarray != NULL) {
          jsize ilen =  (*env)->GetArrayLength(env, iarray);
          ftree[j] = (int *)calloc(ilen,sizeof(int));
          (*env)->GetIntArrayRegion(env, iarray, 0, ilen, ftree[j]);
        } else {
/* fprintf(stderr,"!!! CLUTO_S_ClusterTreeReorder %d\n",j); */
          ftree[j] = (int *)calloc(2,sizeof(int));
          ftree[j][0] = -1;
          ftree[j][1] = -1;
        }
      }
    }

    CLUTO_S_TreeReorderInternal(
        nrows, rwgts, smat, memflag, dbglvl, ptree, ftree);

    /* Copy arrays to java and release the holds on memory */
    if (rwgtsA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rwgtsA, rwgts, 0);
    if (smatA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, smatA, smat, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);

    if (ftreeA!= NULL) {
      int j;
      jsize alen = (*env)->GetArrayLength(env, ftreeA);
      for(j=0; j<alen; j++) {
        jintArray iarray = (jintArray)(*env)->GetObjectArrayElement(env, ftreeA, j);
        if (iarray != NULL) {
          jsize ilen =  (*env)->GetArrayLength(env, iarray);
          if (ftree[j] != NULL) {
            (*env)->SetIntArrayRegion(env, iarray,(jsize)0, ilen ,(jint*)ftree[j]);
            free(ftree[j]);
          }
        }
      }
    }
    if (ftree != NULL)
      free(ftree);
  }
#endif

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1TreeReorder
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint dbglvl, jintArray ptreeA, jobjectArray ftreeA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;

    jint **ftree = NULL;
    if (ftreeA != NULL) {
      int j;
      jsize alen = (*env)->GetArrayLength(env, ftreeA);
      ftree = (int **)calloc(alen,sizeof(int*));
      for(j=0; j<alen; j++) {
        jintArray iarray = (jintArray)(*env)->GetObjectArrayElement(env, ftreeA, j);
        if (iarray != NULL) {
          jsize ilen =  (*env)->GetArrayLength(env, iarray);
          ftree[j] = (int *)calloc(ilen,sizeof(int));
          (*env)->GetIntArrayRegion(env, iarray, 0, ilen, ftree[j]);
        } else {
fprintf(stderr,"!!! CLUTO_V_TreeReorder %d\n",j);
          ftree[j] = (int *)calloc(2,sizeof(int));
          ftree[j][0] = -1;
          ftree[j][1] = -1;
        }
      }
    }

/* fprintf(stderr,">>> CLUTO_V_TreeReorder\n"); */
    CLUTO_V_TreeReorder(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, dbglvl, ptree, ftree);
/* fprintf(stderr,"<<< CLUTO_V_TreeReorder\n"); */

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
/* fprintf(stderr,"CLUTO_V_TreeReorder ftreeA\n"); */
    if (ftreeA!= NULL) {
      int j;
      jsize alen = (*env)->GetArrayLength(env, ftreeA);
      for(j=0; j<alen; j++) {
        jintArray iarray = (jintArray)(*env)->GetObjectArrayElement(env, ftreeA, j);
        if (iarray != NULL) {
          jsize ilen =  (*env)->GetArrayLength(env, iarray);
          if (ftree[j] != NULL) {
            (*env)->SetIntArrayRegion(env, iarray,(jsize)0, ilen ,(jint*)ftree[j]);
            free(ftree[j]);
          }
        }
      }
    }
    if (ftree != NULL)
      free(ftree);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_S_1TreeReorder
   (JNIEnv *env, jclass cls,
    jint nvtxs, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint dbglvl, jintArray ptreeA, jobjectArray ftreeA) {

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;

    jint **ftree = NULL;
    if (ftreeA != NULL) {
      int j;
      jsize alen = (*env)->GetArrayLength(env, ftreeA);
      ftree = (int **)calloc(alen,sizeof(int*));
      for(j=0; j<alen; j++) {
        jintArray iarray = (jintArray)(*env)->GetObjectArrayElement(env, ftreeA, j);
        if (iarray != NULL) {
          jsize ilen =  (*env)->GetArrayLength(env, iarray);
          ftree[j] = (int *)calloc(ilen,sizeof(int));
          (*env)->GetIntArrayRegion(env, iarray, 0, ilen, ftree[j]);
        } else {
fprintf(stderr,"!!! CLUTO_S_TreeReorder %d\n",j);
          ftree[j] = (int *)calloc(2,sizeof(int));
          ftree[j][0] = -1;
          ftree[j][1] = -1;
        }
      }
    }

    CLUTO_S_TreeReorder(
        nvtxs, xadj, adjncy, adjwgt, dbglvl, ptree, ftree);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
    if (ftreeA!= NULL) {
      int j;
      jsize alen = (*env)->GetArrayLength(env, ftreeA);
      for(j=0; j<alen; j++) {
        jintArray iarray = (jintArray)(*env)->GetObjectArrayElement(env, ftreeA, j);
        if (iarray != NULL) {
          jsize ilen =  (*env)->GetArrayLength(env, iarray);
          if (ftree[j] != NULL) {
            (*env)->SetIntArrayRegion(env, iarray,(jsize)0, ilen ,(jint*)ftree[j]);
            free(ftree[j]);
          }
        }
      }
    }
    if (ftree != NULL)
      free(ftree);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1ClusterTreeReorder
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint dbglvl, jint nparts, jintArray partA, 
    jintArray ptreeA, jobjectArray ftreeA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;

    jint **ftree = NULL;
    if (ftreeA != NULL) {
      int j;
      jsize alen = (*env)->GetArrayLength(env, ftreeA);
      ftree = (int **)calloc(alen,sizeof(int*));
      for(j=0; j<alen; j++) {
        jintArray iarray = (jintArray)(*env)->GetObjectArrayElement(env, ftreeA, j);
        if (iarray != NULL) {
          jsize ilen =  (*env)->GetArrayLength(env, iarray);
          ftree[j] = (int *)calloc(ilen,sizeof(int));
          (*env)->GetIntArrayRegion(env, iarray, 0, ilen, ftree[j]);
        } else {
fprintf(stderr,"!!! CLUTO_V_ClusterTreeReorder %d\n",j);
          ftree[j] = (int *)calloc(2,sizeof(int));
          ftree[j][0] = -1;
          ftree[j][1] = -1;
        }
      }
    }

    CLUTO_V_ClusterTreeReorder(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, dbglvl, nparts, part, ptree, ftree);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
    if (ftreeA!= NULL) {
      int j;
      jsize alen = (*env)->GetArrayLength(env, ftreeA);
      for(j=0; j<alen; j++) {
        jintArray iarray = (jintArray)(*env)->GetObjectArrayElement(env, ftreeA, j);
        if (iarray != NULL) {
          jsize ilen =  (*env)->GetArrayLength(env, iarray);
          if (ftree[j] != NULL) {
            (*env)->SetIntArrayRegion(env, iarray,(jsize)0, ilen ,(jint*)ftree[j]);
            free(ftree[j]);
          }
        }
      }
    }
    if (ftree != NULL)
      free(ftree);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_S_1ClusterTreeReorder
   (JNIEnv *env, jclass cls,
    jint nvtxs, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint dbglvl, jint nparts, jintArray partA, jintArray ptreeA, 
    jobjectArray ftreeA) {

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *ptree = ptreeA != NULL ? (*env)->GetIntArrayElements(env, ptreeA, 0) : NULL;

    jint **ftree = NULL;
    if (ftreeA != NULL) {
      int j;
      jsize alen = (*env)->GetArrayLength(env, ftreeA);
      ftree = (int **)calloc(alen,sizeof(int*));
      for(j=0; j<alen; j++) {
        jintArray iarray = (jintArray)(*env)->GetObjectArrayElement(env, ftreeA, j);
        if (iarray != NULL) {
          jsize ilen =  (*env)->GetArrayLength(env, iarray);
          ftree[j] = (int *)calloc(ilen,sizeof(int));
          (*env)->GetIntArrayRegion(env, iarray, 0, ilen, ftree[j]);
        } else {
fprintf(stderr,"!!! CLUTO_S_ClusterTreeReorder %d\n",j);
          ftree[j] = (int *)calloc(2,sizeof(int));
          ftree[j][0] = -1;
          ftree[j][1] = -1;
        }
      }
    }

    CLUTO_S_ClusterTreeReorder(
        nvtxs, xadj, adjncy, adjwgt, dbglvl, nparts, part, ptree, 
        ftree);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (ptreeA!= NULL)
        (*env)->ReleaseIntArrayElements(env, ptreeA, ptree, 0);
    if (ftreeA!= NULL) {
      int j;
      jsize alen = (*env)->GetArrayLength(env, ftreeA);
      for(j=0; j<alen; j++) {
        jintArray iarray = (jintArray)(*env)->GetObjectArrayElement(env, ftreeA, j);
        if (iarray != NULL) {
          jsize ilen =  (*env)->GetArrayLength(env, iarray);
          if (ftree[j] != NULL) {
            (*env)->SetIntArrayRegion(env, iarray,(jsize)0, ilen ,(jint*)ftree[j]);
            free(ftree[j]);
          }
        }
      }
    }
    if (ftree != NULL)
      free(ftree);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1ReorderPartitions
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint nparts, jintArray partA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;

    CLUTO_V_ReorderPartitions(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, nparts, part);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_S_1ReorderPartitions
   (JNIEnv *env, jclass cls,
    jint nvtxs, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint nparts, jintArray partA) {

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;

    CLUTO_S_ReorderPartitions(
        nvtxs, xadj, adjncy, adjwgt, nparts, part);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1GetClusterDistanceMatrix
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint dbglvl, jint nparts, jintArray partA, 
    jfloatArray distmatA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jfloat *distmat = distmatA != NULL ? (*env)->GetFloatArrayElements(env, distmatA, 0) : NULL;

    CLUTO_V_GetClusterDistanceMatrix(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, dbglvl, nparts, part, distmat);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (distmatA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, distmatA, distmat, 0);

  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_S_1GetClusterDistanceMatrix
   (JNIEnv *env, jclass cls,
    jint nvtxs, jintArray xadjA, jintArray adjncyA, jfloatArray adjwgtA, 
    jint dbglvl, jint nparts, jintArray partA, jfloatArray distmatA) {

    /* Get the pointers to the actual array locations */
    jint *xadj = xadjA != NULL ? (*env)->GetIntArrayElements(env, xadjA, 0) : NULL;
    jint *adjncy = adjncyA != NULL ? (*env)->GetIntArrayElements(env, adjncyA, 0) : NULL;
    jfloat *adjwgt = adjwgtA != NULL ? (*env)->GetFloatArrayElements(env, adjwgtA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jfloat *distmat = distmatA != NULL ? (*env)->GetFloatArrayElements(env, distmatA, 0) : NULL;

    CLUTO_S_GetClusterDistanceMatrix(
        nvtxs, xadj, adjncy, adjwgt, dbglvl, nparts, part, distmat);

    /* Copy arrays to java and release the holds on memory */
    if (xadjA!= NULL)
        (*env)->ReleaseIntArrayElements(env, xadjA, xadj, 0);
    if (adjncyA!= NULL)
        (*env)->ReleaseIntArrayElements(env, adjncyA, adjncy, 0);
    if (adjwgtA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, adjwgtA, adjwgt, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (distmatA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, distmatA, distmat, 0);

  }

  JNIEXPORT void Java_jcluto_JClutoWrapper_V_1GetClusterSummaries
   (JNIEnv *env, jclass cls,
    jint nrows, jint ncols, jintArray rowptrA, jintArray rowindA, 
    jfloatArray rowvalA, jint simfun, jint rowmodel, jint colmodel, 
    jfloat colprune, jint nparts, jintArray partA, jint sumtype, 
    jint nfeatures, jintArray r_nsumA, jobjectArray r_spidA, jobjectArray r_swgtA, 
    jobjectArray r_sumptrA, jobjectArray r_sumindA) {

    /* Get the pointers to the actual array locations */
    jint *rowptr = rowptrA != NULL ? (*env)->GetIntArrayElements(env, rowptrA, 0) : NULL;
    jint *rowind = rowindA != NULL ? (*env)->GetIntArrayElements(env, rowindA, 0) : NULL;
    jfloat *rowval = rowvalA != NULL ? (*env)->GetFloatArrayElements(env, rowvalA, 0) : NULL;
    jint *part = partA != NULL ? (*env)->GetIntArrayElements(env, partA, 0) : NULL;
    jint *r_nsum = r_nsumA != NULL ? (*env)->GetIntArrayElements(env, r_nsumA, 0) : NULL;
    int *r_spid;
    float *r_swgt;
    int *r_sumptr;
    int *r_sumind;

    CLUTO_V_GetClusterSummaries(
        nrows, ncols, rowptr, rowind, rowval, simfun, rowmodel, colmodel, 
        colprune, nparts, part, sumtype, nfeatures, r_nsum, &r_spid, &r_swgt, 
        &r_sumptr, &r_sumind);

    /* Copy arrays to java and release the holds on memory */
    if (rowptrA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowptrA, rowptr, 0);
    if (rowindA!= NULL)
        (*env)->ReleaseIntArrayElements(env, rowindA, rowind, 0);
    if (rowvalA!= NULL)
        (*env)->ReleaseFloatArrayElements(env, rowvalA, rowval, 0);
    if (partA!= NULL)
        (*env)->ReleaseIntArrayElements(env, partA, part, 0);
    if (r_nsumA!= NULL)
        (*env)->ReleaseIntArrayElements(env, r_nsumA, r_nsum, 0);
    if (r_spidA!= NULL) {
        int alen = 1;
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_spid);
        (*env)->SetObjectArrayElement(env, r_spidA, 0, array);
    }
    if (r_swgtA!= NULL) {
        int alen = 1;
        jfloatArray array = (jfloatArray)(*env)->NewFloatArray(env, alen );
        (*env)->SetFloatArrayRegion(env, (jfloatArray)array,(jsize)0, alen ,(jfloat*)r_swgt);
        (*env)->SetObjectArrayElement(env, r_swgtA, 0, array);
    }
    if (r_sumptrA!= NULL) {
        int alen = 1;
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_sumptr);
        (*env)->SetObjectArrayElement(env, r_sumptrA, 0, array);
    }
    if (r_sumindA!= NULL) {
        int alen = 1;
        jintArray array = (jintArray)(*env)->NewIntArray(env, alen );
        (*env)->SetIntArrayRegion(env, (jintArray)array,(jsize)0, alen ,(jint*)r_sumind);
        (*env)->SetObjectArrayElement(env, r_sumindA, 0, array);
    }
    if (r_spid != NULL) 
      free(r_spid);
    if (r_swgt != NULL) 
      free(r_swgt);
    if (r_sumptr != NULL) 
      free(r_sumptr);
    if (r_sumind != NULL) 
      free(r_sumind);
  }

