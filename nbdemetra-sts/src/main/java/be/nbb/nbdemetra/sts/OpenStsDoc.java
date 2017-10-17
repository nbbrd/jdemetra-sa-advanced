/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.nbdemetra.sts;

import be.nbb.demetra.sts.document.StsDocument;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "be.nbb.nbdemetra.sts.OpenStsDoc")
@ActionRegistration(displayName = "#CTL_OpenStsDoc")
@ActionReferences({
    @ActionReference(path = StsDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1300)
 })
@NbBundle.Messages("CTL_OpenStsDoc=Open")
public class OpenStsDoc implements ActionListener {

    private final WsNode context;

    public OpenStsDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<StsDocument> doc = context.getWorkspace().searchDocument(context.lookup(), StsDocument.class);
        StsDocumentManager manager = WorkspaceFactory.getInstance().getManager(StsDocumentManager.class);
        manager.openDocument(doc);
    }
}
