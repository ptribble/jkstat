/*
 * SPDX-License-Identifier: CDDL-1.0
 *
 * CDDL HEADER START
 *
 * This file and its contents are supplied under the terms of the
 * Common Development and Distribution License ("CDDL"), version 1.0.
 * You may only use this file in accordance with the terms of version
 * 1.0 of the CDDL.
 *
 * A full copy of the text of the CDDL should have accompanied this
 * source. A copy of the CDDL is also available via the Internet at
 * http://www.illumos.org/license/CDDL.
 *
 * CDDL HEADER END
 *
 * Copyright 2025 Peter Tribble
 *
 */

#include <jni.h>
#include <stdio.h>
#include <sys/types.h>
#include <string.h>
#include <fcntl.h>
#include <kstat.h>

/* for raw kstat support */
#include <nfs/nfs.h>
#include <nfs/nfs_clnt.h>
#include <sys/sysinfo.h>
#include <sys/var.h>
#include <sys/dnlc.h>

#include "uk_co_petertribble_jkstat_api_NativeJKstat.h"

#define LOADLONG(N,T,V) (*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, N, T, V)
#define LOADUINT32(N,V) nameobject = (*env)->NewStringUTF(env, N);(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_UINT32, V)
#define LOADUINT32PTR(N,V) nameobject = (*env)->NewStringUTF(env, #N);(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_UINT32, V->N)
#define LOADINT32PTR(N,V) nameobject = (*env)->NewStringUTF(env, #N);(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_INT32, V->N)
#define LOADUINT64(N,V) nameobject = (*env)->NewStringUTF(env, N);(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_UINT64, V)
#define LOADUINT64PTR(N,V) nameobject = (*env)->NewStringUTF(env, #N);(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_UINT64, V->N)
#define LOADINT64PTR(N,V) nameobject = (*env)->NewStringUTF(env, #N);(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_INT64, V->N)
#define LOADSTRPTR(N,V) nameobject = (*env)->NewStringUTF(env, #N);      dataobject = (*env)->NewStringUTF(env, V->N);      (*env)->CallVoidMethod(env, kstatobject, kso_adddata_mid, nameobject, KSTAT_DATA_CHAR, dataobject);

/*
 * Cache class and method IDs so we don't have to look
 * them up every time.
 */
/* NativeJKstat */
static jclass jks_class;
static jmethodID jks_addkstat_mid;

/* Kstat */
static jclass kso_class;
static jmethodID kso_constructor_mid;
static jmethodID kso_adddata_mid;
static jmethodID kso_addlong_mid;
static jmethodID kso_setinfo_mid;
static jmethodID kso_insertiodata_mid;

static boolean_t jks_ids_cached;
static kstat_ctl_t *kc;

static void
jks_throw(JNIEnv *jenv, jclass jc, const char *fmt, va_list *ap)
{
	char msg[1024];
	(void) vsnprintf(msg, sizeof (msg), fmt, *ap);
	(*jenv)->ThrowNew(jenv, jc, msg);
}

static void
jks_throw_illegal_state(JNIEnv *jenv, const char *fmt, ...)
{
	va_list ap;
	jclass jc = (*jenv)->FindClass(jenv,
	    "java/lang/IllegalStateException");
	va_start(ap, fmt);
	jks_throw(jenv, jc, fmt, &ap);
	(*jenv)->DeleteLocalRef(jenv, jc);
	va_end(ap);
}

