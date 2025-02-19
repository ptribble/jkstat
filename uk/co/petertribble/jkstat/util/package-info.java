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

/**
 * Some utility classes that JKstat uses.
 * <p>
 * NumericStringComparator allows sensible sorting of Strings
 * containing numbers. The numeric component is sorted numerically rather
 * than lexicographically.
 * <p>
 * Humanize scales numeric rates into a more readable k/s or m/s
 * form.</p>
 */

package uk.co.petertribble.jkstat.util;
