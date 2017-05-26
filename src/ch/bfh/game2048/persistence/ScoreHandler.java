package ch.bfh.game2048.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ch.bfh.game2048.model.Highscore;

public class ScoreHandler {

	final static String DIRECTORY_NAME = "Game2048";
	final static String APPDATA_PATH = System.getenv("APPDATA") + "/"+DIRECTORY_NAME+"/";

	public void writeScores(Highscore highscores, String xmlName) throws JAXBException, FileNotFoundException {

		System.out.println("Writing Scores");

		// create JAXB context and instantiate marshaller
		JAXBContext context = JAXBContext.newInstance(Highscore.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		// to marshal "Umlaute" correctly
		m.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");

		// Write to System.out
		m.marshal(highscores, System.out);

		// Write to File
		m.marshal(highscores, new File(APPDATA_PATH + xmlName));
	}

	public Highscore readScores(String xmlName) throws JAXBException, FileNotFoundException {

		createDBIfNotExist(APPDATA_PATH + xmlName);

		JAXBContext context = JAXBContext.newInstance(Highscore.class);

		// get variables from our xml file, created before
		// System.out.println("Output from our XML File: ");
		Unmarshaller um = context.createUnmarshaller();

		Highscore highscores = (Highscore) um.unmarshal(new FileReader(APPDATA_PATH + xmlName));

		// for (GameStatistics g : highscores.getHighscore()) {
		// System.out.println(g.getScore());
		// }
		return highscores;
	}

	/**
	 * - Check if XML Highscore-File already exists
	 * - if not, create it and write xml-header
	 * @param fullPath
	 */
	
	private void createDBIfNotExist(String fullPath) {

	    File directory = new File(APPDATA_PATH);
	    if (! directory.exists()){
	        directory.mkdir();
	        // If you require it to make the entire directory path including parents,
	        // use directory.mkdirs(); here instead.
	    }
		
		Path path = Paths.get(fullPath);

		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			try {
				Files.createFile(path);
				writeScores(new Highscore(), Config.getInstance().getPropertyAsString("highscoreFileName"));
			} catch (IOException | JAXBException e) {
			}
		}
	}

}
