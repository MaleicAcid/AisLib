/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.bus.configuration.filter;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlRootElement;

import dk.dma.ais.filter.GatehouseSourceFilter;
import dk.dma.ais.filter.IPacketFilter;

@XmlRootElement
public class GatehouseSourceFilterConfiguration extends FilterConfiguration {

    private HashMap<String, String> filterEntries = new HashMap<>();

    public GatehouseSourceFilterConfiguration() {

    }
    
    public HashMap<String, String> getFilterEntries() {
        return filterEntries;
    }
    
    public void setFilterEntries(HashMap<String, String> filterEntries) {
        this.filterEntries = filterEntries;
    }

    @Override
    public IPacketFilter getInstance() {
        GatehouseSourceFilter sourceFilter = new GatehouseSourceFilter();
        for (Entry<String, String> entry : filterEntries.entrySet()) {
            sourceFilter.addFilterValue(entry.getKey(), entry.getValue());
        }
        return sourceFilter;
    }

}