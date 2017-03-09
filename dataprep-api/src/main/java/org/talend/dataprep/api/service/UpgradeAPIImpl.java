// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.talend.daikon.annotation.ServiceImplementation;
import org.talend.daikon.security.CryptoHelper;
import org.talend.daikon.token.TokenGenerator;
import org.talend.dataprep.api.service.info.VersionService;
import org.talend.dataprep.api.service.upgrade.UpgradeServerVersion;
import org.talend.services.dataprep.api.UpgradeAPI;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;

@ServiceImplementation
public class UpgradeAPIImpl extends APIService implements UpgradeAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeAPIImpl.class);

    @Autowired
    VersionService service;

    @Value("${upgrade.location:}")
    String upgradeVersionLocation;

    private String token;

    private static String toString(List<UpgradeServerVersion> versions) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        for (UpgradeServerVersion version : versions) {
            builder.append(version.getVersion()).append(' ');
        }
        builder.append(']');
        return builder.toString();
    }

    static Version fromInternal(org.talend.dataprep.info.Version internalVersion) {
        String versionId = internalVersion.getVersionId();
        final String versionAsString = StringUtils.substringBefore(versionId, "-");
        try {
            return Version.valueOf(versionAsString);
        } catch (IllegalArgumentException | ParseException e) {
            LOGGER.info("Couldn't parse version {}. Message was: {}", versionId, e.getMessage());
            return Version.forIntegers(0);
        }
    }

    @PostConstruct
    public void init() {
        token = TokenGenerator.generateMachineToken(new CryptoHelper("DataPrepIsSoCool"));
        LOGGER.debug("Installation token: {}", token);
    }

    // Here for unit test purposes
    void setUpgradeVersionLocation(String upgradeVersionLocation) {
        this.upgradeVersionLocation = upgradeVersionLocation;
    }

    @Override
    public Stream<UpgradeServerVersion> check() {

        // defensive programming
        if (StringUtils.isBlank(upgradeVersionLocation)) {
            return Stream.empty();
        }

        try {
            // Get current version
            final Version parsedCurrentVersion = fromInternal(service.version());

            // POST to URL that serves a JSON Version object
            LOGGER.debug("Contacting upgrade server @ '{}'", upgradeVersionLocation);
            List<UpgradeServerVersion> versions = fetchServerUpgradeVersions(service.version());
            LOGGER.debug("{} available version(s) returned by update server: {}", versions.size(), toString(versions));

            // Compare current version with available and filter new versions
            return versions.stream().filter(v -> Version.valueOf(v.getVersion()).greaterThan(parsedCurrentVersion));
        } catch (Exception e) {
            LOGGER.error("Unable to check for new version (message: {}).", e.getMessage());
            LOGGER.debug("Exception occurred during new version check. ", e);
            return Stream.empty();
        }
    }

    private List<UpgradeServerVersion> fetchServerUpgradeVersions(org.talend.dataprep.info.Version version) throws IOException {
        HttpClient client = new HttpClient();
        final PostMethod post = new PostMethod(upgradeVersionLocation);
        final String response;
        final StringWriter content = new StringWriter();
        try (final JsonGenerator generator = mapper.getFactory().createGenerator(content)) {
            generator.writeStartObject();
            generator.writeStringField("version", version.getVersionId());
            generator.writeStringField("id", token);
            generator.writeEndObject();
            generator.flush();

            post.setRequestEntity(new StringRequestEntity(content.toString(), MediaType.APPLICATION_JSON.getType(), UTF_8.name()));
            client.executeMethod(post);
            response = IOUtils.toString(post.getResponseBodyAsStream());
        } finally {
            post.releaseConnection();
        }

        // Read upgrade server response
        return mapper.readerFor(new TypeReference<List<UpgradeServerVersion>>() {
        }).readValue(response);
    }

}
