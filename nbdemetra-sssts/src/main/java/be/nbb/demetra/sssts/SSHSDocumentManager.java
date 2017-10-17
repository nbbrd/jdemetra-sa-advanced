/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.sssts;

import be.nbb.demetra.sssts.descriptors.SSHSSpecUI;
import be.nbb.demetra.sssts.document.SSHSDocument;
import be.nbb.demetra.sssts.document.SSHSProcessor;
import be.nbb.demetra.sssts.ui.SSHSViewFactory;
import ec.nbdemetra.ui.DocumentUIServices;
import ec.nbdemetra.ui.properties.l2fprod.CustomPropertyEditorRegistry;
import ec.nbdemetra.ws.AbstractWorkspaceTsItemManager;
import ec.nbdemetra.ws.IWorkspaceItemManager;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.sa.SaManager;
import ec.tstoolkit.descriptors.IObjectDescriptor;
import ec.tstoolkit.utilities.Id;
import ec.tstoolkit.utilities.LinearId;
import ec.ui.view.tsprocessing.IProcDocumentView;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = IWorkspaceItemManager.class,
        position = 1840)
public class SSHSDocumentManager extends AbstractWorkspaceTsItemManager<SSHSSpecification, SSHSDocument> {

    static {
        SaManager.instance.add(new SSHSProcessor());
        CustomPropertyEditorRegistry.INSTANCE.registerEnumEditor(SeasonalSpecification.EstimationMethod.class);
        DocumentUIServices.getDefault().register(SSHSDocument.class, new DocumentUIServices.AbstractUIFactory<SSHSSpecification, SSHSDocument>() {

            @Override
            public IProcDocumentView<SSHSDocument> getDocumentView(SSHSDocument document) {
                return SSHSViewFactory.getDefault().create(document);
            }

            @Override
            public IObjectDescriptor<SSHSSpecification> getSpecificationDescriptor(SSHSDocument doc) {
                return new SSHSSpecUI(doc.getSpecification().clone());
            }
        });
    }

    public static final LinearId ID = new LinearId(SSHSProcessor.DESCRIPTOR.family, "documents", SSHSProcessor.DESCRIPTOR.name);
    public static final String PATH = "sshs.doc";
    public static final String ITEMPATH = "sshs.doc.item";
    public static final String CONTEXTPATH = "sshs.doc.context";

    @Override
    protected String getItemPrefix() {
        return "SSHS";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    protected SSHSDocument createNewObject() {
        return new SSHSDocument();
    }

    @Override
    public ItemType getItemType() {
        return ItemType.Doc;
    }

    @Override
    public String getActionsPath() {
        return PATH;
    }

    @Override
    public Status getStatus() {
        return Status.Experimental;
    }

    @Override
    public void openDocument(WorkspaceItem<SSHSDocument> doc) {
        if (doc.isOpen()) {
            doc.getView().requestActive();
        } else {
            SSHSTopComponent view = new SSHSTopComponent(doc);
            doc.setView(view);
            view.open();
            view.requestActive();
        }
    }

    @Override
    public Class<SSHSDocument> getItemClass() {
        return SSHSDocument.class;
    }
}
