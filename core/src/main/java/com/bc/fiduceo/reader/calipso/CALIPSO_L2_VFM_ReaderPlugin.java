/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.fiduceo.reader.calipso;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderPlugin;

public class CALIPSO_L2_VFM_ReaderPlugin implements ReaderPlugin {

    @Override
    public Reader createReader(GeometryFactory geometryFactory) {
        return new CALIPSO_L2_VFM_Reader(geometryFactory);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public DataType getDataType() {
        throw new RuntimeException("not implemented");
    }
}
