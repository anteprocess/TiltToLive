package ninja.anteprocess.com.hakaishin;

/**
 * Created by michael on 2017/02/08.
 */
public class DataModel {

    String name;
    String score;
    String rank;

    public DataModel(String name, String score, String rank) {
        this.name=name;
        this.score=score;
        this.rank=rank;
    }

    public String getName() { return name; }
    public String getScore() {
        return score;
    }
    public String getRank() {
        return rank;
    }

}