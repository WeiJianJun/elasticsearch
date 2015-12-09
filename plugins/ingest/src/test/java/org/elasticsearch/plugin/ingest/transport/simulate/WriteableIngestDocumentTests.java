/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.plugin.ingest.transport.simulate;

import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class WriteableIngestDocumentTests extends ESTestCase {

    public void testEqualsAndHashcode() throws Exception {
        Map<String, String> esMetadata = new HashMap<>();
        int numFields = randomIntBetween(1, IngestDocument.MetaData.values().length);
        for (int i = 0; i < numFields; i++) {
            esMetadata.put(randomFrom(IngestDocument.MetaData.values()).getFieldName(), randomAsciiOfLengthBetween(5, 10));
        }
        Map<String, String> ingestMetadata = new HashMap<>();
        numFields = randomIntBetween(1, 5);
        for (int i = 0; i < numFields; i++) {
            ingestMetadata.put(randomAsciiOfLengthBetween(5, 10), randomAsciiOfLengthBetween(5, 10));
        }
        Map<String, Object> document = RandomDocumentPicks.randomDocument(random());
        WriteableIngestDocument ingestDocument = new WriteableIngestDocument(new IngestDocument(esMetadata, document, ingestMetadata));

        boolean changed = false;
        Map<String, String> otherEsMetadata;
        if (randomBoolean()) {
            otherEsMetadata = new HashMap<>();
            numFields = randomIntBetween(1, IngestDocument.MetaData.values().length);
            for (int i = 0; i < numFields; i++) {
                otherEsMetadata.put(randomFrom(IngestDocument.MetaData.values()).getFieldName(), randomAsciiOfLengthBetween(5, 10));
            }
            changed = true;
        } else {
            otherEsMetadata = Collections.unmodifiableMap(esMetadata);
        }

        Map<String, String> otherIngestMetadata;
        if (randomBoolean()) {
            otherIngestMetadata = new HashMap<>();
            numFields = randomIntBetween(1, 5);
            for (int i = 0; i < numFields; i++) {
                otherIngestMetadata.put(randomAsciiOfLengthBetween(5, 10), randomAsciiOfLengthBetween(5, 10));
            }
            changed = true;
        } else {
            otherIngestMetadata = Collections.unmodifiableMap(ingestMetadata);
        }

        Map<String, Object> otherDocument;
        if (randomBoolean()) {
            otherDocument = RandomDocumentPicks.randomDocument(random());
            changed = true;
        } else {
            otherDocument = Collections.unmodifiableMap(document);
        }

        WriteableIngestDocument otherIngestDocument = new WriteableIngestDocument(new IngestDocument(otherEsMetadata, otherDocument, otherIngestMetadata));
        if (changed) {
            assertThat(ingestDocument, not(equalTo(otherIngestDocument)));
            assertThat(otherIngestDocument, not(equalTo(ingestDocument)));
        } else {
            assertThat(ingestDocument, equalTo(otherIngestDocument));
            assertThat(otherIngestDocument, equalTo(ingestDocument));
            assertThat(ingestDocument.hashCode(), equalTo(otherIngestDocument.hashCode()));
            WriteableIngestDocument thirdIngestDocument = new WriteableIngestDocument(new IngestDocument(Collections.unmodifiableMap(esMetadata), Collections.unmodifiableMap(document), Collections.unmodifiableMap(ingestMetadata)));
            assertThat(thirdIngestDocument, equalTo(ingestDocument));
            assertThat(ingestDocument, equalTo(thirdIngestDocument));
            assertThat(ingestDocument.hashCode(), equalTo(thirdIngestDocument.hashCode()));
        }
    }

    public void testSerialization() throws IOException {
        Map<String, String> esMetadata = new HashMap<>();
        int numFields = randomIntBetween(1, IngestDocument.MetaData.values().length);
        for (int i = 0; i < numFields; i++) {
            esMetadata.put(randomFrom(IngestDocument.MetaData.values()).getFieldName(), randomAsciiOfLengthBetween(5, 10));
        }
        Map<String, String> ingestMetadata = new HashMap<>();
        numFields = randomIntBetween(1, 5);
        for (int i = 0; i < numFields; i++) {
            ingestMetadata.put(randomAsciiOfLengthBetween(5, 10), randomAsciiOfLengthBetween(5, 10));
        }
        Map<String, Object> document = RandomDocumentPicks.randomDocument(random());
        WriteableIngestDocument writeableIngestDocument = new WriteableIngestDocument(new IngestDocument(esMetadata, document, ingestMetadata));

        BytesStreamOutput out = new BytesStreamOutput();
        writeableIngestDocument.writeTo(out);
        StreamInput streamInput = StreamInput.wrap(out.bytes());
        WriteableIngestDocument otherWriteableIngestDocument = WriteableIngestDocument.readWriteableIngestDocumentFrom(streamInput);
        assertThat(otherWriteableIngestDocument, equalTo(writeableIngestDocument));
    }
}