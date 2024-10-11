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

        KSTAT_DATA_CHAR(0, false),
        KSTAT_DATA_INT32(1, true),
        KSTAT_DATA_UINT32(2, true),
        KSTAT_DATA_INT64(3, true),
        KSTAT_DATA_UINT64(4, true),
        KSTAT_DATA_FLOAT(5, true),
        KSTAT_DATA_DOUBLE(6, true),
        KSTAT_DATA_STRING(9, false);
        private final int int_type;
        private final boolean numeric;

        private Type(int int_type, boolean numeric) {
            this.int_type = int_type;
            this.numeric = numeric;
        };

	/**
	 * Return the numeric value of this Type.
	 *
	 * @return an int representing the numeric value of this Type
	 */
        public int toInt() {
            return int_type;
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
	 * @param int_type an integer to return the Type of
	 *
	 * @return the Type corresponding to the argument
	 */
        public static Type toType(int int_type) {
            for (Type type : values()) {
                if (int_type == type.toInt()) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Illegal Type Numeric Value (" +
                    int_type + ")");
        }
    }
    private final Type type;
    private final Object data;

    /**
     * Allocates a {@code KstatData} of the given type and data value.
     *
     * @param type the type of the data
     * @param data the data
     */
    public KstatData(Type type, Object data) {
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
     * @return true if this data is of one of the 4 int_type types
     */
    public boolean isNumeric() {
        return type.isNumeric();
    }
}
