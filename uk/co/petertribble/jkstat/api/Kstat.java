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

package uk.co.petertribble.jkstat.api;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import uk.co.petertribble.jkstat.util.NumericStringComparator;

/**
 * Represents a kstat, actually an implementation of kstat_t. The statistics
 * exposed by this kstat are stored in a {@code Map} with name of the
 * statistic as the key, and the value is a {@code KstatData} containing the
 * value of the statistic and its data type.
 *
 * @author Peter Tribble
 */
public class Kstat implements Serializable, Comparable<Kstat> {

    private static final long serialVersionUID = 1L;

    /**
     * The module of this Kstat.
     */
    private final String module;
    /**
     * The instance of this Kstat.
     */
    private final int instance;
    /**
     * The name of this Kstat.
     */
    private final String name;

    /*
     * The times are in nanoseconds and are of type hrtime_t, which is a
     * signed 64-bit integer, so a long is entirely adequate.
     */
    /**
     * The creation time of this Kstat.
     */
    private long crtime;
    /**
     * The snaptime of this Kstat.
     */
    private long snaptime;
    /**
     * The type of this Kstat.
     */
    private int type;
    /**
     * The class of this Kstat.
     */
    private String kstatClass;

    /*
     * The data is a {@code Map} of names and values. This is obvious for a
     * KSTAT_TYPE_NAMED kstat, which is an explicit set of name/value pairs.
     * Raw kstats are massaged to the same format. Interrupt and I/O kstats
     * have a standard mapping.
     */
    private final transient Map<String, KstatData> dataMap = new HashMap<>();

    /**
     * Creates a new {@code Kstat} of the given {@code module},
     * {@code instance}, and {@code name}.
     *
     * @param module the kstat module
     * @param instance the kstat instance
     * @param name the kstat name
     */
    public Kstat(String module, int instance, String name) {
	this.module = module;
	this.instance = instance;
	this.name = name;
    }

    /**
     * Inserts an item into the data Map.
     *
     * @param s the name of the data item
     * @param dataType the data type
     * @param data the actual data
     */
    private void addDataObject(String s, int dataType, Object data) {
	addDataObject(s, KstatData.Type.toType(dataType), data);
    }

    /**
     * Inserts an item into the data Map.
     *
     * @param s the name of the data item
     * @param dataType the data type
     * @param data the actual data
     */
    public void addDataObject(String s, KstatData.Type dataType, Object data) {
	dataMap.put(s, new KstatData(dataType, data));
    }

    /**
     * Convenience method to add a long to the data Map.
     *
     * @param s the name of the data item
     * @param dataType the data type
     * @param data the actual data
     */
    public void addLongData(String s, int dataType, long data) {
        addDataObject(s, KstatData.Type.toType(dataType), data);
    }

    /**
     * A shortcut for IO statistics, to avoid 12 separate calls.
     * Not to be used by applications.
     *
     * @param nread a long, number of bytes read
     * @param nwritten a long, number of bytes written
     * @param reads a long, number of read operations
     * @param writes a long, number of write operations
     * @param wtime a long, cumulative wait (pre-service) time
     * @param wlentime a long, cumulative wait length*time product
     * @param wlastupdate a long, last time wait queue changed
     * @param rtime a long, cumulative run (service) time
     * @param rlentime a long, cumulative run length*time product
     * @param rlastupdate a long, last time run queue changed
     * @param wcnt a long, count of elements in wait state
     * @param rcnt a long, count of elements in run state
     */
    public void insertIOData(long nread, long nwritten, long reads,
		long writes, long wtime, long wlentime,
		long wlastupdate, long rtime, long rlentime,
		long rlastupdate, long wcnt, long rcnt) {
	addDataObject("nread", KstatData.Type.KSTAT_DATA_UINT64, nread);
	addDataObject("nwritten", KstatData.Type.KSTAT_DATA_UINT64, nwritten);
	addDataObject("reads", KstatData.Type.KSTAT_DATA_UINT32, reads);
	addDataObject("writes", KstatData.Type.KSTAT_DATA_UINT32, writes);
	/*
	 * The kstat_io_t structure stores these as hrtime_t, which
	 * is a signed 64-bit integer.
	 */
	addDataObject("wtime", KstatData.Type.KSTAT_DATA_INT64, wtime);
	addDataObject("wlentime", KstatData.Type.KSTAT_DATA_INT64, wlentime);
	addDataObject("wlastupdate", KstatData.Type.KSTAT_DATA_INT64,
                wlastupdate);
	addDataObject("rtime", KstatData.Type.KSTAT_DATA_INT64, rtime);
	addDataObject("rlentime", KstatData.Type.KSTAT_DATA_INT64, rlentime);
	addDataObject("rlastupdate", KstatData.Type.KSTAT_DATA_INT64,
                rlastupdate);
	addDataObject("wcnt", KstatData.Type.KSTAT_DATA_UINT32, wcnt);
	addDataObject("rcnt", KstatData.Type.KSTAT_DATA_UINT32, rcnt);
    }

