package be.nabu.eai.developer.util;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;

import be.nabu.eai.developer.api.FindFilter;
import be.nabu.jfx.control.tree.Marshallable;

public class FindContentFilter<T> implements FindFilter<T> {
	
	private RAMDirectory index = new RAMDirectory();
	private IndexWriter writer;
	private IndexSearcher searcher;
	private IndexReader reader;
	private Marshallable<T> marshallable;
	
	public FindContentFilter(Marshallable<T> marshallable) {
		this.marshallable = marshallable;
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer())
			.setIndexDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy())
			.setOpenMode(OpenMode.CREATE_OR_APPEND);
		try {
			writer = new IndexWriter(index, config);
			writer.commit();
			reader = DirectoryReader.open(index);
			searcher = new IndexSearcher(reader);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean accept(T item, String newValue) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void set(T item, String content) {
		Document newDocument = new Document();
		// string field is not tokenized
		newDocument.add(new Field("path", marshallable.marshal(item), StringField.TYPE_STORED));
		// text field is tokenized etc
		newDocument.add(new Field("content", content, TextField.TYPE_STORED));
	}
	
//	private Document getDocument(String path) {
//		Query query = new QueryParser()
//	}
//	
	private void commit() throws IOException {
		writer.commit();
		// as per the documentation we need to reopen the index reader to view
		// the changes made by the commit
		IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) reader);
		if (newReader != null) {
			reader = newReader;
			// we also need to reopen the searcher
			searcher = new IndexSearcher(newReader);
		}
	}
}