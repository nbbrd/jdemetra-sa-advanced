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

import java.io.Closeable;
import java.io.IOException;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 * @since 2.2.0
 */
public final class IoUtil {

    private IoUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void closeAll(@NonNull Closeable first, @NonNull Closeable second) throws IOException {
        try {
            first.close();
        } catch (IOException ex) {
            throw ensureClosed(ex, second);
        }
        second.close();
    }

    public static <T extends Throwable> T ensureClosed(@NonNull T ex, @NonNull Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException suppressed) {
            ex.addSuppressed(suppressed);
        }
        return ex;
    }

    @NonNull
    public static <T> Supplier<Iterable<T>> supplierOfServiceLoader(@NonNull Class<T> type) {
        return () -> ServiceLoader.load(type);
    }
}
