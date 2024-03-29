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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Objects;

/**
 * @author Philippe Charles
 */
public final class Jaxb {

    private Jaxb() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @NonNull
    public static Unmarshaller createUnmarshaller(@NonNull Class<?> type) throws IOException {
        Objects.requireNonNull(type);
        try {
            return JAXBContext.newInstance(type).createUnmarshaller();
        } catch (JAXBException ex) {
            throw new Xml.WrappedException(ex);
        }
    }

    @NonNull
    public static Unmarshaller createUnmarshaller(@NonNull JAXBContext context) throws IOException {
        Objects.requireNonNull(context);
        try {
            return context.createUnmarshaller();
        } catch (JAXBException ex) {
            throw new Xml.WrappedException(ex);
        }
    }

    @lombok.Builder(toBuilder = true)
    public static final class Parser<T> implements Xml.Parser<T> {

        @NonNull
        public static <T> Parser<T> of(@NonNull Class<T> type) throws IOException {
            Objects.requireNonNull(type);
            return Parser.<T>builder().factory(() -> createUnmarshaller(type)).build();
        }

        @NonNull
        public static <T> Parser<T> of(@NonNull JAXBContext context) throws IOException {
            Objects.requireNonNull(context);
            return Parser.<T>builder().factory(() -> createUnmarshaller(context)).build();
        }

        @lombok.NonNull
        private final IO.Supplier<? extends Unmarshaller> factory;

        @lombok.Builder.Default
        private boolean preventXXE = true;

        @lombok.NonNull
        @lombok.Builder.Default
        private final IO.Supplier<? extends XMLInputFactory> xxeFactory = Parser::getStaxFactory;

        @Override
        public T parseFile(File source) throws IOException {
            Objects.requireNonNull(source);
            Unmarshaller engine = factory.getWithIO();

            return preventXXE
                    ? parseFileXXE(engine, source, xxeFactory.getWithIO())
                    : parseFile(engine, source);
        }

        @Override
        public T parseReader(Reader resource) throws IOException {
            Objects.requireNonNull(resource);
            Unmarshaller engine = factory.getWithIO();

            return preventXXE
                    ? parseReaderXXE(engine, resource, xxeFactory.getWithIO())
                    : parseReader(engine, resource);
        }

        @Override
        public T parseStream(InputStream resource) throws IOException {
            Objects.requireNonNull(resource);
            Unmarshaller engine = factory.getWithIO();

            return preventXXE
                    ? parseStreamXXE(engine, resource, xxeFactory.getWithIO())
                    : parseStream(engine, resource);
        }

        private static XMLInputFactory getStaxFactory() {
            XMLInputFactory result = XMLInputFactory.newFactory();
            Stax.preventXXE(result);
            return result;
        }

        private static <T> T parseFile(Unmarshaller engine, File source) throws IOException {
            try {
                return (T) engine.unmarshal(source);
            } catch (JAXBException ex) {
                throw new Xml.WrappedException(ex);
            }
        }

        private static <T> T parseReader(Unmarshaller engine, Reader resource) throws IOException {
            try {
                return (T) engine.unmarshal(resource);
            } catch (JAXBException ex) {
                throw new Xml.WrappedException(ex);
            }
        }

        private static <T> T parseStream(Unmarshaller engine, InputStream resource) throws IOException {
            try {
                return (T) engine.unmarshal(resource);
            } catch (JAXBException ex) {
                throw new Xml.WrappedException(ex);
            }
        }

        private static <T> T parseFileXXE(Unmarshaller engine, File source, XMLInputFactory xxe) throws IOException {
            try (FileInputStream resource = new FileInputStream(source)) {
                XMLStreamReader reader = xxe.createXMLStreamReader(resource);
                try {
                    return (T) engine.unmarshal(reader);
                } finally {
                    reader.close();
                }
            } catch (XMLStreamException | JAXBException ex) {
                throw new Xml.WrappedException(ex);
            }
        }

        private static <T> T parseReaderXXE(Unmarshaller engine, Reader resource, XMLInputFactory xxe) throws IOException {
            try {
                XMLStreamReader reader = xxe.createXMLStreamReader(resource);
                try {
                    return (T) engine.unmarshal(reader);
                } finally {
                    reader.close();
                }
            } catch (XMLStreamException | JAXBException ex) {
                throw new Xml.WrappedException(ex);
            }
        }

        private static <T> T parseStreamXXE(Unmarshaller engine, InputStream resource, XMLInputFactory xxe) throws IOException {
            try {
                XMLStreamReader reader = xxe.createXMLStreamReader(resource);
                try {
                    return (T) engine.unmarshal(reader);
                } finally {
                    reader.close();
                }
            } catch (XMLStreamException | JAXBException ex) {
                throw new Xml.WrappedException(ex);
            }
        }
    }
}
