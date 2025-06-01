package server.domain.DAO;

import java.util.Hashtable;

import common.data.models.HumanBeingModel.HumanBeing;

public interface HumanBeingDAO {

    public void writeData(Hashtable<Integer, HumanBeing> collection);
    public Hashtable<Integer, HumanBeing> readData();

}
