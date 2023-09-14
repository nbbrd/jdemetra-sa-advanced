/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jd2.workspace.io;

import org.checkerframework.checker.nullness.qual.NonNull;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Objects;

/**
 * @author Philippe Charles
 */
public final class Stax {
    private Stax() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Prevents XXE vulnerability by disabling features.
     *
     * @param factory non-null factory
     * @see https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#XMLInputFactory_.28a_StAX_parser.29
     */
    public static void preventXXE(@NonNull XMLInputFactory factory) {
        setFeature(factory, XMLInputFactory.SUPPORT_DTD, false);
        setFeature(factory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    }

    @FunctionalInterface
    public static interface FlowHandler<I, T> {

        @NonNull
        T parse(@NonNull I input, @NonNull Closeable onClose) throws IOException, XMLStreamException;

        @NonNull
        static <I, T> FlowHandler<I, T> of(@NonNull ValueHandler<I, T> handler) {
            return handler.asFlow();
        }
    }

    @FunctionalInterface
    public static interface ValueHandler<I, T> {

        @NonNull
        T parse(@NonNull I input) throws XMLStreamException;

        @NonNull
        default FlowHandler<I, T> asFlow() {
            return (input, onClose) -> {
                try (Closeable c = onClose) {
                    return parse(input);
                }
            };
        }
    }

    @lombok.Builder(toBuilder = true)
    public static final class StreamParser<T> implements Xml.Parser<T> {

        @NonNull
        public static <T> StreamParser<T> flowOf(@NonNull FlowHandler<XMLStreamReader, T> handler) {
            return StreamParser.<T>builder().handler(handler).build();
        }

        @NonNull
        public static <T> StreamParser<T> valueOf(@NonNull ValueHandler<XMLStreamReader, T> handler) {
            return StreamParser.<T>builder().handler(handler.asFlow()).build();
        }

        @lombok.NonNull
        private final FlowHandler<XMLStreamReader, T> handler;

        @lombok.NonNull
        @lombok.Builder.Default
        private final IO.Supplier<? extends XMLInputFactory> factory = XMLInputFactory::newFactory;

        @lombok.Builder.Default
        private boolean preventXXE = true;

        @Override
        public T parseReader(IO.Supplier<? extends Reader> source) throws IOException {
            Reader resource = source.getWithIO();
            return parse(o -> o.createXMLStreamReader(resource), resource);
        }

        @Override
        public T parseStream(IO.Supplier<? extends InputStream> source) throws IOException {
            InputStream resource = source.getWithIO();
            return parse(o -> o.createXMLStreamReader(resource), resource);
        }

        @Override
        public T parseReader(Reader resource) throws IOException {
            Objects.requireNonNull(resource);
            return parse(o -> o.createXMLStreamReader(resource), IO.Runnable.noOp().asCloseable());
        }

        @Override
        public T parseStream(InputStream resource) throws IOException {
            Objects.requireNonNull(resource);
            return parse(o -> o.createXMLStreamReader(resource), IO.Runnable.noOp().asCloseable());
        }

        @NonNull
        public T parse(@NonNull XMLStreamReader input, @NonNull Closeable onClose) throws IOException {
            try {
                return handler.parse(input, onClose);
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException | IOException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
        }

        private T parse(XSupplier<XMLStreamReader> supplier, Closeable onClose) throws IOException {
            try {
                XMLStreamReader input = supplier.create(getEngine());
                return parse(input, () -> closeBoth(input, onClose));
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException | IOException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
        }

        private XMLInputFactory getEngine() throws IOException {
            XMLInputFactory result = factory.getWithIO();
            if (preventXXE) {
                preventXXE(result);
            }
            return result;
        }

        private static void closeBoth(XMLStreamReader input, Closeable onClose) throws IOException {
            try {
                input.close();
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
            onClose.close();
        }
    }

    @lombok.Builder(toBuilder = true)
    public static final class EventParser<T> implements Xml.Parser<T> {

        @NonNull
        public static <T> EventParser<T> flowOf(@NonNull FlowHandler<XMLEventReader, T> handler) {
            return EventParser.<T>builder().handler(handler).build();
        }

        @NonNull
        public static <T> EventParser<T> valueOf(@NonNull ValueHandler<XMLEventReader, T> handler) {
            return EventParser.<T>builder().handler(handler.asFlow()).build();
        }

        @lombok.NonNull
        private final FlowHandler<XMLEventReader, T> handler;

        @lombok.NonNull
        @lombok.Builder.Default
        private final IO.Supplier<? extends XMLInputFactory> factory = XMLInputFactory::newFactory;

        @lombok.Builder.Default
        private boolean preventXXE = true;

        @Override
        public T parseReader(IO.Supplier<? extends Reader> source) throws IOException {
            Reader resource = source.getWithIO();
            return parse(o -> o.createXMLEventReader(resource), resource);
        }

        @Override
        public T parseStream(IO.Supplier<? extends InputStream> source) throws IOException {
            InputStream resource = source.getWithIO();
            return parse(o -> o.createXMLEventReader(resource), resource);
        }

        @Override
        public T parseReader(Reader resource) throws IOException {
            Objects.requireNonNull(resource);
            return parse(o -> o.createXMLEventReader(resource), IO.Runnable.noOp().asCloseable());
        }

        @Override
        public T parseStream(InputStream resource) throws IOException {
            Objects.requireNonNull(resource);
            return parse(o -> o.createXMLEventReader(resource), IO.Runnable.noOp().asCloseable());
        }

        private T parse(XSupplier<XMLEventReader> supplier, Closeable onClose) throws IOException {
            try {
                XMLEventReader input = supplier.create(getEngine());
                return parse(input, () -> closeBoth(input, onClose));
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException | IOException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
        }

        private T parse(XMLEventReader input, Closeable onClose) throws IOException {
            try {
                return handler.parse(input, onClose);
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException | IOException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
        }

        private XMLInputFactory getEngine() throws IOException {
            XMLInputFactory result = factory.getWithIO();
            if (preventXXE) {
                preventXXE(result);
            }
            return result;
        }

        private static void closeBoth(XMLEventReader input, Closeable onClose) throws IOException {
            try {
                input.close();
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
            onClose.close();
        }
    }

    @FunctionalInterface
    private static interface XSupplier<T> {

        T create(XMLInputFactory input) throws XMLStreamException;
    }

    private static void setFeature(XMLInputFactory factory, String feature, boolean value) {
        if (factory.isPropertySupported(feature)
                && ((Boolean) factory.getProperty(feature)) != value) {
            factory.setProperty(feature, value);
        }
    }
}