    /**
     * Returns whether the named statistic exists. Requires the data to have
     * been read first.
     *
     * @param s the name of the statistic to be checked
     *
     * @return true if the named statistic exists, false otherwise
     */
    public boolean hasStatistic(String s) {
	return dataMap.containsKey(s);
    }

    /**
     * Gets the value of the named statistic.
     *
     * @param s the name of the statistic to be retrieved
     *
     * @return the data, or null if the statistic doesn't exist
     */
    public Object getData(String s) {
	KstatData kd = dataMap.get(s);
	return (kd == null) ? null : kd.getData();
    }

    /**
     * Gets the value of the named statistic as a long.
     *
     * @param s the name of the statistic to be retrieved
     *
     * @return the data, or null if the statistic doesn't exist
     *
     * @throws IllegalArgumentException if the requested statistic isn't a
     * numeric type
     */
    public long longData(String s) {
	if (!isNumeric(s)) {
	    throw new IllegalArgumentException("Statistic not a number");
	}
	return (Long) getData(s);
    }

    /**
     * Returns whether this statistic is of a numeric type. If the statistic
     * has not yet been read, or does not exist, returns false.
     *
     * @param s the name of the statistic to be checked
     *
     * @return true if this statistic is known to be a numeric type
     */
    public boolean isNumeric(String s) {
	KstatData kd = dataMap.get(s);
	return (kd != null) && kd.isNumeric();
    }

    /**
     * Sets the standard data for this {@code Kstat}.
     *
     * @param kstatClass the kstat class of this {@code Kstat}
     * @param type the type of this {@code Kstat}
     * @param crtime the creation time of this {@code Kstat}
     * @param snaptime the snap time of this {@code Kstat}
     */
    public void setStandardInfo(String kstatClass, int type, long crtime,
		long snaptime) {
	this.kstatClass = kstatClass;
	this.type = type;
	this.crtime = crtime;
	this.snaptime = snaptime;
    }

    /**
     * Gets the kstat class of this {@code Kstat}.
     *
     * @return the class of this {@code Kstat}
     */
    public String getKstatClass() {
	return kstatClass;
    }

    /**
     * Gets the type of this {@code Kstat}.
     *
     * @return the type of this {@code Kstat}
     */
    public int getType() {
	return type;
    }

    /**
     * Gets the kstat type as a {@code String}.
     *
     * @return the symbolic name of the kstat type
     */
    public String getTypeAsString() {
	return KstatType.getTypeAsString(type);
    }

    /**
     * Gets the creation time of this {@code Kstat}.
     *
     * @return the time in nanoseconds at which this {@code Kstat} was created
     */
    public long getCrtime() {
	return crtime;
    }

    /**
     * Gets the snaptime of this kstat, the time in nanoseconds at which the
     * data snapshot was taken.
     *
     * @return the time in nanoseconds at which the data snapshot of this
     * {@code Kstat} was taken
     */
    public long getSnaptime() {
	return snaptime;
    }

    /**
     * Gets the age of this {@code Kstat}.
     *
     * @return the age of this {@code Kstat} in nanoseconds
     */
    public long getAge() {
	return snaptime - crtime;
    }

    /**
     * Gets the {@code Set} of statistic names of this kstat. The list is
     * sorted (backed by a {@code TreeSet}).
     *
     * @return a {@code Set} of {@code String}s comprising the names of the
     * statistics in this {@code Kstat}
     */
    public Set<String> statistics() {
	return new TreeSet<>(dataMap.keySet());
    }

