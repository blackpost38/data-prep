/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.dataprep.i18n;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.AbstractLocaleContextResolver;

// This component must have the LOCALE_RESOLVER_BEAN_NAME as it is searched by this name in DispatcherServlet.initLocaleResolver:524
@Component(LOCALE_RESOLVER_BEAN_NAME)
public class DataprepLocale extends AbstractLocaleContextResolver {

    private static final Logger LOGGER = getLogger(DataprepLocale.class);

    private final Locale applicationLocale;

    private static final Locale DEFAULT_LOCALE = Locale.US;

    public DataprepLocale(@Value("${help.facets.language:}") String configuredLocale) {
        setDefaultLocale(Locale.US);
        this.applicationLocale = resolveApplicationLocale(configuredLocale);
    }

    @Override
    public LocaleContext resolveLocaleContext(HttpServletRequest request) {
        return new SimpleLocaleContext(applicationLocale);
    }

    @Override
    public void setLocaleContext(HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext) {
        throw new UnsupportedOperationException();
    }

    private static Locale resolveApplicationLocale(String configuredLocale) {
        Locale locale;
        if (StringUtils.isNotBlank(configuredLocale)) {
            try {
                locale = LocaleUtils.toLocale(configuredLocale);
                LOGGER.info("Setting application locale to configured {}", locale);
            } catch (IllegalArgumentException e) {
                // Illegal locale
                locale = DEFAULT_LOCALE;
                LOGGER.warn(
                        "Error parsing configured application locale: {}. Defaulting to {}. Locale must be in the form \"en_US\" or \"fr_CA\"",
                        configuredLocale, DEFAULT_LOCALE);
            }
        } else {
            locale = DEFAULT_LOCALE;
            LOGGER.info("Setting application locale to default value {}", DEFAULT_LOCALE);
        }
        return locale;
    }
}
