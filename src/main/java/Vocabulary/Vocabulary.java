package Vocabulary;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Datastores.Dataset;
import Datastores.FileDataAdapter;
import Utils.POSTagConverter;

public class Vocabulary {
	private Map<DBWord, Integer> VocSearchForID = new HashMap<>();
	private List<DBWord> VocList = new ArrayList<>();
	private Map<String, List<Integer>> VocText2IDs = new HashMap<>();
	// private static Vocabulary instance = null;
	private Dataset mDataset = null;
	private int mLevel = -1;
	// public static synchronized Vocabulary getInstance()
	// throws ClassNotFoundException, SQLException {
	// if (instance == null) {
	// instance = new Vocabulary();
	// }
	// return instance;
	// }

	public Dataset getDataset() {
		return mDataset;
	}

	public List<DBWord> getWordList() {
		return VocList;
	}

	public Vocabulary(Dataset dat, int level) {
		mLevel = level;
		mDataset = dat;
	}

	public void writeWordsToFile(String fileName) throws FileNotFoundException {
		System.out.print(">>Writing Words to file");

		PrintWriter pw = new PrintWriter(fileName);
		for (DBWord word : VocList) {
			pw.println(word.getText() + "," + word.getPOS() + ","
					+ word.getCount());
		}
		pw.close();

	}

	public int loadDBKeyword() throws SQLException, ClassNotFoundException,
			UnsupportedEncodingException, IOException {
		List<DBWord> wordListFromDB = FileDataAdapter.getInstance()
				.getVoc(mDataset.getDirectory(), mLevel);
		for (DBWord word : wordListFromDB) {
			// add to voc
			addDBWord(word);
		}
		return wordListFromDB.size();
	}

	private void addDBWord(DBWord w) throws IOException {

		int id = w.getID();
		// if (id != VocList.size())
		// throw new IOException(
		// "got error while reading words from DB, data might be corrupted");
		VocList.add(w);
		VocSearchForID.put(w, id);
		List<Integer> IDs = VocText2IDs.get(w.getText());
		if (IDs == null)
			IDs = new ArrayList<>();
		IDs.add(id);
		VocText2IDs.put(w.getText(), IDs);
	}

	private int addNewWord(String text, int POS, int count, int positionLast, int positionFirst) {
		int id = VocList.size();
		DBWord w = new DBWord(id, text, POS, count, positionLast, positionFirst);
		VocList.add(w);
		VocSearchForID.put(w, id);

		List<Integer> IDs = VocText2IDs.get(text);
		if (IDs == null)
			IDs = new ArrayList<>();
		IDs.add(id);
		VocText2IDs.put(text, IDs);

		return id;
	}

	public int addWord(String text, int POS,int positionLast, int positionFirst)
			throws SQLException, ParseException {
		Integer wordID = VocSearchForID
				.get(new DBWord(-1, text.intern(), POS, -1, positionLast, positionFirst));
		// not in voc, create a new entry for this word
		if (wordID == null) {
			wordID = addNewWord(text, POS, 1, positionLast, positionFirst);
		} else {
			VocList.get(wordID).incrementCount();
		}
		return wordID;
	}

	private DBWord getWord(int keywordid) {
		DBWord word = null;
		try {
			word = VocList.get(keywordid);
		} catch (IndexOutOfBoundsException e) {
			// do nothing, just return null is fine
		}
		return word;
	}

	public DBWord getWordFromDB(int keywordid)
			throws SQLException, UnsupportedEncodingException, IOException {
		DBWord word = getWord(keywordid);
		if (word == null) {
			word = FileDataAdapter.getInstance().getWord(keywordid,
					mDataset.getDirectory(), mLevel);
			addDBWord(word);
		}
		return word;
	}

	public void writeToDB() throws ClassNotFoundException, SQLException,
			UnsupportedEncodingException, IOException {
		// TODO Auto-generated method stub
		System.out.println(">> Writing words data to Database");
		FileDataAdapter.getInstance().writeVoc(VocList, mDataset.getDirectory(),
				mLevel);
		System.out.println(">> Wrote " + VocList.size() + " words");

	}

	public List<Integer> getWordIDs(String word) {
		// TODO Auto-generated method stub
		return VocText2IDs.get(word);
	}

	// public String getText(int id) throws Exception {
	// // TODO Auto-generated method stub
	// DBWord word = getWordFromDB(id);
	// if (word == null)
	// throw new Exception("There is no word for the id = " + id);
	// return word.getText();
	// }

}
