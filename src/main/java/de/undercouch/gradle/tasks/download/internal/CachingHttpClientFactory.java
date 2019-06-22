// Copyright 2016 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.gradle.tasks.download.internal;

import de.undercouch.gradle.tasks.download.org.apache.http.HttpHost;
import de.undercouch.gradle.tasks.download.org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link HttpClientFactory} that caches created clients
 * until the {@link #close()} method is called.
 * @author Michel Kraemer
 */
public class CachingHttpClientFactory extends DefaultHttpClientFactory {
    private Map<CacheKey, CloseableHttpClient> cachedClients = new HashMap<>();

    @Override
    public CloseableHttpClient createHttpClient(HttpHost httpHost,
            boolean acceptAnyCertificate) {
        CacheKey key = new CacheKey(httpHost, acceptAnyCertificate);
        CloseableHttpClient c = cachedClients.get(key);
        if (c == null) {
            c = super.createHttpClient(httpHost, acceptAnyCertificate);
            cachedClients.put(key, c);
        }
        return c;
    }

    /**
     * Close all HTTP clients created by this factory
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        for (CloseableHttpClient c : cachedClients.values()) {
            c.close();
        }
        cachedClients.clear();
    }
    
    /**
     * A key in the map of cached HTTP clients
     */
    private static class CacheKey {
        private final HttpHost httpHost;
        private final boolean acceptAnyCertificate;

        CacheKey(HttpHost httpHost, boolean acceptAnyCertificate) {
            this.httpHost = httpHost;
            this.acceptAnyCertificate = acceptAnyCertificate;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (acceptAnyCertificate ? 1231 : 1237);
            result = prime * result + ((httpHost == null) ? 0 : httpHost.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CacheKey other = (CacheKey)obj;
            if (acceptAnyCertificate != other.acceptAnyCertificate) {
                return false;
            }
            if (httpHost == null) {
                return other.httpHost == null;
            }
            return httpHost.equals(other.httpHost);
        }
    }
}