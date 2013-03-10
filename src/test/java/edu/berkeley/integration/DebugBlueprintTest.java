package edu.berkeley.integration;

import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.Test;

public class DebugBlueprintTest extends CamelBlueprintTestSupport {

    // override this method, and return the location of our Blueprint XML file to be used for testing
    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/blueprint.xml";
    }

    // here we have regular Junit @Test method
    @Test
    public void testRoute() throws Exception {
        
    }

}