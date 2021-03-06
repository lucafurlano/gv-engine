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
package tests.unit.gvesb.utils;

import java.util.HashMap;
import java.util.Map;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;
import tests.unit.BaseTestCase;
import tests.unit.gvrules.bean.figure.Circle;


/**
 *
 * @version 3.0.0 10/giu/2010
 * @author GreenVulcano Developer Team
 */
public class GVESBPropertyHandlerTestCase extends BaseTestCase
{
    private static final String EXPECTED_RESULT         = "hello_world_expander";
    private static final String EXPECTED_RESULT_REVERSE = "expander_world_hello";

    
    @Override
    protected void setUp() throws Exception {    	
    	super.setUp();
    }
    
    public void testBasicHandler() throws PropertiesHandlerException{
    	String base = PropertiesHandler.expand("${{gv.app.home}}");
    	
    	assertNotSame("${{gv.app.home}}", base);
    	
    	assertEquals(PropertiesHandler.expand("{test:{id:1}}"), "{test:{id:1}}");
    }
    
    /**
     * @throws Exception
     */
    public final void testGVBuffer() throws Exception
    {
        GVBuffer gvBuffer = new GVBuffer();
        String inputA = "ognl{{#object.property['prop1']}}_ognl{{#object.property['prop2']}}_ognl{{#object.property['prop3']}}";

        gvBuffer.setProperty("prop1", "hello");
        gvBuffer.setProperty("prop2", "world");
        gvBuffer.setProperty("prop3", "expander");
        String result = PropertiesHandler.expand(inputA, null, gvBuffer);
        assertEquals(EXPECTED_RESULT, result);

        String inputB = "ognl{{#appo=#object.property['prop1'],#object.property['prop1']=#object.property['prop3'],#object.property['prop3']=#appo}}";
        PropertiesHandler.expand(inputB, null, gvBuffer);
        assertEquals(EXPECTED_RESULT_REVERSE, PropertiesHandler.expand(inputA, null, gvBuffer));
    }

    /**
     * @throws Exception
     */
    public final void testGVBuffer2() throws Exception
    {
        GVBuffer gvBuffer1 = new GVBuffer("SYS1", "SRV1");
        gvBuffer1.setProperty("prop1", "SRV1_value1");
        gvBuffer1.setProperty("prop2", "SRV1_value2");

        GVBuffer gvBuffer2 = new GVBuffer("SYS2", "SRV2");
        gvBuffer2.setProperty("prop1", "SRV2_value1");
        gvBuffer2.setProperty("prop2", "SRV2_value2");

        String input = "ognl{{#object.property['prop1']=#extra.property['prop1'],#object.property['prop2']=#extra.property['prop2']}}";

        PropertiesHandler.expand(input, null, gvBuffer1, gvBuffer2);

        System.out.println("testGVBuffer2: " + gvBuffer1);
        System.out.println("testGVBuffer2: " + gvBuffer2);

        assertEquals(gvBuffer2.getProperty("prop1"), gvBuffer1.getProperty("prop1"));
        assertEquals(gvBuffer2.getProperty("prop2"), gvBuffer1.getProperty("prop2"));
    }
    
    public final void testGVBufferDefaultProperty() throws Exception{
        GVBuffer gvBuffer1 = new GVBuffer("SYS1", "SRV1");
        gvBuffer1.setProperty("prop1", "SRV1_value1");
        gvBuffer1.setProperty("prop2", "SRV1_value2");
        
        gvBuffer1.setObject(new Circle("cyano"));
      
        Map<String,Object> props = new HashMap<>();
        for (String propName : gvBuffer1.getPropertyNamesSet()) {
        	props.put(propName, gvBuffer1.getProperty(propName));
        }
        
        String input = "@{{prop1}}";
        String output = PropertiesHandler.expand(input, props, gvBuffer1, null);
        assertEquals(gvBuffer1.getProperty("prop1"), output);
        
        input = "@{{prop2::notthis}}";
        output = PropertiesHandler.expand(input, props, gvBuffer1, null);
        assertEquals(gvBuffer1.getProperty("prop2"), output);
        
        input = "@{{prop3::yeah}}";
        output = PropertiesHandler.expand(input, props, gvBuffer1, null);
        assertEquals("yeah", output);
        
        input = "@{{prop3::@{{prop1}}}}";
        output = PropertiesHandler.expand(input, props, gvBuffer1, null);
        assertEquals(gvBuffer1.getProperty("prop1"), output);
       
       
    }
    
    public final void testGVBufferObject() throws Exception{
        GVBuffer gvBuffer1 = new GVBuffer("SYS1", "SRV1");
        gvBuffer1.setProperty("prop1", "SRV1_value1");
        gvBuffer1.setProperty("prop2", "SRV1_value2");
        
        gvBuffer1.setObject(new Circle("cyano"));
      
        String input = "#{{object.color}}";

        String output = PropertiesHandler.expand(input, null, gvBuffer1, null);

        assertEquals(Circle.class.cast(gvBuffer1.getObject()).getColor(), output);
       
    }
    
    public final void testGVBufferJSON() throws Exception{
        GVBuffer gvBuffer1 = new GVBuffer("SYS1", "SRV1");
        gvBuffer1.setProperty("prop1", "SRV1_value1");
        gvBuffer1.setProperty("prop2", "SRV1_value2");
        
        gvBuffer1.setObject("{\"type\":\"json\",\"nested\":{\"value\":\"it works\"}}");
      
        String input = "json{{type}}";
        String output = PropertiesHandler.expand(input, null, gvBuffer1.getObject(), null);

        assertEquals("json", output);
        
        input = "json{{nested.value}}";
        output = PropertiesHandler.expand(input, null, gvBuffer1.getObject(), null);

        assertEquals("it works", output);
       
        gvBuffer1.setObject("{\"type\":\"bytes\",\"nested\":{\"value\":\"it works\"}}".getBytes());
        input = "json{{nested.value}}";
        output = PropertiesHandler.expand(input, null, gvBuffer1.getObject(), null);
              
        assertEquals("it works", output);
        
        gvBuffer1.setObject("[21, 22, {\"value\":\"it works\"}]".getBytes());
        input = "json{{array[2].value}}";
        
        output = PropertiesHandler.expand(input, null, gvBuffer1.getObject(), null);
        
        assertEquals("it works", output);
        
        
        gvBuffer1.setObject("				"
        		+ "\t\n{\"type\":\"json\",\"nested\":"
        		+ "		{\"value\":\"it works\"}"
        		+ "}\n\n\r");
        
        input = "json{{type}}";
        output = PropertiesHandler.expand(input, null, gvBuffer1.getObject(), null);

        assertEquals("json", output);
        
    }
}
