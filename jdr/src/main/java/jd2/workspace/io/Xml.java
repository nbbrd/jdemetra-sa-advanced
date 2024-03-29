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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Set of utilities related to XML.
 *
 * @author Philippe Charles
 */
public final class Xml {

    private Xml() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static interface Parser<T> {

        @NonNull
        default T parseChars(@NonNull CharSequence source) throws IOException {
            return parseReader(() -> new StringReader(source.toString()));
        }

        @NonNull
        default T parseFile(@NonNull File source) throws IOException {
            return parseStream(() -> new FileInputStream(source));
        }

        @NonNull
        default T parsePath(@NonNull Path source) throws IOException {
            Optional<File> file = IO.getFile(source);
            return file.isPresent()
                    ? parseFile(file.get())
                    : parseReader(() -> Files.newBufferedReader(source));
        }

        @NonNull
        default T parseReader(IO.Supplier<? extends Reader> source) throws IOException {
            try (Reader resource = source.getWithIO()) {
                return parseReader(resource);
            }
        }

        @NonNull
        default T parseStream(IO.Supplier<? extends InputStream> source) throws IOException {
            try (InputStream resource = source.getWithIO()) {
                return parseStream(resource);
            }
        }

        @NonNull
        T parseReader(@NonNull Reader resource) throws IOException;

        @NonNull
        T parseStream(@NonNull InputStream resource) throws IOException;
    }

    public static final class WrappedException extends IOException {

        public WrappedException(Exception ex) {
            super(ex);
        }
    }
}
