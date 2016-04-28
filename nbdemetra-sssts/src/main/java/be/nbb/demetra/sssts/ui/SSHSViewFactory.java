/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.sssts.ui;

import be.nbb.demetra.sssts.SSHSResults;
import be.nbb.demetra.sssts.SSHSSpecification;
import be.nbb.demetra.sssts.document.HtmlSSHS;
import be.nbb.demetra.sssts.document.SSHSDocument;
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
public class SSHSViewFactory extends SaDocumentViewFactory<SSHSSpecification, SSHSDocument> {

    public static final String SELECTION = "Selection", NOISE = "Noise", WK = "Wiener-Kolmogorov analysis";
    public static final String COMPONENTS = "Components";
    public static final String FINALS = "Final estimators";
    private static final Id MODEL_SELECTION = new LinearId(MODEL, SELECTION);
    private static final Id MODEL_NOISE = new LinearId(MODEL, NOISE);
    private static final Id MODEL_WK_COMPONENTS = new LinearId(MODEL, WK, COMPONENTS);
    private static final Id MODEL_WK_FINALS = new LinearId(MODEL, WK, FINALS);
    private static final AtomicReference<IProcDocumentViewFactory<SSHSDocument>> INSTANCE = new AtomicReference(new SSHSViewFactory());

    public static IProcDocumentViewFactory<SSHSDocument> getDefault() {
        return INSTANCE.get();
    }

    public static void setDefault(IProcDocumentViewFactory<SSHSDocument> factory) {
        INSTANCE.set(factory);
    }

    public static InformationExtractor<SSHSDocument, TsData> maresExtractor() {
        return MaresExtractor.INSTANCE;
    }

    public SSHSViewFactory() {
        registerDefault();
        registerFromLookup(SSHSDocument.class);
    }

    @Override
    public Id getPreferredView() {
        return MAIN_CHARTS_LOW;
    }

    @Deprecated
    public void registerDefault() {
        registerMainViews();
        registerLightPreprocessingViews();
        registerSSHSViews();
        registerBenchmarkingView();
        registerDiagnosticsViews();
    }

    //<editor-fold defaultstate="collapsed" desc="REGISTER MAIN VIEWS">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 100000 + 1000)
    public static class MainChartsLowFactory extends SaDocumentViewFactory.MainChartsLowFactory<SSHSDocument> {

