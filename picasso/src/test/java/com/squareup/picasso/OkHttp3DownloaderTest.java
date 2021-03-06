/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(
    sdk = 23 // Works around https://github.com/robolectric/robolectric/issues/2566.
)
public final class OkHttp3DownloaderTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule public MockWebServer server = new MockWebServer();

  @Test public void works() throws Exception {
    server.enqueue(new MockResponse().setBody("Hi"));
    okhttp3.Request request = new Request.Builder().url(server.url("/")).build();
    Downloader downloader = new OkHttp3Downloader(new OkHttpClient(), null, true);
    Response response = downloader.load(request);
    assertThat(response.body().string()).isEqualTo("Hi");
  }

  @Test public void shutdownClosesCacheIfNotShared() throws Exception {
    Cache cache = new Cache(temporaryFolder.getRoot(), 100);
    OkHttpClient client = new OkHttpClient.Builder().cache(cache).build();
    Downloader downloader = new OkHttp3Downloader(client, cache, false);
    downloader.shutdown();
    assertThat(cache.isClosed()).isTrue();
  }

  @Test public void shutdownDoesNotCloseCacheIfSharedClient() throws Exception {
    okhttp3.Cache cache = new okhttp3.Cache(temporaryFolder.getRoot(), 100);
    OkHttpClient client = new OkHttpClient.Builder().cache(cache).build();
    new OkHttp3Downloader(client, cache, true).shutdown();
    assertThat(cache.isClosed()).isFalse();
  }
}
