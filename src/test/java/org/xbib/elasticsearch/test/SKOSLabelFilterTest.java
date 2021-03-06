/**
 * Copyright 2010 Bernhard Haslhofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.xbib.elasticsearch.test;

import java.io.IOException;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import org.xbib.elasticsearch.index.analysis.skos.SKOSAnalyzer;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xbib.elasticsearch.plugin.analysis.SKOSAnalysisPlugin;

/**
 * Testing the SKOS Label Filter
 */
public class SKOSLabelFilterTest extends AbstractFilterTest {

    @BeforeMethod
    @Override
    public void setUp() throws Exception {
        super.setUp();
        skosAnalyzer = new SKOSAnalyzer(skosEngine,
                SKOSAnalyzer.ExpansionType.LABEL);
        writer = new IndexWriter(directory, new IndexWriterConfig(SKOSAnalysisPlugin.getLuceneVersion(),
                skosAnalyzer));
    }

    @Test
    public void termQuerySearch() throws CorruptIndexException, IOException {

        Document doc = new Document();
        doc.add(new Field("content", "The quick brown fox jumps over the lazy dog",
                TextField.TYPE_STORED));

        writer.addDocument(doc);

        searcher = new IndexSearcher(DirectoryReader.open(writer, false));

        TermQuery tq = new TermQuery(new Term("content", "hops"));

        Assert.assertEquals(1, TestUtil.hitCount(searcher, tq));

    }

    @Test
    public void phraseQuerySearch() throws CorruptIndexException, IOException {

        Document doc = new Document();
        doc.add(new Field("content", "The quick brown fox jumps over the lazy dog",
                TextField.TYPE_STORED));

        writer.addDocument(doc);

        searcher = new IndexSearcher(DirectoryReader.open(writer, false));

        PhraseQuery pq = new PhraseQuery();
        pq.add(new Term("content", "fox"));
        pq.add(new Term("content", "hops"));

        Assert.assertEquals(1, TestUtil.hitCount(searcher, pq));

    }

    @Test
    public void queryParserSearch() throws IOException, QueryNodeException {

        Document doc = new Document();
        doc.add(new Field("content", "The quick brown fox jumps over the lazy dog",
                TextField.TYPE_STORED));

        writer.addDocument(doc);

        searcher = new IndexSearcher(DirectoryReader.open(writer, false));

        Query query = new StandardQueryParser(skosAnalyzer).parse("\"fox jumps\"", "content");

        Assert.assertEquals(1, TestUtil.hitCount(searcher, query));

        Assert.assertEquals("content:\"fox (jumps hops leaps)\"", query.toString());
        Assert.assertEquals("org.apache.lucene.search.MultiPhraseQuery", query
                .getClass().getName());

        query = new StandardQueryParser(new StandardAnalyzer(SKOSAnalysisPlugin.getLuceneVersion())).parse("\"fox jumps\"", "content");
        Assert.assertEquals(1, TestUtil.hitCount(searcher, query));

        Assert.assertEquals("content:\"fox jumps\"", query.toString());
        Assert.assertEquals("org.apache.lucene.search.PhraseQuery", query
                .getClass().getName());

    }

    @Test
    public void testTermQuery() throws CorruptIndexException, IOException,
            QueryNodeException {

        Document doc = new Document();
        doc.add(new Field("content", "I work for the united nations",
                TextField.TYPE_STORED));

        writer.addDocument(doc);

        searcher = new IndexSearcher(DirectoryReader.open(writer, false));

        StandardQueryParser parser = new StandardQueryParser(new SimpleAnalyzer(SKOSAnalysisPlugin.getLuceneVersion()));

        Query query = parser.parse("united nations", "content");

        Assert.assertEquals(1, TestUtil.hitCount(searcher, query));

    }

    // @Test
    public void displayTokensWithLabelExpansion() throws IOException {

        String text = "The quick brown fox jumps over the lazy dog";

        AnalyzerUtils.displayTokensWithFullDetails(skosAnalyzer, text);
        // AnalyzerUtils.displayTokensWithPositions(synonymAnalyzer, text);

    }
}