JNIEXPORT jobject JNICALL Java_uk_co_petertribble_jkstat_api_NativeJKstat_getKstatObject
  (JNIEnv *env, jobject jobj, jstring jmodule, jint jinst, jstring jname)
{
  kstat_t *ks;
  kstat_named_t *kn;
  kstat_io_t *kiot;
  kstat_intr_t *kintrt;
  const char *kmodule;
  const char *kname;
  int itype;
  jobject kstatobject = NULL;
  jobject dataobject;
  jstring nameobject;
  int n;
  char dchar[17];
  char *dcharptr;
  dcharptr = dchar;

  kname = (*env)->GetStringUTFChars(env,jname,NULL);
  kmodule = (*env)->GetStringUTFChars(env,jmodule,NULL);

  ks = kstat_lookup(kc, (char *)kmodule, (int) jinst, (char *)kname);
  if (ks == NULL) {
    (*env)->ReleaseStringUTFChars(env, jname, kname);
    (*env)->ReleaseStringUTFChars(env, jmodule, kmodule);
    return(NULL);
  }

  /*
   * It would be nice if we could return something different at this point
   * as there are kstats that exist in the chain but have no data. Or perhaps
   * just return a new kstat with the type information filled in but no
   * data.
   */
  if (kstat_read(kc,ks,0) == -1) {
    (*env)->ReleaseStringUTFChars(env, jname, kname);
    (*env)->ReleaseStringUTFChars(env, jmodule, kmodule);
    return(NULL);
  }

  itype = ks->ks_type;

  kstatobject = (*env)->NewObject(env, kso_class, kso_constructor_mid, jmodule, jinst, jname);
  /*
   * insert the general data on this kstat
   */
  nameobject = (*env)->NewStringUTF(env, ks->ks_class);
  (*env)->CallVoidMethod(env, kstatobject, kso_setinfo_mid, nameobject, (jint) itype, (uint64_t)ks->ks_crtime, (uint64_t)ks->ks_snaptime);
  if (itype == KSTAT_TYPE_NAMED) {
    /*
     * Iterate through the data fields
     */
    for (n = ks->ks_ndata, kn = KSTAT_NAMED_PTR(ks); n > 0; n--, kn++) {
      /*
       * should switch on the data type
       * but fake garbage for now
       */
      nameobject = (*env)->NewStringUTF(env, kn->name);
      switch (kn->data_type) {
      case KSTAT_DATA_INT32:
	(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, kn->data_type, (int64_t)kn->value.i32);
	break;
      case KSTAT_DATA_UINT32:
	(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, kn->data_type, (uint64_t)kn->value.ui32);
	break;
      case KSTAT_DATA_INT64:
	(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, kn->data_type, kn->value.i64);
	break;
      case KSTAT_DATA_UINT64:
	(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, kn->data_type, kn->value.ui64);
	break;
      case KSTAT_DATA_STRING:
	if (KSTAT_NAMED_STR_PTR(kn) == NULL) {
	  dataobject = (*env)->NewStringUTF(env, "null");
	} else {
	  dataobject = (*env)->NewStringUTF(env, KSTAT_NAMED_STR_PTR(kn));
	}
	(*env)->CallVoidMethod(env, kstatobject, kso_adddata_mid, nameobject, kn->data_type, dataobject);
	break;
      case KSTAT_DATA_CHAR:
	strlcpy(dcharptr, kn->value.c, 16);
	dataobject = (*env)->NewStringUTF(env, dchar);
	(*env)->CallVoidMethod(env, kstatobject, kso_adddata_mid, nameobject, kn->data_type, dataobject);
	break;
      default:
	dataobject = (*env)->NewStringUTF(env, "junk");
	(*env)->CallVoidMethod(env, kstatobject, kso_adddata_mid, nameobject, kn->data_type, dataobject);
      }
    }
  }

  if (itype == KSTAT_TYPE_IO) {
    /*
     * Expose the kstat_io_t structure as a hash
     */
    /*
     * u_longlong_t nread,nwritten (bytes)
     * uint_t reads,writes (operations)
     * hrtime_t = int64_t times
     * the java method expects all values as long
     */
    kiot = KSTAT_IO_PTR(ks);
    (*env)->CallVoidMethod(env, kstatobject, kso_insertiodata_mid, (int64_t)kiot->nread, (int64_t)kiot->nwritten, (uint64_t)kiot->reads, (uint64_t)kiot->writes, (int64_t)kiot->wtime, (int64_t)kiot->wlentime, (int64_t)kiot->wlastupdate, (int64_t)kiot->rtime, (int64_t)kiot->rlentime, (int64_t)kiot->rlastupdate, (int64_t)kiot->wcnt, (int64_t)kiot->rcnt);
  }

  if (itype == KSTAT_TYPE_INTR) {
    /*
     * early attempt at support. This is pretty easy as I just expose
     * the kstat_intr_t array as a hash. Names chosen to be the same as
     * those used by the perl kstat command.
     */
    kintrt = KSTAT_INTR_PTR(ks);
    nameobject = (*env)->NewStringUTF(env, "hard");
    (*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_UINT32, (uint64_t)kintrt->intrs[KSTAT_INTR_HARD]);
    nameobject = (*env)->NewStringUTF(env, "soft");
    (*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_UINT32, (uint64_t)kintrt->intrs[KSTAT_INTR_SOFT]);
    nameobject = (*env)->NewStringUTF(env, "watchdog");
    (*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_UINT32, (uint64_t)kintrt->intrs[KSTAT_INTR_WATCHDOG]);
    nameobject = (*env)->NewStringUTF(env, "spurious");
    (*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_UINT32, (uint64_t)kintrt->intrs[KSTAT_INTR_SPURIOUS]);
    nameobject = (*env)->NewStringUTF(env, "multiple_service");
    (*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_UINT32, (uint64_t)kintrt->intrs[KSTAT_INTR_MULTSVC]);
  }

  if (itype == KSTAT_TYPE_RAW) {
    /*
     * This is messy, as each raw kstat must be done manually.
     * The kstat browser shows the most popular kstats.
     *
     * The following are implemented:
     *
     * unix:*:var
     * nfs:*:mntinfo
     * cpustat::
     * unix:*:ncstats
     * unix:*:sysinfo
     * unix:*:vminfo
     * mm:*:phys_installed
     *
     * These kstats are ignored, generally for the same reason
     * as the perl implementation, namely that the data is bogus:
     *
     * unix:*:sfmmu_percpu_stat
     * ufs directio:*:UFS DirectIO Stats
     * sockfs:*:sock_unix_list
     *
     * These kstats are on the TODO list:
     *
     * unix:*:kstat_headers
     */
    if (!strcmp(kmodule,"unix")) {
      if (!strcmp(kname,"var")) {
	struct var *varp;
	varp = (struct var *)(ks->ks_data);
	LOADINT32PTR(v_buf, (uint64_t)varp);
	LOADINT32PTR(v_call, (uint64_t)varp);
	LOADINT32PTR(v_proc, (uint64_t)varp);
	LOADINT32PTR(v_maxupttl, (uint64_t)varp);
	LOADINT32PTR(v_nglobpris, (uint64_t)varp);
	LOADINT32PTR(v_maxsyspri, (uint64_t)varp);
	LOADINT32PTR(v_clist, (uint64_t)varp);
	LOADINT32PTR(v_maxup, (uint64_t)varp);
	LOADINT32PTR(v_hbuf, (uint64_t)varp);
	LOADINT32PTR(v_hmask, (uint64_t)varp);
	LOADINT32PTR(v_pbuf, (uint64_t)varp);
	LOADINT32PTR(v_sptmap, (uint64_t)varp);
	LOADINT32PTR(v_maxpmem, (uint64_t)varp);
	LOADINT32PTR(v_autoup, (uint64_t)varp);
	LOADINT32PTR(v_bufhwm, (uint64_t)varp);
      }
      if (!strcmp(kname,"ncstats")) {
	struct ncstats *ncstatsp;
	ncstatsp = (struct ncstats *)(ks->ks_data);
	LOADINT32PTR(hits, (uint64_t)ncstatsp);
	LOADINT32PTR(misses, (uint64_t)ncstatsp);
	LOADINT32PTR(enters, (uint64_t)ncstatsp);
	LOADINT32PTR(dbl_enters, (uint64_t)ncstatsp);
	LOADINT32PTR(long_enter, (uint64_t)ncstatsp);
	LOADINT32PTR(long_look, (uint64_t)ncstatsp);
	LOADINT32PTR(move_to_front, (uint64_t)ncstatsp);
	LOADINT32PTR(purges, (uint64_t)ncstatsp);
      }
      if (!strcmp(kname,"sysinfo")) {
	sysinfo_t *sysinfop;
	sysinfop = (sysinfo_t *)(ks->ks_data);
	LOADUINT32PTR(updates, (uint64_t)sysinfop);
	LOADUINT32PTR(runque, (uint64_t)sysinfop);
	LOADUINT32PTR(runocc, (uint64_t)sysinfop);
	LOADUINT32PTR(swpque, (uint64_t)sysinfop);
	LOADUINT32PTR(swpocc, (uint64_t)sysinfop);
	LOADUINT32PTR(waiting, (uint64_t)sysinfop);
      }
      /*
       * this one requires 64-bit types
       */
      if (!strcmp(kname,"vminfo")) {
	vminfo_t *vminfop;
	vminfop = (vminfo_t *)(ks->ks_data);
	LOADUINT64PTR(freemem, (uint64_t)vminfop);
	LOADUINT64PTR(swap_resv, (uint64_t)vminfop);
	LOADUINT64PTR(swap_alloc, (uint64_t)vminfop);
	LOADUINT64PTR(swap_avail, (uint64_t)vminfop);
	LOADUINT64PTR(swap_free, (uint64_t)vminfop);
	/*
	 * Appeared in a Solaris 10 update
	 * LOADUINT64PTR(updates, (uint64_t)vminfop);
	 */
      }
    }
    if ((!strcmp(kmodule,"nfs"))&&(!strcmp(kname,"mntinfo"))) {
      struct mntinfo_kstat *mntinfop;
      mntinfop = (struct mntinfo_kstat *)(ks->ks_data);
      LOADSTRPTR(mik_proto, mntinfop);
      LOADUINT32PTR(mik_vers, (uint64_t)mntinfop);
      LOADUINT32PTR(mik_flags, (uint64_t)mntinfop);
      LOADUINT32PTR(mik_secmod, (uint64_t)mntinfop);
      LOADUINT32PTR(mik_curread, (uint64_t)mntinfop);
      LOADUINT32PTR(mik_curwrite, (uint64_t)mntinfop);
      LOADINT32PTR(mik_timeo, (int64_t)mntinfop);
      LOADINT32PTR(mik_retrans, (int64_t)mntinfop);
      LOADUINT32PTR(mik_acregmin, (uint64_t)mntinfop);
      LOADUINT32PTR(mik_acregmax, (uint64_t)mntinfop);
      LOADUINT32PTR(mik_acdirmin, (uint64_t)mntinfop);
      LOADUINT32PTR(mik_acdirmax, (uint64_t)mntinfop);
      LOADUINT32PTR(mik_noresponse, (uint64_t)mntinfop);
      LOADUINT32PTR(mik_failover, (uint64_t)mntinfop);
      LOADUINT32PTR(mik_remap, (uint64_t)mntinfop);
      LOADSTRPTR(mik_curserver, mntinfop);
      LOADUINT32("lookup_srtt", (uint64_t)mntinfop->mik_timers[0].srtt);
      LOADUINT32("lookup_deviate", (uint64_t)mntinfop->mik_timers[0].deviate);
      LOADUINT32("lookup_rtxcur", (uint64_t)mntinfop->mik_timers[0].rtxcur);
      LOADUINT32("read_srtt", (uint64_t)mntinfop->mik_timers[1].srtt);
      LOADUINT32("read_deviate", (uint64_t)mntinfop->mik_timers[1].deviate);
      LOADUINT32("read_rtxcur", (uint64_t)mntinfop->mik_timers[1].rtxcur);
      LOADUINT32("write_srtt", (uint64_t)mntinfop->mik_timers[2].srtt);
      LOADUINT32("write_deviate", (uint64_t)mntinfop->mik_timers[2].deviate);
      LOADUINT32("write_rtxcur", (uint64_t)mntinfop->mik_timers[2].rtxcur);
    }
    if (!strcmp(kmodule,"cpu_stat")) {
      cpu_stat_t    *statp;
      cpu_sysinfo_t *sysinfop;
      cpu_syswait_t *syswaitp;
      cpu_vminfo_t  *vminfop;
      statp = (cpu_stat_t *)(ks->ks_data);
      sysinfop = &statp->cpu_sysinfo;
      syswaitp = &statp->cpu_syswait;
      vminfop  = &statp->cpu_vminfo;
      LOADUINT32("idle", (uint64_t)sysinfop->cpu[CPU_IDLE]);
      LOADUINT32("user", (uint64_t)sysinfop->cpu[CPU_USER]);
      LOADUINT32("kernel", (uint64_t)sysinfop->cpu[CPU_KERNEL]);
      LOADUINT32("wait", (uint64_t)sysinfop->cpu[CPU_WAIT]);
      LOADUINT32("wait_io", (uint64_t)sysinfop->wait[W_IO]);
      LOADUINT32("wait_swap", (uint64_t)sysinfop->wait[W_SWAP]);
      LOADUINT32("wait_pio", (uint64_t)sysinfop->wait[W_PIO]);
      LOADUINT32PTR(bread, (uint64_t)sysinfop);
      LOADUINT32PTR(bwrite, (uint64_t)sysinfop);
      LOADUINT32PTR(lread, (uint64_t)sysinfop);
      LOADUINT32PTR(lwrite, (uint64_t)sysinfop);
      LOADUINT32PTR(phread, (uint64_t)sysinfop);
      LOADUINT32PTR(phwrite, (uint64_t)sysinfop);
      LOADUINT32PTR(pswitch, (uint64_t)sysinfop);
      LOADUINT32PTR(trap, (uint64_t)sysinfop);
      LOADUINT32PTR(intr, (uint64_t)sysinfop);
      LOADUINT32PTR(syscall, (uint64_t)sysinfop);
      LOADUINT32PTR(sysread, (uint64_t)sysinfop);
      LOADUINT32PTR(syswrite, (uint64_t)sysinfop);
      LOADUINT32PTR(sysfork, (uint64_t)sysinfop);
      LOADUINT32PTR(sysvfork, (uint64_t)sysinfop);
      LOADUINT32PTR(sysexec, (uint64_t)sysinfop);
      LOADUINT32PTR(readch, (uint64_t)sysinfop);
      LOADUINT32PTR(writech, (uint64_t)sysinfop);
      /* 3 unused entries (rcvint, xmtint, mdmint) skipped */
      LOADUINT32PTR(rawch, (uint64_t)sysinfop);
      LOADUINT32PTR(canch, (uint64_t)sysinfop);
      LOADUINT32PTR(outch, (uint64_t)sysinfop);
      LOADUINT32PTR(msg, (uint64_t)sysinfop);
      LOADUINT32PTR(sema, (uint64_t)sysinfop);
      LOADUINT32PTR(namei, (uint64_t)sysinfop);
      LOADUINT32PTR(ufsiget, (uint64_t)sysinfop);
      LOADUINT32PTR(ufsdirblk, (uint64_t)sysinfop);
      LOADUINT32PTR(ufsipage, (uint64_t)sysinfop);
      LOADUINT32PTR(ufsinopage, (uint64_t)sysinfop);
      LOADUINT32PTR(inodeovf, (uint64_t)sysinfop);
      LOADUINT32PTR(fileovf, (uint64_t)sysinfop);
      LOADUINT32PTR(procovf, (uint64_t)sysinfop);
      LOADUINT32PTR(intrthread, (uint64_t)sysinfop);
      LOADUINT32PTR(intrblk, (uint64_t)sysinfop);
      LOADUINT32PTR(idlethread, (uint64_t)sysinfop);
      LOADUINT32PTR(inv_swtch, (uint64_t)sysinfop);
      LOADUINT32PTR(nthreads, (uint64_t)sysinfop);
      LOADUINT32PTR(cpumigrate, (uint64_t)sysinfop);
      LOADUINT32PTR(xcalls, (uint64_t)sysinfop);
      LOADUINT32PTR(mutex_adenters, (uint64_t)sysinfop);
      LOADUINT32PTR(rw_rdfails, (uint64_t)sysinfop);
      LOADUINT32PTR(rw_wrfails, (uint64_t)sysinfop);
      LOADUINT32PTR(modload, (uint64_t)sysinfop);
      LOADUINT32PTR(modunload, (uint64_t)sysinfop);
      LOADUINT32PTR(bawrite, (uint64_t)sysinfop);
      /* remaining entries skipped */
      LOADUINT32PTR(iowait, (uint64_t)syswaitp);
      /* 2 unused entries (swap, physio) skipped */
      LOADUINT32PTR(pgrec, (uint64_t)vminfop);
      LOADUINT32PTR(pgfrec, (uint64_t)vminfop);
      LOADUINT32PTR(pgin, (uint64_t)vminfop);
      LOADUINT32PTR(pgpgin, (uint64_t)vminfop);
      LOADUINT32PTR(pgout, (uint64_t)vminfop);
      LOADUINT32PTR(pgpgout, (uint64_t)vminfop);
      LOADUINT32PTR(swapin, (uint64_t)vminfop);
      LOADUINT32PTR(pgswapin, (uint64_t)vminfop);
      LOADUINT32PTR(swapout, (uint64_t)vminfop);
      LOADUINT32PTR(pgswapout, (uint64_t)vminfop);
      LOADUINT32PTR(zfod, (uint64_t)vminfop);
      LOADUINT32PTR(dfree, (uint64_t)vminfop);
      LOADUINT32PTR(scan, (uint64_t)vminfop);
      LOADUINT32PTR(rev, (uint64_t)vminfop);
      LOADUINT32PTR(hat_fault, (uint64_t)vminfop);
      LOADUINT32PTR(as_fault, (uint64_t)vminfop);
      LOADUINT32PTR(maj_fault, (uint64_t)vminfop);
      LOADUINT32PTR(cow_fault, (uint64_t)vminfop);
      LOADUINT32PTR(prot_fault, (uint64_t)vminfop);
      LOADUINT32PTR(softlock, (uint64_t)vminfop);
      LOADUINT32PTR(kernel_asflt, (uint64_t)vminfop);
      LOADUINT32PTR(pgrrun, (uint64_t)vminfop);
      LOADUINT32PTR(execpgin, (uint64_t)vminfop);
      LOADUINT32PTR(execpgout, (uint64_t)vminfop);
      LOADUINT32PTR(execfree, (uint64_t)vminfop);
      LOADUINT32PTR(anonpgin, (uint64_t)vminfop);
      LOADUINT32PTR(anonpgout, (uint64_t)vminfop);
      LOADUINT32PTR(anonfree, (uint64_t)vminfop);
      LOADUINT32PTR(fspgin, (uint64_t)vminfop);
      LOADUINT32PTR(fspgout, (uint64_t)vminfop);
      LOADUINT32PTR(fsfree, (uint64_t)vminfop);
    }
    if ((!strcmp(kmodule,"mm"))&&(!strcmp(kname,"phys_installed"))) {
      struct memunit {
	uint64_t address;
	uint64_t size;
      } *kspmem;
      char intstr[32];
      kspmem = (struct memunit *)(ks->ks_data);
      /*
       * Use the address as the key in the hash, and the size as the value.
       */
      for (n = ks->ks_ndata; n > 0; n--) {
	sprintf(intstr, "%ld", kspmem->address);
	nameobject = (*env)->NewStringUTF(env, intstr);
	(*env)->CallVoidMethod(env, kstatobject, kso_addlong_mid, nameobject, KSTAT_DATA_UINT64, kspmem->size);
	kspmem++;
      }
    }
  }

  /*
   * clean up
   */
  (*env)->ReleaseStringUTFChars(env, jname, kname);
  (*env)->ReleaseStringUTFChars(env, jmodule, kmodule);

  return (kstatobject);
}

