/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.iasi;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;


// @todo 2 tb/tb write tests 2017-04-24
enum InstrumentGroup {
    GENERIC,
    AMSU_A,
    ASCAT,
    ATOVS,
    AVHRR_3,
    GOME,
    GRAS,
    HIRS_4,
    IASI,
    MHS,
    SEM,
    ADCS,
    SBUV,
    DUMMY,
    ARCHIVE,
    IASI_L2;

    public static InstrumentGroup readInstrumentGroup(ImageInputStream iis) throws IOException {
        final byte b = iis.readByte();

        if (isValid(b)) {
            return values()[b];
        }

        throw new IOException("unknown instrument group '" + b + "'");
    }

    private static boolean isValid(byte b) {
        return (b >= 0 && b < values().length);
    }
}