    /**
     * Gets the module of this {@code Kstat}.
     *
     * @return the name of this {@code Kstat}'s module
     */
    public String getModule() {
	return module;
    }

    /**
     * Gets the instance of this {@code Kstat}.
     *
     * @return the instance of this {@code Kstat}, as an int
     */
    public int getInst() {
	return instance;
    }

    /**
     * Gets the instance of this {@code Kstat}, as a String.
     *
     * @return the instance of this {@code Kstat}, as a String
     */
    public String getInstance() {
	return Integer.toString(instance);
    }

    /**
     * Gets the name of this {@code Kstat}.
     *
     * @return the name of this {@code Kstat}
     */
    public String getName() {
	return name;
    }

    /**
     * Gets the full name of this {@code Kstat}, in the form
     * module:instance:name.
     *
     * @return the full name of this {@code Kstat}, in the form
     * module:instance:name
     */
    public String getTriplet() {
	StringBuilder buf = new StringBuilder();
	buf.append(module)
	    .append(':')
	    .append(instance)
	    .append(':')
	    .append(name);
	return buf.toString();
    }

    /**
     * Generate a JSON representation of this {@code Kstat}.
     *
     * @return a String containing a JSON representation of this {@code Kstat}
     */
    public String toJSON() {
	/*
	 * This is hand rolled. This minimizes dependencies and guarantees the
	 * representation stays fixed. Besides, generating JSON isn't hard.
	 */
	boolean firstdata = true;
	StringBuilder sb = new StringBuilder(148);
	// metadata
	sb.append("{\"class\":\"").append(kstatClass)
	    .append("\",\"type\":").append(type)
	    .append(",\"module\":\"").append(module)
	    .append("\",\"instance\":").append(instance)
	    .append(",\"name\":\"").append(name)
	    .append("\",\"crtime\":").append(crtime)
	    .append(",\"snaptime\":").append(snaptime)
	    .append(",\"data\":{");
	// data
	for (String s : dataMap.keySet()) {
	    if (firstdata) {
		firstdata = false;
	    } else {
		sb.append(',');
	    }
	    KstatData kd = dataMap.get(s);
	    sb.append('\"').append(s).append("\":");
	    if (kd.isNumeric()) {
		sb.append(kd.getData());
	    } else {
		sb.append('\"').append(kd.getData()).append('\"');
	    }
	}
	// end data
	sb.append("}}");
	return sb.toString();
    }

    /**
     * Returns a String representation of this {@code Kstat}. In fact, we
     * just return the name.
     *
     * @return the name of this {@code Kstat}
     */
    @Override
    public String toString() {
	return name;
    }

    /**
     * Returns whether the requested Object is equal to this {@code Kstat}.
     * Equality implies that the Object is of class Kstat and has the same
     * module, instance, and name. It does not imply that both were measured
     * at the same time or contain the same data.
     *
     * @param o the object to be tested for equality
     *
     * @return true if the object is a {@code Kstat} with the same identifying
     * triplet as this {@code Kstat}
     */
    @Override
    public boolean equals(Object o) {
	if (o instanceof Kstat) {
	    Kstat ks = (Kstat) o;
	    return module.equals(ks.getModule())
		&& (instance == ks.getInst())
		&& name.equals(ks.getName());
        }
        return false;
    }

    /**
     * Returns a hash code value for this {@code Kstat}.
     *
     * @return a hash code value for this {@code Kstat}
     */
    @Override
    public int hashCode() {
	int hash = 17;
	hash = (37 * hash) + module.hashCode();
	hash = (37 * hash) + instance;
	hash = (37 * hash) + name.hashCode();
	return hash;
    }

    /**
     * Compare with another {@code Kstat}.
     *
     * @param ks the {@code Kstat} to be compared
     *
     * @return the signed comparison of the supplied {@code Kstat} with this
     * {@code Kstat}
     */
    @Override
    public int compareTo(Kstat ks) {
	if (this == ks) {
	    return 0;
	}
	if (!module.equals(ks.getModule())) {
	    return module.compareTo(ks.getModule());
	}
	if (instance != ks.getInst()) {
	    return instance - ks.getInst();
	}
	return NumericStringComparator.getInstance().compare(name,
							ks.getName());
    }
}
