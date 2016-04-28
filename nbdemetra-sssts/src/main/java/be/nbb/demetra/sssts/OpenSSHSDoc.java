/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.sssts;

import be.nbb.demetra.sssts.document.SSHSDocument;
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
id = "be.nbb.nbdemetra.sssts.OpenSSHSDoc")
@ActionRegistration(displayName = "#CTL_OpenSSHSDoc")
@ActionReferences({
    @ActionReference(path = SSHSDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1300)
 })
@NbBundle.Messages("CTL_OpenSSHSDoc=Open")
public class OpenSSHSDoc implements ActionListener {

    private final WsNode context;

    public OpenSSHSDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<SSHSDocument> doc = context.getWorkspace().searchDocument(context.lookup(), SSHSDocument.class);
        SSHSDocumentManager manager = WorkspaceFactory.getInstance().getManager(SSHSDocumentManager.class);
        manager.openDocument(doc);
    }
}
