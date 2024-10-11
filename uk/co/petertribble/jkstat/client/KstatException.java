/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

package uk.co.petertribble.jkstat.client;

/**
 * An exception class for the JKstat client. This is thrown instead of
 * RunTimeException so that consumers can detect failures, and can identify
 * them as being generated by JKstat rather than being generic java errors.
 *
 * @author Peter Tribble
 */
public class KstatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new KstatException.
     */
    public KstatException() {
	super();
    }

    /**
     * Construct a new KstatException with the specified detail message.
     *
     * @param message the detail message
     */
    public KstatException(String message) {
	super(message);
    }

    /**
     * Construct a new KstatException with the specified detail message and
     * cause.
     *
     * @param message the detail message
     * @param cause the underlying cause for this KstatException
     */
    public KstatException(String message, Throwable cause) {
	super(message, cause);
    }
}
