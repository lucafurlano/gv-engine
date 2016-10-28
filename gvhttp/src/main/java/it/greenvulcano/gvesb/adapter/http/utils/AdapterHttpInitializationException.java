/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.adapter.http.utils;

/**
 *
 * AdapterHttpInitializationException class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class AdapterHttpInitializationException extends AdapterHttpException
{
    private static final long serialVersionUID = -1385229766444236471L;

    /**
     * @param id
     */
    public AdapterHttpInitializationException(String id)
    {
        super(id);
    }

    /**
     * @param id
     * @param cause
     */
    public AdapterHttpInitializationException(String id, Throwable cause)
    {
        super(id, cause);
    }

    /**
     * @param id
     * @param params
     */
    public AdapterHttpInitializationException(String id, String[][] params)
    {
        super(id, params);
    }

    /**
     * @param id
     * @param params
     * @param cause
     */
    public AdapterHttpInitializationException(String id, String[][] params, Throwable cause)
    {
        super(id, params, cause);
    }

}
