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

/**
 * Represents a kstat data item. An item of kstat data has a data type
 * and a value.
 *
 * @author Peter Tribble
 */
public class KstatData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Inner class representing the Kstat data type. See
     * /usr/include/sys/kstat.h for details, as the definitions here are
     * derived from there, and must match.
     */
    public enum Type {

	/**
	 * Data is a char.
	 */
        KSTAT_DATA_CHAR(0, false),
	/**
	 * Data is signed 32-bit int.
	 */
        KSTAT_DATA_INT32(1, true),
	/**
	 * Data is unsigned 32-bit int.
	 */
        KSTAT_DATA_UINT32(2, true),
	/**
	 * Data is signed 64-bit int.
	 */
        KSTAT_DATA_INT64(3, true),
	/**
	 * Data is unsigned 64-bit int.
	 */
        KSTAT_DATA_UINT64(4, true),
	/**
	 * Data is a float.
	 */
        KSTAT_DATA_FLOAT(5, true),
	/**
	 * Data is a double.
	 */
        KSTAT_DATA_DOUBLE(6, true),
	/**
	 * Data is a string.
	 */
        KSTAT_DATA_STRING(9, false);
        private final int intType;
        private final boolean numeric;

        Type(final int intType, final boolean numeric) {
            this.intType = intType;
            this.numeric = numeric;
        }

	/**
	 * Return the numeric value of this Type.
	 *
	 * @return an int representing the numeric value of this Type
	 */
        public int toInt() {
            return intType;
        }

	/**
	 * Return whether this Type is a numeric value.
	 *
	 * @return true if this object is a numeric type
	 */
        public boolean isNumeric() {
            return numeric;
        }

	/**
	 * Return a Type by numeric value.
	 *
	 * @param intType an integer to return the Type of
	 *
	 * @return the Type corresponding to the argument
	 */
        public static Type toType(final int intType) {
            for (Type type : values()) {
                if (intType == type.toInt()) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Illegal Type Numeric Value ("
                    + intType + ")");
        }
    }
    /**
     * The Type of this KstatData.
     */
    private final Type type;
    private final transient Object data;

    /**
     * Allocates a {@code KstatData} of the given type and data value.
     *
     * @param type the type of the data
     * @param data the data
     */
    public KstatData(final Type type, final Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Gets the data type of this {@code KstatData}.
     *
     * @return The data type of this {@code KstatData}
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the data held by this {@code KstatData}.
     *
     * @return the data held by this {@code KstatData}
     */
    public Object getData() {
        return data;
    }

    /**
     * Gets a {@code String} representation of this {@code KstatData}.
     *
     * @return The {@code String} representation of this {@code KstatData}'s
     * data
     */
    @Override
    public String toString() {
        return data.toString();
    }

    /**
     * Returns whether this {@code KstatData} is of a Numeric type.
     *
     * @return true if this data is of one of the 4 intType types
     */
    public boolean isNumeric() {
        return type.isNumeric();
    }
}
