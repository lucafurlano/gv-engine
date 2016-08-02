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
package it.greenvulcano.gvesb.statistics;

import org.w3c.dom.Node;

/**
 * This interface declare the methods for store the statistics data information
 *
 * @version     3.0.0 Feb 17, 2010
 * @author     GreenVulcano Developer Team
 *
 *
 */

public interface IStatisticsWriter {
    /**
     * This method initialize the StatisticsWriter Plug-In
     *
     * @param node
     * @throws GVStatisticsException
     */
    public void init(Node node) throws GVStatisticsException;

    /**
     * This method store the statistics data object
     *
     * @param statisticsData
     * @return
     * @throws GVStatisticsException
     */
    public boolean writeStatisticsData(StatisticsData statisticsData) throws GVStatisticsException;


    /**
     * Perform cleanup operations.
     */
    public void destroy();
}
