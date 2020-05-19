/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package demetra.workspace.io;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.experimental.UtilityClass
public class JaxbUtil {

    @NonNull
    public Object unmarshal(@NonNull Path file, @NonNull JAXBContext context) throws JAXBException, IOException {
        try {
            return Jaxb.Parser.of(context).parsePath(file);
        } catch (Xml.WrappedException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof JAXBException) {
                throw (JAXBException) cause;
            }
            throw ex;
        }
    }

    @NonNull
    public Object unmarshal(@NonNull Path file, @NonNull Unmarshaller unmarshaller) throws JAXBException, IOException {
        try {
            return Jaxb.Parser.builder().factory(() -> unmarshaller).build().parsePath(file);
        } catch (Xml.WrappedException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof JAXBException) {
                throw (JAXBException) cause;
            }
            throw ex;
        }
    }

    public void marshal(@NonNull Path file, @NonNull JAXBContext context, @NonNull Object jaxbElement, boolean formatted) throws JAXBException, IOException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
        marshal(file, marshaller, jaxbElement);
    }

    public void marshal(@NonNull Path file, @NonNull Marshaller marshaller, @NonNull Object jaxbElement) throws JAXBException, IOException {
        Optional<File> localFile = IO.getFile(file);
        if (localFile.isPresent()) {
            marshaller.marshal(jaxbElement, localFile.get());
        } else {
            try (Writer writer = Files.newBufferedWriter(file)) {
                marshaller.marshal(jaxbElement, writer);
            }
        }
    }

    @NonNull
    public JAXBContext createContext(@NonNull Class<?> type) {
        try {
            return JAXBContext.newInstance(type);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <X> void forSingle(@Nullable X item, @NonNull Consumer<? super X> action) {
        Objects.requireNonNull(action, "action");
        if (item != null) {
            action.accept(item);
        }
    }

    public <X> void forEach(@Nullable X[] array, @NonNull Consumer<? super X> action) {
        Objects.requireNonNull(action, "action");
        if (array != null) {
            for (X o : array) {
                action.accept(o);
            }
        }
    }
}
