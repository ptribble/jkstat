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
 * Classes allowing the construction of a client that can retrieve
 * kstat data from a JKstat server.
 * <p>
 * A RemoteJKstat is a JKstat implementation that you can query for the
 * data. In normal use, this is the only class that's of interest. It can
 * simply be created using a URL to specify the server, or read its
 * configuration from a KClientConfig.
 * <p>
 * The JKStatClient class handles communication over the wire.
 */

package uk.co.petertribble.jkstat.client;
