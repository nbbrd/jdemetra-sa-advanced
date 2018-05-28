/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.data;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class IArrayOfDoublesReaderTest {
    
    public IArrayOfDoublesReaderTest() {
    }

    @Test
    public void testNorm() {
        ArrayOfDoublesReader reader=new ArrayOfDoublesReader(100, i->new Random(i).nextDouble());
//        System.out.println(reader.norm1());
//        System.out.println(reader.norm2());
//        System.out.println(reader.normInf());
        assertTrue(reader.normInf()<=reader.norm2());
        assertTrue(reader.norm2()<=reader.norm1());
    }
    
}
