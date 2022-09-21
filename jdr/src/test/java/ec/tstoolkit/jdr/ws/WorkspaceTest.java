/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.ws;

import data.Data;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import java.io.IOException;
import jdr.spec.ts.Utility.Dictionary;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class WorkspaceTest {
    
    public WorkspaceTest() {
    }

    //@Test
    public void testOpen() throws IOException {
        Workspace ws = Workspace.open("c:\\localdata\\sarepository\\test.xml");
        Dictionary dictionary = ws.dictionary();
        for (String s:dictionary.names())
            System.out.println(s);
        long t0=System.currentTimeMillis();
        ws.computeAll();
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
        
        ws.save("c:\\sarepository\\mytest3.xml");
    }
    
    //@Test
    public void testNew() throws IOException {
        Workspace ws = Workspace.create(new Dictionary());
        MultiProcessing mp = ws.newMultiProcessing("test");
        mp.add("a", Data.P, TramoSeatsSpecification.RSAfull);
        mp.add("p", Data.P, X13Specification.RSA5);
        mp = ws.newMultiProcessing("test2");
        ws.save("c:\\sarepository\\mytest3.xml");
        
        ws = Workspace.open("c:\\sarepository\\mytest3.xml");
        System.out.println(ws.getMultiProcessingCount());
     }
}
