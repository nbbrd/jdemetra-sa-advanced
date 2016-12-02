/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.sssts;

import be.nbb.demetra.sssts.descriptors.SSSTSSpecUI;
import be.nbb.demetra.sssts.document.SSSTSDocument;
import be.nbb.demetra.sssts.document.SSSTSProcessor;
import be.nbb.demetra.sssts.ui.SSSTSViewFactory;
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
public class SSSTSDocumentManager extends AbstractWorkspaceTsItemManager<SSSTSSpecification, SSSTSDocument> {

    static {
        SaManager.instance.add(new SSSTSProcessor());
        CustomPropertyEditorRegistry.INSTANCE.registerEnumEditor(SeasonalSpecification.EstimationMethod.class);
        DocumentUIServices.getDefault().register(SSSTSDocument.class, new DocumentUIServices.AbstractUIFactory<SSSTSSpecification, SSSTSDocument>() {

            @Override
            public IProcDocumentView<SSSTSDocument> getDocumentView(SSSTSDocument document) {
                return SSSTSViewFactory.getDefault().create(document);
            }

            @Override
            public IObjectDescriptor<SSSTSSpecification> getSpecificationDescriptor(SSSTSDocument doc) {
                return new SSSTSSpecUI(doc.getSpecification().clone());
            }
        });
    }

    public static final LinearId ID = new LinearId(SSSTSProcessor.DESCRIPTOR.family, "documents", SSSTSProcessor.DESCRIPTOR.name);
    public static final String PATH = "sssts.doc";
    public static final String ITEMPATH = "sssts.doc.item";
    public static final String CONTEXTPATH = "sssts.doc.context";

    @Override
    protected String getItemPrefix() {
        return "SSSTS";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    protected SSSTSDocument createNewObject() {
        return new SSSTSDocument();
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
    public void openDocument(WorkspaceItem<SSSTSDocument> doc) {
        if (doc.isOpen()) {
            doc.getView().requestActive();
        } else {
            SSSTSTopComponent view = new SSSTSTopComponent(doc);
            doc.setView(view);
            view.open();
            view.requestActive();
        }
    }

    @Override
    public Class<SSSTSDocument> getItemClass() {
        return SSSTSDocument.class;
    }
}
