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
 * Standard graphical user interface components for JKstat.
 * <p>
 * Accessories are little display widgets displaying a specific kstat
 * (or type of kstat). They all extend KstatAccessoryPanel, and you
 * should use the KstatAccessoryPanel interface and its methods to
 * update the data, and to stop and start the updates. Accessories
 * should normally have no other public methods.
 * <p>
 * KstatAccessoryRegistry can be queried to see if an accessory exists for
 * a given kstat, and to obtain an instance of the correct accessory panel.
 * Again, cast to a KstatAccessoryPanel.
 * <p>
 * There is support for displaying a kstat in tables, see
 * KstatTable. There are also some specific TableModels to support iostat
 * and mpstat.
 * <p>
 * Other classes support the generation of charts, using
 * <a href="https://www.jfree.org/jfreechart/">JFreeChart</a>.
 */

package uk.co.petertribble.jkstat.gui;
