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
 * The JKstat browser allows a user to interactively browse Solaris
 * or illumos kstats.
 * <p>
 * A kstat is a triplet module:instance:name, which is loaded into a
 * hierarchical set of Maps and then presented to the user as a tree.
 * <p>
 * A kstat is displayed as a table, with an optional accessory to give
 * a graphical representation.
 */

package uk.co.petertribble.jkstat.browser;
