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
 * Implementations of JKstat servers.
 * <p>
 * The JKstatServer is a standalone server that can also be embedded in
 * a servlet engine. It uses Apache XML-RPC for communication. Data is
 * serialized using JSON.
 * <p>
 * There is also an example JMX implementation. The standard JMX
 * console can be used, but you will need to add the jkstat jar file to
 * the classpath.
 */

package uk.co.petertribble.jkstat.server;
