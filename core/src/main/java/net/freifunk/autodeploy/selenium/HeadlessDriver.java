/*
 * Freifunk Auto Deployer
 * Copyright (C) 2013, 2014 by Andreas Baldeau <andreas@baldeau.net>
 *
 *
 * For contributers see file CONTRIB.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 *
 * Uses Logback (http://logback.qos.ch/) which is dual licensed under EPL v1.0 and LGPL v2.1.
 * See http://logback.qos.ch/license.html for details.
 */
package net.freifunk.autodeploy.selenium;

import java.net.URL;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.WebClient;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * Headless {@link WebDriver}.
 *
 * @author Andreas Baldeau <andreas@baldeau.net>
 */
public class HeadlessDriver extends HtmlUnitDriver {

    /**
     * {@link CredentialsProvider} with (re-)settable username and password to be used in {@link HeadlessDriverCredentialsProvider}.
     *
     * @author Andreas Baldeau <andreas@baldeau.net>
     */
    static class HeadlessDriverCredentialsProvider implements CredentialsProvider {

        private String _username;
        private String _password;

        @Override
        public void setCredentials(final AuthScope authscope, final Credentials credentials) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Credentials getCredentials(final AuthScope authscope) {
            return _username == null || _password == null ? null : new UsernamePasswordCredentials(_username, _password);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        /**
         * Sets the credentials to use.
         */
        public void set(final String username, final String password) {
            _username = username;
            _password = password;
        }

        /**
         * Resets the credentials to use.
         */
        public void reset() {
            set(null, null);
        }
    }

    private HeadlessDriverCredentialsProvider _headlessDriverCredentialsProvider;

    public HeadlessDriver() {
        super();
        this.setJavascriptEnabled(true);
    }

    @Override
    protected void get(final URL url) {
        final String userInfo = url.getUserInfo();
        if (userInfo != null) {
            final Iterable<String> parts = Splitter.on(':').split(userInfo);
            Preconditions.checkState(Iterables.size(parts) == 2, "Expecting format user:password for userinfo.");
            final String username = Iterables.get(parts, 0);
            final String password = Iterables.getLast(parts);
            _headlessDriverCredentialsProvider.set(username, password);
        } else {
            _headlessDriverCredentialsProvider.reset();
        }
        super.get(url);
    }

    @Override
    protected WebClient modifyWebClient(final WebClient client) {
        _headlessDriverCredentialsProvider = new HeadlessDriverCredentialsProvider();
        client.setCredentialsProvider(_headlessDriverCredentialsProvider);

        return client;
    }

    /**
     * Gets the {@link WebClient} in use.
     */
    public WebClient getClient() {
        return super.getWebClient();
    }
}
