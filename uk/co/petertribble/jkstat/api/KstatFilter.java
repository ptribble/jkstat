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

import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;

/**
 * Kstats can be filtered by class, type, module, instance, name, and
 * statistic. There can be at most one instance of class or type specified.
 * If the class or type is unspecified, then kstats of all classes (or types)
 * will be returned.
 *
 * <p>Filtering by type is only likely to be useful for kstats of types
 * KSTAT_TYPE_IO and KSTAT_TYPE_INTR.
 *
 * @author Peter Tribble
 */
public class KstatFilter {

    private String filterClass;
    private Set<FilterQuartet> filterList;
    private Set<FilterQuartet> antiFilterList;
    private JKstat jkstat;
    // an invalid type, as an int cannot be null
    private int filterType = -1;

    /**
     * Create a kstat filter. The filter is initially empty, so that all kstats
     * will be returned.
     *
     * @param jkstat a JKstat object
     */
    public KstatFilter(final JKstat jkstat) {
	this.jkstat = jkstat;
	filterList = new HashSet<>();
	antiFilterList = new HashSet<>();
    }

    /**
     * Filter on this kstat class. Only kstats of this class will be returned
     * by this KstatFilter.
     *
     * @param filterClass the name of the class of kstat to be matched
     */
    public void setFilterClass(final String filterClass) {
	this.filterClass = filterClass;
    }

    /**
     * Filter on this kstat type. Only kstats that have a kstat type equal to
     * this type will be returned by this KstatFilter.
     *
     * @param filterType the type of kstat to be matched
     */
    public void setFilterType(final int filterType) {
	this.filterType = filterType;
    }

    /**
     * Add a filter of the form "module:instance:name:statistic", where the
     * components of the kstat specification are delimited by a ":". Kstats
     * that match this filter will be returned. If any component is left
     * unspecified (blank) or is "*", then match any value.
     *
     * Shortened versions of the filter such as "sd" will result in a match
     * to just those components specified. Expressions with more than 3
     * occurrences of the : delimiter will be silently ignored.
     *
     * @param filter the filter expression
     */
    public void addFilter(final String filter) {
	doFilter(filter, filterList);
    }

    /**
     * Add a filter of the form "module:instance:name:statistic", where the
     * components of the kstat specification are delimited by a ":". Kstats
     * that match this filter will not be returned. If any component is left
     * unspecified (blank) or is "*", then match any value.
     *
     * Shortened versions of the filter such as "sd" will result in a match
     * to just those components specified. Expressions with more than 3
     * occurrences of the : delimiter will be silently ignored.
     *
     * @param filter the filter expression
     */
    public void addNegativeFilter(final String filter) {
	doFilter(filter, antiFilterList);
    }

    /*
     * Common filter code.
     */
    @SuppressWarnings("fallthrough")
    private void doFilter(final String filter, final Set<FilterQuartet> fset) {
	if (filter == null) {
	    return;
	}
	String[] s = filter.split(":");
	/*
	 * If we have too many fields, then the caller gave us an invalid
	 * expression. This will be silently ignored.
	 */
	if (s.length > 4) {
	    return;
	}
	/*
	 * The parts of the filter are split by : and come in the order
	 * module:instance:name:statistic.
	 *
	 * If we have less than that number of fields, assume the rest are
	 * effectively wildcards. This mirrors the behaviour of kstat(8).
	 */
	String module = null;
	Integer instance = null;
	String name = null;
	String statistic = null;
	// Deliberately fall through the switch
	switch (s.length) {
	    case 4:
		if (!"".equals(s[3]) && !"*".equals(s[3])) {
		    statistic = s[3];
		}
	    case 3:
		if (!"".equals(s[2]) && !"*".equals(s[2])) {
		    name = s[2];
		}
	    case 2:
		if (!"".equals(s[1]) && !"*".equals(s[1])) {
		    instance = Integer.valueOf(s[1]);
		}
	    case 1:
		if (!"".equals(s[0]) && !"*".equals(s[0])) {
		    module = s[0];
		}
	}
	fset.add(new FilterQuartet(module, instance, name, statistic));
    }

    /**
     * Returns a {@code Set} of {@code Kstat}s that match the current filter.
     * The returned {@code Kstat}s will not be sorted.
     *
     * @return the {@code Kstat}s that match the filter
     */
    public Set<Kstat> getKstats() {
	return getKstats(false);
    }

    /**
     * Returns a {@code Set} of {@code Kstat}s that match the current filter.
     * If requested, the {@code Kstat}s will be sorted, backed by a
     * {@code TreeSet}.
     *
     * @param sorted whether the returned {@code Kstat}s should be sorted
     *
     * @return the {@code Kstat}s that match the filter
     */
    public Set<Kstat> getKstats(final boolean sorted) {
	Set<Kstat> matchset = sorted ? new TreeSet<>() : new HashSet<>();
	for (Kstat ks : jkstat.getKstats()) {
	    if (matchFilter(ks)) {
		matchset.add(ks);
	    }
	}
	return matchset;
    }

