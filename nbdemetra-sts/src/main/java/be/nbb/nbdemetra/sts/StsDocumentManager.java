/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.nbdemetra.sts;

import be.nbb.demetra.sts.StsSpecification;
import be.nbb.demetra.sts.document.StsDocument;
import be.nbb.demetra.sts.document.StsProcessor;
import be.nbb.nbdemetra.sts.descriptors.StsSpecUI;
import be.nbb.nbdemetra.sts.ui.StructuralModelViewFactory;
import ec.nbdemetra.ui.DocumentUIServices;
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
position = 1800)
public class StsDocumentManager extends AbstractWorkspaceTsItemManager<StsSpecification, StsDocument> {

    static {
        SaManager.instance.add(new StsProcessor());
        DocumentUIServices.getDefault().register(StsDocument.class, new DocumentUIServices.AbstractUIFactory<StsSpecification, StsDocument>() {

            @Override
            public IProcDocumentView<StsDocument> getDocumentView(StsDocument document) {
                return StructuralModelViewFactory.getDefault().create(document);
            }

            @Override
            public IObjectDescriptor<StsSpecification> getSpecificationDescriptor(StsDocument doc) {
                return new StsSpecUI(doc.getSpecification().clone());
            }
        });
    }
    public static final LinearId ID = new LinearId(StsProcessor.DESCRIPTOR.family, "documents", StsProcessor.DESCRIPTOR.name);
    public static final String PATH = "sts.doc";
    public static final String ITEMPATH = "sts.doc.item";
    public static final String CONTEXTPATH = "sts.doc.context";

    @Override
    protected String getItemPrefix() {
        return "StsDoc";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    protected StsDocument createNewObject() {
        return new StsDocument();
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
        return Status.Acceptable;
    }

    @Override
    public void openDocument(WorkspaceItem<StsDocument> doc) {
        if (doc.isOpen()) {
            doc.getView().requestActive();
        } else {
            StsTopComponent view = new StsTopComponent(doc);
            doc.setView(view);
            view.open();
            view.requestActive();
        }
    }

    @Override
    public Class<StsDocument> getItemClass() {
        return StsDocument.class;
    }
}
