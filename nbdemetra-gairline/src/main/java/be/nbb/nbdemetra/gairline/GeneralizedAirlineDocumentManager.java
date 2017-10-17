/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.nbdemetra.gairline;

import be.nbb.nbdemetra.gairline.descriptors.GeneralizedAirlineSpecUI;
import be.nbb.nbdemetra.gairline.ui.GeneralizedAirlineViewFactory;
import be.nbb.demetra.gairline.GeneralizedAirlineSpecification;
import be.nbb.demetra.gairline.document.GeneralizedAirlineDocument;
import be.nbb.demetra.gairline.document.GeneralizedAirlineProcessor;
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
        position = 1820)
public class GeneralizedAirlineDocumentManager extends AbstractWorkspaceTsItemManager<GeneralizedAirlineSpecification, GeneralizedAirlineDocument> {

    static {
        SaManager.instance.add(new GeneralizedAirlineProcessor());

        DocumentUIServices.getDefault().register(GeneralizedAirlineDocument.class, new DocumentUIServices.AbstractUIFactory<GeneralizedAirlineSpecification, GeneralizedAirlineDocument>() {
            SaManager mgr;

            @Override
            public IProcDocumentView<GeneralizedAirlineDocument> getDocumentView(GeneralizedAirlineDocument document) {
                return GeneralizedAirlineViewFactory.getDefault().create(document);
            }

            @Override
            public IObjectDescriptor<GeneralizedAirlineSpecification> getSpecificationDescriptor(GeneralizedAirlineDocument doc) {
                return new GeneralizedAirlineSpecUI(doc.getSpecification().clone());
            }
        });
    }
    public static final LinearId ID = new LinearId(GeneralizedAirlineProcessor.DESCRIPTOR.family, "documents", GeneralizedAirlineProcessor.DESCRIPTOR.name);
    public static final String PATH = "gairline.doc";
    public static final String ITEMPATH = "gairline.doc.item";
    public static final String CONTEXTPATH = "gairline.doc.context";

    @Override
    protected String getItemPrefix() {
        return "GeneralizedAirlineDoc";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    protected GeneralizedAirlineDocument createNewObject() {
        return new GeneralizedAirlineDocument();
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
    public void openDocument(WorkspaceItem<GeneralizedAirlineDocument> doc) {
        if (doc.isOpen()) {
            doc.getView().requestActive();
        } else {
            GeneralizedAirlineTopComponent view = new GeneralizedAirlineTopComponent(doc);
            doc.setView(view);
            view.open();
            view.requestActive();
        }
    }

    @Override
    public Class<GeneralizedAirlineDocument> getItemClass() {
        return GeneralizedAirlineDocument.class;
    }
}
