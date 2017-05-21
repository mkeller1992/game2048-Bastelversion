package ch.bfh.game2048.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;

import ch.bfh.game2048.model.GameStatistics;

public class GistUtil {
	GitHubClient client;
	GistService gistService;

	public GistUtil() {
		client = new GitHubClient().setOAuth2Token("ad62dfa8170ad633b427f9bb6802af3772bcfbc6");
		gistService = new GistService(client);
	}

	public String setHighScore(List<GameStatistics> list) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);

			oos.writeObject(list);

			oos.flush();
			oos.close();
			bos.flush();

			String content = Base64.getEncoder().encodeToString(bos.toByteArray());

			setHighScore(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<GameStatistics> getHighScore() {
		try {
			 String content = readHighScore();

			ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(content.getBytes()));
			BOMInputStream bomis = new BOMInputStream(bis);
			ObjectInputStream ois = new ObjectInputStream(bomis);

			return (List<GameStatistics>) ois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String readHighScore() {
		try {

			Gist g = gistService.getGist("cc5c464caba2742d2194c971b5330251");

			GistFile f = g.getFiles().get("Highscore");

			return f.getContent();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void setHighScore(String content) {

		try {

			Gist g = gistService.getGist("cc5c464caba2742d2194c971b5330251");

			GistFile f = g.getFiles().get("Highscore");

			f.setContent(content);

			g.getFiles().put("Highscore", f);

			gistService.updateGist(g);
			System.out.println(g.getUpdatedAt());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