JNIEXPORT jint JNICALL Java_uk_co_petertribble_jkstat_api_NativeJKstat_getKCID
  (JNIEnv *env, jobject jobj)
{
  kid_t chainid;
  (void) kstat_chain_update(kc);
  chainid = kc->kc_chain_id;
  return (int) chainid;
}

JNIEXPORT jint JNICALL Java_uk_co_petertribble_jkstat_api_NativeJKstat_enumerate
   (JNIEnv *env, jobject jobj)
{
  kstat_t *ks;
  jstring jmodule;
  jstring jname;
  jstring nameobject;
  int inst;
  kid_t chainid;

  (void) kstat_chain_update(kc);

  for (ks = kc->kc_chain; ks != 0; ks = ks->ks_next) {
    jmodule = (*env)->NewStringUTF(env, ks->ks_module);
    inst = ks->ks_instance;
    jname = (*env)->NewStringUTF(env, ks->ks_name);
    nameobject = (*env)->NewStringUTF(env, ks->ks_class);
    (*env)->CallVoidMethod(env, jobj, jks_addkstat_mid, jmodule, inst,
	jname, nameobject, (jint) ks->ks_type, (uint64_t) ks->ks_crtime);
    (*env)->DeleteLocalRef(env, jmodule);
    (*env)->DeleteLocalRef(env, jname);
    (*env)->DeleteLocalRef(env, nameobject);
  }

  chainid = kc->kc_chain_id;
  return (int) chainid;
}

