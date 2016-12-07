/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.sssts.ui;

import be.nbb.demetra.sssts.SSSTSResults;
import be.nbb.demetra.sssts.SSSTSSpecification;
import be.nbb.demetra.sssts.document.HtmlSSSTS;
import be.nbb.demetra.sssts.document.SSSTSDocument;
import ec.ui.view.tsprocessing.ChartUI;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.analysis.SlidingSpans;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.*;
import ec.ui.view.tsprocessing.*;
import ec.ui.view.tsprocessing.sa.SaDocumentViewFactory;
import static ec.ui.view.tsprocessing.sa.SaDocumentViewFactory.MAIN_CHARTS_LOW;
import static ec.ui.view.tsprocessing.sa.SaDocumentViewFactory.ssExtractor;
import java.util.concurrent.atomic.AtomicReference;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
public class SSSTSViewFactory extends SaDocumentViewFactory<SSSTSSpecification, SSSTSDocument> {

    public static final String SELECTION = "Selection", NOISE = "Noise", WK = "Wiener-Kolmogorov analysis";
    public static final String COMPONENTS = "Components";
    public static final String FINALS = "Final estimators";
    private static final Id MODEL_SELECTION = new LinearId(MODEL, SELECTION);
//    private static final Id MODEL_NOISE = new LinearId(MODEL, NOISE);
//    private static final Id MODEL_WK_COMPONENTS = new LinearId(MODEL, WK, COMPONENTS);
//    private static final Id MODEL_WK_FINALS = new LinearId(MODEL, WK, FINALS);
    private static final AtomicReference<IProcDocumentViewFactory<SSSTSDocument>> INSTANCE = new AtomicReference(new SSSTSViewFactory());

    public static IProcDocumentViewFactory<SSSTSDocument> getDefault() {
        return INSTANCE.get();
    }

    public static void setDefault(IProcDocumentViewFactory<SSSTSDocument> factory) {
        INSTANCE.set(factory);
    }

    public static InformationExtractor<SSSTSDocument, TsData> maresExtractor() {
        return MaresExtractor.INSTANCE;
    }

    public SSSTSViewFactory() {
        registerDefault();
        registerFromLookup(SSSTSDocument.class);
    }

    @Override
    public Id getPreferredView() {
        return MAIN_CHARTS_LOW;
    }

    @Deprecated
    public void registerDefault() {
        registerMainViews();
        registerLightPreprocessingViews();
        registerSSSTSViews();
        registerBenchmarkingView();
        registerDiagnosticsViews();
    }

    //<editor-fold defaultstate="collapsed" desc="REGISTER MAIN VIEWS">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 100000 + 1000)
    public static class MainChartsLowFactory extends SaDocumentViewFactory.MainChartsLowFactory<SSSTSDocument> {