        public MainChartsLowFactory() {
            super(SSHSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 100000 + 2000)
    public static class MainChartsHighFactory extends SaDocumentViewFactory.MainChartsHighFactory<SSHSDocument> {

        public MainChartsHighFactory() {
            super(SSHSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 100000 + 3000)
    public static class MainTableFactory extends SaDocumentViewFactory.MainTableFactory<SSHSDocument> {

        public MainTableFactory() {
            super(SSHSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER SI VIEW">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 100000 + 4000)
    public static class MainSiFactory extends SaDocumentViewFactory.MainSiFactory<SSHSDocument> {

        public MainSiFactory() {
            super(SSHSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER LIGHT PREPROCESSING VIEWS">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 200000 + 1000)
    public static class PreprocessingSummaryFactory extends SaDocumentViewFactory.PreprocessingSummaryFactory<SSHSDocument> {

        public PreprocessingSummaryFactory() {
            super(SSHSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 200000 + 2000)
    public static class PreprocessingRegsFactory extends SaDocumentViewFactory.PreprocessingRegsFactory<SSHSDocument> {

        public PreprocessingRegsFactory() {
            super(SSHSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 200000 + 3000)
    public static class PreprocessingDetFactory extends SaDocumentViewFactory.PreprocessingDetFactory<SSHSDocument> {

        public PreprocessingDetFactory() {
            super(SSHSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER MIXED AIRLINE">
    @Deprecated
    public void registerSSHSViews() {
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 300000 + 1000)
    public static class ModelSelectionFactory extends ItemFactory<SSHSResults> {

        public ModelSelectionFactory() {
            super(MODEL_SELECTION, new DefaultInformationExtractor<SSHSDocument, SSHSResults>() {
                @Override
                public SSHSResults retrieve(SSHSDocument source) {
                    return source.getDecompositionPart();
                }
            }, new HtmlItemUI<View, SSHSResults>() {
                @Override
                public IHtmlElement getHtmlElement(View host, SSHSResults information) {
                    return new HtmlSSHS(information.getAllModels(), information.getBestModelPosition());
                }
            });
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 300000 + 2000)
    public static class ModelNoiseFactory extends ItemFactory<CompositeResults> {

        public ModelNoiseFactory() {
            super(MODEL_NOISE, new DefaultInformationExtractor<SSHSDocument, CompositeResults>() {
                @Override
                public CompositeResults retrieve(SSHSDocument source) {
                    return source.getResults();
                }
            }, new ChartUI(SSHSResults.NOISE_DATA, SSHSResults.IRREGULAR));
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 300000 + 2500)
    public static class ModelResFactory extends ItemFactory<TsData> {

        public ModelResFactory() {
            super(MODEL_RES, maresExtractor(), new ResidualsUI());
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 300000 + 2600)
    public static class ModelResStatsFactory extends ItemFactory<NiidTests> {

        public ModelResStatsFactory() {
            super(MODEL_RES_STATS, new DefaultInformationExtractor<SSHSDocument, NiidTests>() {
                @Override
                public NiidTests retrieve(SSHSDocument source) {
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
    public static class BenchmarkingFactory extends SaDocumentViewFactory.BenchmarkingFactory<SSHSDocument> {

        public BenchmarkingFactory() {
            super(SSHSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER DIAGNOSTICS VIEWS">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 500000 + 1000)
    public static class DiagnosticsSummaryFactory extends SaDocumentViewFactory.DiagnosticsSummaryFactory<SSHSDocument> {

        public DiagnosticsSummaryFactory() {
            super(SSHSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 500000 + 2000)
    public static class DiagnosticsSpectrumResFactory extends SaDocumentViewFactory.DiagnosticsSpectrumResFactory<SSHSDocument> {

        public DiagnosticsSpectrumResFactory() {
            super(SSHSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 500000 + 3000)
    public static class DiagnosticsSpectrumIFactory extends SaDocumentViewFactory.DiagnosticsSpectrumIFactory<SSHSDocument> {

        public DiagnosticsSpectrumIFactory() {
            super(SSHSDocument.class);
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 500000 + 4000)
    public static class DiagnosticsSpectrumSaFactory extends SaDocumentViewFactory.DiagnosticsSpectrumSaFactory<SSHSDocument> {

        public DiagnosticsSpectrumSaFactory() {
            super(SSHSDocument.class);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="REGISTER SEASONALITY VIEW">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 500000 + 5000)
    public static class DiagnosticsSeasonalityFactory extends SaDocumentViewFactory.DiagnosticsSeasonalityFactory<SSHSDocument> {

        public DiagnosticsSeasonalityFactory() {
            super(SSHSDocument.class);
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
    public static class DiagnosticsSlidingTdFactory extends SaDocumentViewFactory.DiagnosticsSlidingTdFactory<SSHSDocument> {

        public DiagnosticsSlidingTdFactory() {
            super(SSHSDocument.class);
        }
    }

    //@ServiceProvider(service = ProcDocumentItemFactory.class)
    public static class DiagnosticsSlidingSaFactory extends SaDocumentViewFactory.DiagnosticsSlidingSaFactory<SSHSDocument> {

        public DiagnosticsSlidingSaFactory() {
            super(SSHSDocument.class);
        }
    }
    //</editor-fold>

    private static class ItemFactory<I> extends ComposedProcDocumentItemFactory<SSHSDocument, I> {

        public ItemFactory(Id itemId, InformationExtractor<? super SSHSDocument, I> informationExtractor, ItemUI<? extends IProcDocumentView<SSHSDocument>, I> itemUI) {
            super(SSHSDocument.class, itemId, informationExtractor, itemUI);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="EXTRACTORS IMPL">

    private static class MaresExtractor extends DefaultInformationExtractor<SSHSDocument, TsData> {

        private static final MaresExtractor INSTANCE = new MaresExtractor();

        @Override
        public TsData retrieve(SSHSDocument source) {
            return source.getDecompositionPart().getResiduals();
        }
    };
    //</editor-fold>
}
