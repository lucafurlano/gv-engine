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
package it.greenvulcano.gvesb.security.callers;

import javax.ejb.EJBContext;

/**
 * EJBCaller class
 *
 * @version 3.0.0 Feb 17, 2010
 * @author GreenVulcano Developer Team
 **/
public class EJBCaller implements Caller
{

    private EJBContext ejbContext = null;

    /**
     * Constructor with parameter.
     *
     * @param ejbContext
     *        EJBContext
     */
    public EJBCaller(EJBContext ejbContext)
    {
        this.ejbContext = ejbContext;
    }

    /**
     * @see it.greenvulcano.gvesb.security.callers.Caller#getCallerName()
     */
    public String getCallerName()
    {
        return ejbContext.getCallerPrincipal().getName();
    }

    /**
     * @see it.greenvulcano.gvesb.security.callers.Caller#isCallerInRole(java.lang.String)
     */
    public boolean isCallerInRole(String role)
    {
        return ejbContext.isCallerInRole(role);
    }

    /**
     * @see it.greenvulcano.gvesb.security.callers.Caller#isSecure()
     */
    public boolean isSecure()
    {
        return false;
    }

    /**
     * @param ejbContext
     *        The ejbContext to set.
     */
    public void setEjbContext(EJBContext ejbContext)
    {
        this.ejbContext = ejbContext;
    }
}