    /**
     * Return those statistics matched by this filter for the given Kstat.
     * If the Kstat is only matched because it matches a statistic by name,
     * only return that statistic. If we match on module, instance, or name
     * then return the entire Set of statistics.
     *
     * @param ks the Kstat whose statistics should be filtered
     *
     * @return a filtered Set of statistics
     */
    public Set<String> filteredStatistics(final Kstat ks) {
	if (ks == null) {
	    return null;
	}
	/*
	 * The question is whether we match on the module:instance:name
	 * fields or whether we match a statistic . So, we check
	 * module:instance:name, and if we match, then we return all the
	 * statistics.
	 */
	if (matchMIN(ks)) {
	    return ks.statistics();
	}
	/*
	 * If we get here then we matched a statistic. So we return the
	 * statistic (or statistics) we actually matched.
	 */
	return matchedStats(ks);
    }

    /*
     * Match a kstat against the filters to see if it matches by
     * module:instance:name, or more accurately that we weren't matched on
     * the statistic
     *
     * As it is only called from filteredStatistics, we know ks is not null
     */
    private boolean matchMIN(final Kstat ks) {
	/*
	 * If there are no filters we definitely match. While counterintuitive
	 * this behaviour is correct - it means we cannot possibly have been
	 * matched on statistic
	 */
	if (filterList.isEmpty()) {
	    return true;
	}
	for (FilterQuartet fq : filterList) {
	    // skip this if we match on statistic
	    if ((fq.statistic != null) && ks.hasStatistic(fq.statistic)) {
		break;
	    }
	    if ((fq.module != null) && fq.module.equals(ks.getModule())) {
		return true;
	    }
	    if ((fq.instance != null) && (fq.instance == ks.getInst())) {
		return true;
	    }
	    if ((fq.name != null) && fq.name.equals(ks.getName())) {
		return true;
	    }
	}
	return false;
    }

    /*
     * Return the Set of statistics that this filter matches against the
     * given Kstat.
     */
    private Set<String> matchedStats(final Kstat ks) {
	Set<String> nstats = new TreeSet<>();
	/*
	 * Because matchMIN is called before this, we know there has to be
	 * at least one positive filter.
	 */
	for (FilterQuartet fq : filterList) {
	    /*
	     * If the pattern has a statistic, and it matches one of the
	     * statistics in this kstat, add it to the list.
	     */
	    if ((fq.statistic != null) && ks.hasStatistic(fq.statistic)) {
		nstats.add(fq.statistic);
	    }
	}
	/*
	 * If we didn't add any statistics then the logic is wrong and we must
	 * have matched some other way, so return the entire set.
	 */
	return nstats.isEmpty()	? ks.statistics() : nstats;
    }

    /*
     * Match a Kstat against the filter. Return true if the Kstat
     * matches the kstat class (if not null) and any of the filters.
     *
     * If the kstat to be checked is null, or is read and later found to be
     * null, then return false. These null values indicate invalid kstats
     * (perhaps they've disappeared), and as such they shouldn't appear
     * in the list of matching kstats. These null kstats are not a sign
     * of an error - kstats vanish as a matter of course.
     *
     * @param ks the Kstat to be matched against the filter
     *
     * @return true if the Kstat matches the filter
     */
    private boolean matchFilter(Kstat ks) {
	if (ks == null) {
	    // invalid kstat, we definitely don't want it
	    return false;
	}
	/*
	 * If we fail to match the class, return false immediately
	 */
	if ((filterClass != null) && !filterClass.equals(ks.getKstatClass())) {
	    return false;
	}
	/*
	 * If we fail to match the type, return false immediately
	 */
	if ((filterType >= 0) && (ks.getType() != filterType)) {
	    return false;
	}
	/*
	 * We passed the match against class and type, now check through the
	 * filters. First, look through any negative filters.
	 */
	for (FilterQuartet fq : antiFilterList) {
	    if (matchTriplet(ks, fq)) {
		return false;
	    }
	}
	/*
	 * If there are no filters we definitely match.
	 */
	if (filterList.isEmpty()) {
	    return true;
	}
	/*
	 * Iterate through the filters,
	 */
	boolean smatch;
	boolean statread = false;
	for (FilterQuartet fq : filterList) {
	    /*
	     * To check against statistics involves reading the data for this
	     * kstat, which isn't normally done by the enumeration. So we must
	     * read the kstat to initialize the statistics hash. As this
	     * operation is expensive, try to avoid it.
	     */
	    if (matchTriplet(ks, fq)) {
		if (fq.statistic == null) {
		    smatch = true;
		} else {
		    if (!statread) {
			ks = jkstat.getKstat(ks);
			statread = true;
		    }
		    // if not a valid kstat, then it shouldn't match
		    if (ks == null) {
			return false;
		    }
		    smatch = ks.hasStatistic(fq.statistic);
		}
		if (smatch) {
		    return true;
		}
	    }
	}
	/*
	 * If we get this far we don't match anything.
	 */
	return false;
    }

    private boolean matchTriplet(final Kstat ks, final FilterQuartet fq) {
	boolean mmatch =
	    (fq.module == null) || fq.module.equals(ks.getModule());
	boolean imatch =
	    (fq.instance == null) || fq.instance == ks.getInst();
	boolean nmatch =
	    (fq.name == null) || fq.name.equals(ks.getName());
	return mmatch && imatch && nmatch;
    }

    /**
     * Inner class to describe a filter.
     */
    class FilterQuartet {
	protected String module;
	protected Integer instance;
	protected String name;
	protected String statistic;

	FilterQuartet(final String module, final Integer instance,
		      final String name, final String statistic) {
	    this.module = module;
	    this.instance = instance;
	    this.name = name;
	    this.statistic = statistic;
	}
    }
}