JNIEXPORT void JNICALL Java_uk_co_petertribble_jkstat_api_NativeJKstat_cacheids
   (JNIEnv *env, jclass class)
{
  jclass class_lref;

  if (jks_ids_cached) {
    /*
     * Cached ids include a global reference to the NativeJKstat class,
     * preventing the class from being unloaded. The NativeJKstat static
     * initializer should never execute more than once.
     */
    jks_throw_illegal_state(env, "class ids already cached");
    return;
  }

  class_lref = (*env)->FindClass(env, "uk/co/petertribble/jkstat/api/NativeJKstat");
  jks_class = (*env)->NewGlobalRef(env, class_lref);
  jks_addkstat_mid = (*env)->GetMethodID(env, jks_class, "addKstat",
	    "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;IJ)V");

  class_lref = (*env)->FindClass(env, "uk/co/petertribble/jkstat/api/Kstat");
  kso_class = (*env)->NewGlobalRef(env, class_lref);
  kso_constructor_mid = (*env)->GetMethodID(env, kso_class, "<init>",
	    "(Ljava/lang/String;ILjava/lang/String;)V");
  kso_adddata_mid = (*env)->GetMethodID(env, kso_class, "addDataObject",
	    "(Ljava/lang/String;ILjava/lang/Object;)V");
  kso_addlong_mid = (*env)->GetMethodID(env, kso_class, "addLongData",
	    "(Ljava/lang/String;IJ)V");
  kso_insertiodata_mid = (*env)->GetMethodID(env, kso_class, "insertIOData",
	    "(JJJJJJJJJJJJ)V");
  kso_setinfo_mid = (*env)->GetMethodID(env, kso_class, "setStandardInfo",
	    "(Ljava/lang/String;IJJ)V");

  kc = kstat_open();
  if (!kc) {
    perror("kstat_open");
  }

  jks_ids_cached = B_TRUE;
}
