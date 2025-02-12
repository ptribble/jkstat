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
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 *
 * Copyright 2025 Peter Tribble.
 */
package uk.co.petertribble.jkstat.util;

import java.util.Comparator;

/**
 * Sorts non-numeric portions of strings lexicographically, but sorts
 * numeric portions of strings numerically. Allows multiple numbers
 * interspersed throughout the string. Note that the numbers serve as
 * sort delimiters, so that "cc1" sorts higher than "c2".
 *
 * @author Tom Erickson
 */
public final class NumericStringComparator implements Comparator<String> {

    private static final NumericStringComparator INSTANCE =
	    new NumericStringComparator();

    private NumericStringComparator() {
    }

    /**
     * Get the singleton NumericStringComparator instance.
     *
     * @return the singleton NumericStringComparator instance
     */
    public static NumericStringComparator getInstance() {
	return INSTANCE;
    }

    /**
     * Compare two strings, the numeric portion(s) being sorted numerically
     * and the remainder lexicographically.
     *
     * @param s1 The first String
     * @param s2 The String to be compared to the first String
     * @return 0 if the two Strings are equal, 1 if s1 sorts after (is larger
     * than) s2, and -1 if s2 sorts after s1
     */
    @Override
    public int compare(String s1, String s2) {
	char c1;
	char c2;
	boolean d1;
	boolean d2;
	int n1;
	int n2;

	int i = 0;
	int len1 = s1.length();
	int len2 = s2.length();
	int imax = (len2 > len1) ? len1 : len2;

	while (i < imax) {
	    c1 = s1.charAt(i);
	    c2 = s2.charAt(i);
	    d1 = Character.isDigit(c1);
	    d2 = Character.isDigit(c2);
	    if (d1) {
		if (d2) {
		    n1 = i + 1;
		    while ((n1 < len1) && Character.isDigit(s1.charAt(n1))) {
			++n1;
		    }
		    n2 = i + 1;
		    while ((n2 < len2) && Character.isDigit(s2.charAt(n2))) {
			++n2;
		    }
		    // if different lengths the longer is larger
		    if (n1 != n2) {
			return n1 - n2;
		    }
		    // same length, compare the digits
		    while (i < n1) {
			c1 = s1.charAt(i);
			c2 = s2.charAt(i);
			if (c1 != c2) {
			    return c1 - c2;
			}
			++i;
		    }
		} else {
		    return -1; // s2 non-numeric sequence is longer
		}
	    } else {
		if (d2) {
		    return 1; // s1 non-numeric sequence is longer
		}
	    }
	    if (c1 != c2) {
		return c1 - c2;
	    }
	    ++i;
	}
	return len1 - len2;
    }
}
