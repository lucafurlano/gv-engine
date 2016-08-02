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
package it.greenvulcano.gvesb.gvdte.config;

import it.greenvulcano.gvesb.gvdte.DTEException;

/**
 * Exception class for configuration errors
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 *
 *
 */
public class ConfigException extends DTEException
{
    private static final long serialVersionUID = 210L;

    /**
     * Creates a new ConfigException with error code identified by
     * <code>errorId</code> and no cause.
     *
     * @param errorId
     *        ErrorId associated to the exception
     */
    public ConfigException(String errorId)
    {
        super(errorId);
    }

    /**
     * Creates a new ConfigException with error code identified by
     * <code>errorId</code> and no cause. <code>params</code> is used to
     * complete the error message.
     *
     * @param errorId
     *        ErrorId associated to the exception
     * @param params
     *        key/value array of parameter to be substituted in the error
     *        message.
     */
    public ConfigException(String errorId, String[][] params)
    {
        super(errorId, params);
    }

    /**
     * Creates a new ConfigException with error code identified by
     * <code>errorId</code> and a cause.
     *
     * @param errorId
     *        ErrorId associated to the exception
     * @param exc
     *        Throwable that caused this exception to get thrown
     */
    public ConfigException(String errorId, Throwable exc)
    {
        super(errorId, exc);
    }

    /**
     * Creates a new ConfigException with a cause.
     *
     * @param errorId
     *        message associated to the exception
     * @param params
     *        key/value array of parameter to be substituted in the error
     *        message.
     * @param exc
     *        Throwable that caused this exception to get thrown
     */
    public ConfigException(String errorId, String[][] params, Throwable exc)
    {
        super(errorId, params, exc);
    }

}
