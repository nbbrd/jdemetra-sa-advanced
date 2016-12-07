/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.sssts;

import be.nbb.demetra.sssts.document.SSSTSDocument;
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
id = "be.nbb.nbdemetra.sssts.OpenSSSTSDoc")
@ActionRegistration(displayName = "#CTL_OpenSSSTSDoc")
@ActionReferences({
    @ActionReference(path = SSSTSDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1300)
 })
@NbBundle.Messages("CTL_OpenSSSTSDoc=Open")
public class OpenSSSTSDoc implements ActionListener {

    private final WsNode context;

    public OpenSSSTSDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<SSSTSDocument> doc = context.getWorkspace().searchDocument(context.lookup(), SSSTSDocument.class);
        SSSTSDocumentManager manager = WorkspaceFactory.getInstance().getManager(SSSTSDocumentManager.class);
        manager.openDocument(doc);
    }
}