        public MainChartsLowFactory() {
            super(SSSTSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 100000 + 2000)
    public static class MainChartsHighFactory extends SaDocumentViewFactory.MainChartsHighFactory<SSSTSDocument> {

        public MainChartsHighFactory() {
            super(SSSTSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 100000 + 3000)
    public static class MainTableFactory extends SaDocumentViewFactory.MainTableFactory<SSSTSDocument> {

        public MainTableFactory() {
            super(SSSTSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER SI VIEW">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 100000 + 4000)
    public static class MainSiFactory extends SaDocumentViewFactory.MainSiFactory<SSSTSDocument> {

        public MainSiFactory() {
            super(SSSTSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER LIGHT PREPROCESSING VIEWS">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 200000 + 1000)
    public static class PreprocessingSummaryFactory extends SaDocumentViewFactory.PreprocessingSummaryFactory<SSSTSDocument> {

        public PreprocessingSummaryFactory() {
            super(SSSTSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 200000 + 2000)
    public static class PreprocessingRegsFactory extends SaDocumentViewFactory.PreprocessingRegsFactory<SSSTSDocument> {

        public PreprocessingRegsFactory() {
            super(SSSTSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 200000 + 3000)
    public static class PreprocessingDetFactory extends SaDocumentViewFactory.PreprocessingDetFactory<SSSTSDocument> {

        public PreprocessingDetFactory() {
            super(SSSTSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER MIXED AIRLINE">
    @Deprecated
    public void registerSSSTSViews() {
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 300000 + 1000)
    public static class ModelSelectionFactory extends ItemFactory<SSSTSResults> {

        public ModelSelectionFactory() {
            super(MODEL_SELECTION, new DefaultInformationExtractor<SSSTSDocument, SSSTSResults>() {
                @Override
                public SSSTSResults retrieve(SSSTSDocument source) {
                    return source.getDecompositionPart();
                }
            }, new HtmlItemUI<View, SSSTSResults>() {
                @Override
                public IHtmlElement getHtmlElement(View host, SSSTSResults information) {
                    return new HtmlSSSTS(information.getAllModels(), information.getBestModelPosition());
                }
            });
        }
    }

//    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 300000 + 2000)
//    public static class ModelNoiseFactory extends ItemFactory<CompositeResults> {
//
//        public ModelNoiseFactory() {
//            super(MODEL_NOISE, new DefaultInformationExtractor<SSSTSDocument, CompositeResults>() {
//                @Override
//                public CompositeResults retrieve(SSSTSDocument source) {
//                    return source.getResults();
//                }
//            }, new ChartUI(SSSTSResults.NOISE_DATA, SSSTSResults.IRREGULAR));
//        }
//    }
//
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 300000 + 2500)
    public static class ModelResFactory extends ItemFactory<TsData> {

        public ModelResFactory() {
            super(MODEL_RES, maresExtractor(), new ResidualsUI());
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 300000 + 2600)
    public static class ModelResStatsFactory extends ItemFactory<NiidTests> {

        public ModelResStatsFactory() {
            super(MODEL_RES_STATS, new DefaultInformationExtractor<SSSTSDocument, NiidTests>() {
                @Override
                public NiidTests retrieve(SSSTSDocument source) {
                    TsData res = source.getDecompositionPart().getResiduals();
                    int np = source.getPreprocessingPart() != null
                            ? source.getPreprocessingPart().description.getArimaComponent().getFreeParametersCount()
                            : 0;
                    return new NiidTests(res.getValues(), res.getFrequency().intValue(), np, true);
                }
            }, new ResidualsStatsUI());
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 300000 + 2700)
    public static class ModelResDistFactory extends ItemFactory<TsData> {

        public ModelResDistFactory() {
            super(MODEL_RES_DIST, maresExtractor(), new ResidualsDistUI());
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER BENCHMARKING VIEW">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 400000 + 1000)
    public static class BenchmarkingFactory extends SaDocumentViewFactory.BenchmarkingFactory<SSSTSDocument> {

        public BenchmarkingFactory() {
            super(SSSTSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER DIAGNOSTICS VIEWS">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 500000 + 1000)
    public static class DiagnosticsSummaryFactory extends SaDocumentViewFactory.DiagnosticsSummaryFactory<SSSTSDocument> {

        public DiagnosticsSummaryFactory() {
            super(SSSTSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 500000 + 2000)
    public static class DiagnosticsSpectrumResFactory extends SaDocumentViewFactory.DiagnosticsSpectrumResFactory<SSSTSDocument> {

        public DiagnosticsSpectrumResFactory() {
            super(SSSTSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 500000 + 3000)
    public static class DiagnosticsSpectrumIFactory extends SaDocumentViewFactory.DiagnosticsSpectrumIFactory<SSSTSDocument> {

        public DiagnosticsSpectrumIFactory() {
            super(SSSTSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 500000 + 4000)
    public static class DiagnosticsSpectrumSaFactory extends SaDocumentViewFactory.DiagnosticsSpectrumSaFactory<SSSTSDocument> {

        public DiagnosticsSpectrumSaFactory() {
            super(SSSTSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER SEASONALITY VIEW">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 500000 + 5000)
    public static class DiagnosticsSeasonalityFactory extends SaDocumentViewFactory.DiagnosticsSeasonalityFactory<SSSTSDocument> {

        public DiagnosticsSeasonalityFactory() {
            super(SSSTSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER SLIDING SPANS">
    //@ServiceProvider(service = ProcDocumentItemFactory.class)
    public static class DiagnosticsSlidingSummaryFactory extends ItemFactory<SlidingSpans> {

        public DiagnosticsSlidingSummaryFactory() {
            super(DIAGNOSTICS_SLIDING_SUMMARY, ssExtractor(), new SlidingSpansUI(ModellingDictionary.S, ModellingDictionary.SI_CMP));
        }
    }

    //@ServiceProvider(service = ProcDocumentItemFactory.class)
    public static class DiagnosticsSlidingSeasFactory extends ItemFactory<SlidingSpans> {

        public DiagnosticsSlidingSeasFactory() {
            super(DIAGNOSTICS_SLIDING_SEAS, ssExtractor(), new SlidingSpansDetailUI(ModellingDictionary.S_CMP));
        }
    }

    //@ServiceProvider(service = ProcDocumentItemFactory.class)
    public static class DiagnosticsSlidingTdFactory extends SaDocumentViewFactory.DiagnosticsSlidingTdFactory<SSSTSDocument> {

        public DiagnosticsSlidingTdFactory() {
            super(SSSTSDocument.class);
        }
    }

    //@ServiceProvider(service = ProcDocumentItemFactory.class)
    public static class DiagnosticsSlidingSaFactory extends SaDocumentViewFactory.DiagnosticsSlidingSaFactory<SSSTSDocument> {

        public DiagnosticsSlidingSaFactory() {
            super(SSSTSDocument.class);
        }
    }
    //</editor-fold>

    private static class ItemFactory<I> extends ComposedProcDocumentItemFactory<SSSTSDocument, I> {

        public ItemFactory(Id itemId, InformationExtractor<? super SSSTSDocument, I> informationExtractor, ItemUI<? extends IProcDocumentView<SSSTSDocument>, I> itemUI) {
            super(SSSTSDocument.class, itemId, informationExtractor, itemUI);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="EXTRACTORS IMPL">

    private static class MaresExtractor extends DefaultInformationExtractor<SSSTSDocument, TsData> {

        private static final MaresExtractor INSTANCE = new MaresExtractor();

        @Override
        public TsData retrieve(SSSTSDocument source) {
            return source.getDecompositionPart().getResiduals();
        }
    };
    //</editor-fold>
}
